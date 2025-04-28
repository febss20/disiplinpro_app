package com.dsp.disiplinpro.data.model

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val fotoProfil: String? = null,
    val fotoProfilObjectKey: String? = null,
    val fotoProfilExpiration: Long? = null,
    val googleId: String? = null,
    val isGoogleUser: Boolean = false,
    val lastLogin: Long = 0
)