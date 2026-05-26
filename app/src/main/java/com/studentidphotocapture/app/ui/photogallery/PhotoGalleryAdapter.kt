package com.studentidphotocapture.app.ui.photogallery

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.studentidphotocapture.app.R
import com.studentidphotocapture.app.data.model.Student
import java.io.File

class PhotoGalleryAdapter(
    private val onStudentClick: (Student) -> Unit
) : RecyclerView.Adapter<PhotoGalleryAdapter.PhotoViewHolder>() {

    private var students: List<Student> = emptyList()

    fun updateStudents(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_gallery, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount(): Int = students.size

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhotoThumbnail)
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvAdmissionNumber: TextView = itemView.findViewById(R.id.tvAdmissionNumber)
        private val tvClassSection: TextView = itemView.findViewById(R.id.tvClassSection)

        fun bind(student: Student) {
            tvStudentName.text = student.name
            tvAdmissionNumber.text = "Admission: ${student.admissionNumber}"
            tvClassSection.text = "${student.classGrade} - ${student.section}"

            // Load photo thumbnail
            loadPhotoThumbnail(student.id)

            itemView.setOnClickListener {
                onStudentClick(student)
            }
        }

        private fun loadPhotoThumbnail(studentId: String) {
            try {
                // Try to get photo from common storage location
                val photoFile = getPhotoFile(studentId)
                if (photoFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    ivPhoto.setImageBitmap(bitmap)
                } else {
                    // Set placeholder if photo not found
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                // Set placeholder on error
                ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }

        private fun getPhotoFile(studentId: String): File {
            // This should match the photo saving logic in CameraActivity
            val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_PICTURES
            )
            val studentPhotosDir = File(picturesDir, "StudentIDPhotos")
            return File(studentPhotosDir, "${studentId}.jpg")
        }
    }
}
