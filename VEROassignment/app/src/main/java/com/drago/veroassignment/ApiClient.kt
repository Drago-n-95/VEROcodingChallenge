package com.drago.veroassignment

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ApiClient {
    private val client = OkHttpClient()

    // Function to authorize with the API and obtain an access token
    fun authorize(username: String, password: String, callback: (accessToken: String?) -> Unit) {
        val mediaType = "application/json".toMediaTypeOrNull()

        // Creates a JSON request body with the username and password
        val requestBody = JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString()

        // Creates a request to the authorization endpoint
        val request = Request.Builder()
            .url("https://api.baubuddy.de/index.php/login")
            .post(requestBody.toRequestBody(mediaType))
            .addHeader("Authorization", "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz")
            .addHeader("Content-Type", "application/json")
            .build()

        // Send the request asynchronously and handle the response or failure
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handles failure
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Parses the JSON response to extract the access token
                    val json = response.body?.string()?.let { JSONObject(it) }
                    val accessToken = json?.getJSONObject("oauth")?.getString("access_token")
                    callback(accessToken)
                } else {
                    // Handles unsuccessful response
                    callback(null)
                }
            }
        })
    }

    // Function to fetch resources from the API using the access token
    fun getResources(accessToken: String, callback: ResourcesCallback) {
        // Creates a GET request to the resources endpoint with the access token
        val request = Request.Builder()
            .url("https://api.baubuddy.de/dev/index.php/v1/tasks/select")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        // Sends the request asynchronously and handle the response or failure
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Parse the JSON response to obtain a list of resources
                    val jsonResponse = response.body?.string()
                    val resources = parseResources(jsonResponse)
                    callback.onSuccess(resources)
                } else {
                    callback.onFailure()
                }
            }
        })
    }

    // Helper function to parse the resources from the API response
    private fun parseResources(jsonResponse: String?): List<Resource> {
        val resources = mutableListOf<Resource>()
        try {
            val jsonArray = JSONArray(jsonResponse)
            for (i in 0 until jsonArray.length()) {
                // Extract the task, title, description, and colorCode from each JSON object
                val jsonObject = jsonArray.getJSONObject(i)
                val task = jsonObject.getString("task")
                val title = jsonObject.getString("title")
                val description = jsonObject.getString("description")
                val colorCode = jsonObject.getString("colorCode")

                // Creates a Resource object and add it to the list
                resources.add(Resource(task, title, description, colorCode))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return resources
    }
}
