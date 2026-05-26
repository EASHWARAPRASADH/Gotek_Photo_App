package com.studentidphotocapture.app.ui.csvimport

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.util.CSVImportError

class ImportErrorAdapter(private val errors: List<CSVImportError>) : RecyclerView.Adapter<ImportErrorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRowNumber: TextView = view.findViewById(R.id.tvRowNumber)
        val tvStudentId: TextView = view.findViewById(R.id.tvStudentId)
        val tvErrorMessage: TextView = view.findViewById(R.id.tvErrorMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_import_error, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val error = errors[position]
        holder.tvRowNumber.text = "Row ${error.rowNumber}"
        holder.tvStudentId.text = error.studentId ?: "N/A"
        holder.tvErrorMessage.text = error.errorMessage
    }

    override fun getItemCount(): Int = errors.size
}
