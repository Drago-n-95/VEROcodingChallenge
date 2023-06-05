package com.drago.veroassignment

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class DataUpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    lateinit var resourcesList: List<Resource>

    override fun doWork(): Result {
        try {
            val data = fetchDataFromServer()

            updateData(data)

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

    companion object {
        // Function to create a periodic work request for DataUpdateWorker
        fun createWorkRequest(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<DataUpdateWorker>(60, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().build())
                .build()
        }
    }

    private fun fetchDataFromServer(): List<Resource> {
        val apiClient = ApiClient()

        // Authorizes with the server and fetch resources
        apiClient.authorize("365", "1") { accessToken ->
            if (accessToken != null) {
                Log.i("Look access token", accessToken.toString())
                // Authorization successful, fetch resources
                apiClient.getResources(accessToken, object : ResourcesCallback {
                    override fun onSuccess(resources: List<Resource>) {
                        resourcesList = resources
                    }

                    override fun onFailure() {
                        // Handles API request failure
                    }
                })
            } else {
                // Handles authorization failure
            }
        }

        return resourcesList
    }

    private fun updateData(data: List<Resource>) {
        // Performs data update operation (e.g., update UI, update database, etc.)
        ResourceAdapter(data)
    }
}
