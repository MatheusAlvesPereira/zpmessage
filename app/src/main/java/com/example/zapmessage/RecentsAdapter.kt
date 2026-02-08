package com.example.zapmessage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentsAdapter(private val phoneNumbers: List<PhoneData>) : RecyclerView.Adapter<RecentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phoneNumberTextView: TextView = view.findViewById(R.id.itemPhoneNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.phoneNumberTextView.text = phoneNumbers[position].phoneNumber
    }

    override fun getItemCount() = phoneNumbers.size
}
