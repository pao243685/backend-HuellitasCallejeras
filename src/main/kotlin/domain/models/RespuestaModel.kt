package com.example.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val message: String,
    val url: String,
)