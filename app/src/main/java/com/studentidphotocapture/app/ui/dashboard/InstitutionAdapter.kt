package com.studentidphotocapture.app.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.studentidphotocapture.app.R

data class InstitutionSummary(
    val code: String,
    val name: String,
    val totalStudents: Int,
    val completedStudents: Int
) {
    val percent: Int
        get() = if (totalStudents > 0) (completedStudents * 100) / totalStudents else 0
    val pendingStudents: Int
        get() = totalStudents - completedStudents
}

class InstitutionAdapter(
    private var institutions: List<InstitutionSummary>,
    private val onViewDetails: (String) -> Unit
) : RecyclerView.Adapter<InstitutionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvInstitutionName)
        val tvCode: TextView = view.findViewById(R.id.tvInstitutionCode)
        val tvPercent: TextView = view.findViewById(R.id.tvPercent)
        val tvStats: TextView = view.findViewById(R.id.tvStats)
        val tvPending: TextView = view.findViewById(R.id.tvPending)
        val tvPrincipal: TextView = view.findViewById(R.id.tvPrincipal)
        val tvSync: TextView = view.findViewById(R.id.tvSync)
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.institutionProgressBar)
        val btnView: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_institution, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = institutions[position]
        holder.tvName.text = item.name
        holder.tvCode.text = "CODE: ${item.code}"
        holder.tvPercent.text = "${item.percent}%"
        holder.tvStats.text = "Captured: ${item.completedStudents} / Total: ${item.totalStudents}"
        holder.tvPending.text = "Pending: ${item.pendingStudents}"
        holder.progressBar.progress = item.percent
        
        // Dummy creative data based on code
        val principalName = when(item.code) {
            "Bharathi Vidyalaya HSS" -> "Dr. K. Senthilvelan"
            "St. Mary's Matriculation" -> "Mrs. M. Anandhi"
            "Tagore Higher Sec. School" -> "Mr. R. Paneerselvam"
            else -> "Dr. S. Radhakrishnan"
        }
        val syncTime = when(item.code) {
            "Bharathi Vidyalaya HSS" -> "Last Sync: 10 mins ago"
            "St. Mary's Matriculation" -> "Last Sync: 1 hr ago"
            "Tagore Higher Sec. School" -> "Last Sync: 5 mins ago"
            else -> "Last Sync: Just now"
        }
        
        holder.tvPrincipal.text = "Principal: $principalName"
        holder.tvSync.text = syncTime
        
        holder.btnView.setOnClickListener {
            onViewDetails(item.code)
        }
    }

    override fun getItemCount() = institutions.size

    fun updateData(newList: List<InstitutionSummary>) {
        institutions = newList
        notifyDataSetChanged()
    }
}
