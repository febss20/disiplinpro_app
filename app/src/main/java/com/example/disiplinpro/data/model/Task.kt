package com.example.disiplinpro.data.model
import com.google.firebase.Timestamp

data class Task(
    val id: String = "",
    val judulTugas: String = "",
    val matkul: String = "",
    val tanggal: Timestamp = Timestamp.now(),
    val waktu: Timestamp = Timestamp.now(),
    val isCompleted: Boolean = false
)