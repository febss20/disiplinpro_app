package com.example.disiplinpro.data.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val fotoProfil: String? = null,
    val fotoProfilObjectKey: String? = null,
    val fotoProfilExpiration: Long? = null
)