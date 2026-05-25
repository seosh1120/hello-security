package com.example.hellosecurity.dto

// 파트너 생성 요청 바디
data class PartnerCreateRequest(
    val id: String,
    val name: String,
    val location: String
)