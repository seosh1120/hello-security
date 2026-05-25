package com.example.hellosecurity.repository

import com.example.hellosecurity.entity.AdminUser
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<AdminUser, Long> {
    fun findByEmail(email: String): AdminUser?
}