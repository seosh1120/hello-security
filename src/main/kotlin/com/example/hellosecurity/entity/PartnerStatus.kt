package com.example.hellosecurity.entity

import java.util.Locale

enum class PartnerStatus(
    val description: String,
) {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    ;

    companion object {
        fun fromString(value: String?): PartnerStatus? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}