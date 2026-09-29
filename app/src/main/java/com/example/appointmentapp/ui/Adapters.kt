package com.example.appointmentapp.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appointmentapp.R
import com.example.appointmentapp.data.model.Appointment
import com.example.appointmentapp.data.model.Service

class ServiceAdapter(
    private var services: List<Service> = emptyList(),
    private val onClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDetails: TextView = view.findViewById(R.id.tvServiceDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = services[position]
        holder.tvName.text = service.name
        holder.tvDetails.text = "$${service.price} - ${service.durationMinutes} mins"
        if (service.description.isNotBlank()) {
            holder.tvDetails.text = holder.tvDetails.text.toString() + "\n" + service.description
        }
        holder.itemView.setOnClickListener { onClick(service) }
    }

    override fun getItemCount() = services.size

    fun updateList(newList: List<Service>) {
        services = newList
        notifyDataSetChanged()
    }
}

class AppointmentAdapter(
    private var appointments: List<Appointment> = emptyList(),
    private val onClick: (Appointment) -> Unit = {}
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvRef: TextView = view.findViewById(R.id.tvRef)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = appointments[position]
        holder.tvName.text = item.serviceName
        holder.tvDate.text = "Date: ${item.date}"
        holder.tvTime.text = "Time: ${item.timeSlot}"
        holder.tvStatus.text = "Status: ${item.status}"
        holder.tvRef.text = "Receipt: #${item.id.takeLast(8).uppercase()}"

        val color = when (item.status) {
            "BOOKED" -> R.color.badgeBooked
            "CONFIRMED" -> R.color.badgeConfirmed
            "CHECKED_IN" -> R.color.badgeCheckedIn
            "COMPLETED" -> R.color.badgeCompleted
            "CANCELLED" -> R.color.badgeCancelled
            "NO_SHOW" -> R.color.badgeNoShow
            else -> R.color.badgeBooked
        }
        holder.tvStatus.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, color)
            )
        )
        holder.tvStatus.setTextColor(android.graphics.Color.WHITE)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = appointments.size

    fun updateList(newList: List<Appointment>) {
        appointments = newList
        notifyDataSetChanged()
    }

    fun currentList() = appointments
}