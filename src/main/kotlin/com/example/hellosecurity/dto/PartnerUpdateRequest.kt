package com.example.hellosecurity.dto

// 파트너 수정 요청 바디
data class PartnerUpdateRequest(
    val name: String,
    val location: String
)