package com.drago.veroassignment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class SearchDialogFragment: DialogFragment() {
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var resourceAdapter: ResourceAdapter
    private lateinit var resourcesList: List<Resource>

    companion object {
        // Static method to create a new instance of the SearchDialogFragment
        fun newInstance(resourceAdapter: ResourceAdapter, resourcesList: List<Resource>): SearchDialogFragment {
            val fragment = SearchDialogFragment()
            fragment.resourceAdapter = resourceAdapter
            fragment.resourcesList = resourcesList
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflates the layout for the dialog fragment
        val view = inflater.inflate(R.layout.search_dialog, container, false)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)

        // Sets an onClickListener for the search button
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            performSearch(query)
            dismiss() // Dismisses the dialog fragment after performing the search
        }
        return view
    }

    // Function to perform the search based on the query entered by the user
    private fun performSearch(query: String) {
        val colorMapping = mapOf(
            "red" to "#FF0000",
            "green" to "#00FF00",
            "blue" to "#0000FF",
            // Adds more color mappings as needed
        )

        // Filters the resources list based on the query
        val filteredList = resourcesList.filter { resource ->
            resource.task.contains(query, ignoreCase = true) ||
                    resource.title.contains(query, ignoreCase = true) ||
                    resource.description.contains(query, ignoreCase = true) ||
                    resource.colorCode.contains(query, ignoreCase = true) ||
                    colorMapping.any { (colorName, colorCode) ->
                        colorName.contains(query, ignoreCase = true) && resource.colorCode.equals(colorCode, ignoreCase = true)
                    }
        }

        // Updates the resource adapter with the filtered list of resources
        resourceAdapter.updateList(filteredList)
    }
}
