package com.example.zapmessage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentsAdapter(
    private val phoneNumbers: List<PhoneData>,
    private val onMessageClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<RecentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phoneNumberTextView: TextView = view.findViewById(R.id.itemPhoneNumber)
        val btnMessage: ImageButton = view.findViewById(R.id.btnMessage)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recent_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phoneData = phoneNumbers[position]
        holder.phoneNumberTextView.text = phoneData.phoneNumber
        
        holder.btnMessage.setOnClickListener {
            phoneData.phoneNumber?.let { onMessageClick(it) }
        }
        
        holder.btnDelete.setOnClickListener {
            phoneData.phoneNumber?.let { onDeleteClick(it) }
        }
    }

    override fun getItemCount() = phoneNumbers.size
}
