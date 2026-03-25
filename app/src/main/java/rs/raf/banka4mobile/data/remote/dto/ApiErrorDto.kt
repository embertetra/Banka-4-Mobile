package rs.raf.banka4mobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val message: String,
    val status: String,
    val timestamp: String
)
