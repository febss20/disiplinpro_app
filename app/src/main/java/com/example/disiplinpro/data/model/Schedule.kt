package com.example.disiplinpro.data.model
import com.google.firebase.Timestamp

data class Schedule(
    val id: String = "",
    val hari: String = "",
    val waktuMulai: Timestamp = Timestamp.now(),
    val waktuSelesai: Timestamp = Timestamp.now(),
    val matkul: String = "",
    val ruangan: String = ""
)