package com.drago.veroassignment

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResourceAdapter(private var resources: List<Resource>) : RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {

    // Function to update the list of resources with a new list and notify the adapter
    fun updateList(newList: List<Resource>) {
        resources = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflates the item layout and create a ViewHolder
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_resource, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Binds the data of the resource at the specified position to the ViewHolder
        holder.taskTextView.text = resources[position].task
        holder.titleTextView.text = resources[position].title
        holder.descriptionTextView.text = resources[position].description
        Log.e("RA colorcodes", resources[position].colorCode)

        // Sets the background color of the colorCodeView using the colorCode of the resource
        val colorCode = resources[position].colorCode

        try {
            holder.colorCodeView.setBackgroundColor(Color.parseColor(colorCode))
        } catch (e: IllegalArgumentException) {
            Log.e("RA", "Invalid color code: $colorCode")
        }
    }

    override fun getItemCount(): Int {
        // Returns the total number of resources in the list
        return resources.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // ViewHolder class to hold the views of each item in the RecyclerView
        val taskTextView: TextView = itemView.findViewById(R.id.taskTextView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val colorCodeView: View = itemView.findViewById(R.id.colorCodeView)
    }
}
