package com.example.hellosecurity.repository

import com.example.hellosecurity.entity.AdminPartnerMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface AdminPartnerMappingRepository : JpaRepository<AdminPartnerMapping, Long> {
    @Query("""
        SELECT COUNT(apm) > 0
        FROM AdminPartnerMapping apm
        WHERE apm.adminUser.email = :email AND apm.partnerId = :partnerId
    """)
    fun existsByEmailAndPartnerId(email: String, partnerId: String): Boolean

    // 💡 기존 exists 쿼리는 그대로 두고, 아래 메서드를 새로 추가하자!
    @Query("""
        SELECT apm.partnerId 
        FROM AdminPartnerMapping apm 
        WHERE apm.adminUser.id = :adminUserId
    """)
    fun findPartnerIdsByAdminUserId(adminUserId: Long): List<String>
}