package com.example.appointmentapp.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        holder.itemView.setOnClickListener { onClick(service) }
    }

    override fun getItemCount() = services.size

    fun updateList(newList: List<Service>) {
        services = newList
        notifyDataSetChanged()
    }
}

class AppointmentAdapter(
    private var appointments: List<Appointment> = emptyList()
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
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
    }

    override fun getItemCount() = appointments.size

    fun updateList(newList: List<Appointment>) {
        appointments = newList
        notifyDataSetChanged()
    }
}
