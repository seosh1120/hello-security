package com.example.hellosecurity.entity

enum class AdminUserRole(
    val description: String,
) {
    MANAGER("Manager"),
    STAFF("Staff"),
    ;

    companion object {
        fun fromString(value: String?): AdminUserRole? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}