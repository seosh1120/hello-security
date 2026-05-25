package com.example.hellosecurity.dto

import com.example.hellosecurity.entity.PartnerStatus

data class PartnerStatusUpdateRequest(
    val status: PartnerStatus // 활성화 / 비활성화 열거형(Enum)
)