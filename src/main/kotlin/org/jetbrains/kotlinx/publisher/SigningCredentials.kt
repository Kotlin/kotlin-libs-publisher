package org.jetbrains.kotlinx.publisher

data class SigningCredentials(
    val key: String,
    val privateKey: String,
    val keyPassphrase: String
)
