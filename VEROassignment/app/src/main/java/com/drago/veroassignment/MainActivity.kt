package com.drago.veroassignment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ResourcesCallback {

    // Views
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    // Data
    private var resourcesList: List<Resource> = emptyList()
    private lateinit var resourceAdapter: ResourceAdapter
    private var searchQuery: String = ""

    // API client
    private lateinit var apiClient: ApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create an instance of the API client
        apiClient = ApiClient()

        // Authorize with the API client
        apiClient.authorize("365", "1") { accessToken ->

            if (accessToken != null) {
                // Authorization successful, fetch resources
                apiClient.getResources(accessToken, object : ResourcesCallback {

                    override fun onSuccess(resources: List<Resource>) {
                        runOnUiThread {
                            resourcesList = resources
                            resourceAdapter = ResourceAdapter(resourcesList)
                            recyclerView.adapter = resourceAdapter
                        }
                    }

                    override fun onFailure() {
                        Toast.makeText(this@MainActivity, "Failed to fetch resources", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@MainActivity, "Authorization failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener {
            fetchData()
        }

        // Schedule periodic data updates using WorkManager
        scheduleDataUpdates()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu resource file
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_search -> {
                // Handle search menu item click
                val searchDialogFragment = SearchDialogFragment.newInstance(resourceAdapter, resourcesList)
                searchDialogFragment.show(supportFragmentManager, "SearchDialogFragment")
                true
            }
            R.id.menu_scan -> {
                // Handle scan menu item click
                startQrCodeScanner()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSuccess(resources: List<Resource>) {
        // This method is not used in this class
    }

    override fun onFailure() {
        // This method is not used in this class
    }

    private fun fetchData() {
        // Use coroutines to perform the data fetching task in the background

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Simulating a delay for fetching data
                delay(2000)

                apiClient.authorize("365", "1") { accessToken ->
                    if (accessToken != null) {
                        Log.i("Look access token", accessToken.toString())
                        apiClient.getResources(accessToken, object : ResourcesCallback {
                            override fun onSuccess(resources: List<Resource>) {
                                runOnUiThread {
                                    resourcesList = resources
                                    resourceAdapter.updateList(resourcesList)
                                }
                                showToast("Data refreshed successfully!")
                            }

                            override fun onFailure() {
                                runOnUiThread {
                                    showToast("Failed to fetch resources")
                                }
                            }
                        })
                    } else {
                        runOnUiThread {
                            showToast("Authorization failed")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showToast("Failed to refresh data!")
                }
            } finally {
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        // Schedule periodic data updates using WorkManager
        val workRequest = DataUpdateWorker.createWorkRequest()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "dataUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun showToast(message: String) {
        // Show a toast message on the UI thread
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleDataUpdates() {
        // Schedule periodic data updates using WorkManager

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dataUpdateWorkRequest = PeriodicWorkRequestBuilder<DataUpdateWorker>(
            repeatInterval = 1, // Repeat every 1 day
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "DataUpdateWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                dataUpdateWorkRequest
            )
    }

    private fun startQrCodeScanner() {
        // Start the QR code scanner
        val integrator = IntentIntegrator(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Scan a QR code")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            val scannedText = result.contents
            if (scannedText != null) {
                searchQuery = scannedText
                performSearch(searchQuery)
            }
        }
    }

    private fun performSearch(query: String) {
        // Perform a search on the resources list based on the given query

        val filteredList = resourcesList.filter { resource ->
            resource.task.contains(query, ignoreCase = true) ||
                    resource.title.contains(query, ignoreCase = true) ||
                    resource.description.contains(query, ignoreCase = true)
        }
        resourceAdapter.updateList(filteredList)
    }
}
