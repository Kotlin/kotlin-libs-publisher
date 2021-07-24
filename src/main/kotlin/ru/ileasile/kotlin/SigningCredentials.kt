package ru.ileasile.kotlin

data class SigningCredentials(
    val key: String,
    val privateKey: String,
    val keyPassphrase: String
)
