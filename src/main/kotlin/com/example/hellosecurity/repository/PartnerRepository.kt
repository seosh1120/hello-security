package com.example.hellosecurity.repository

import com.example.hellosecurity.entity.Partner
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PartnerRepository : JpaRepository<Partner, String> {
    @Query("""
        SELECT p FROM Partner p 
        WHERE p.id IN (
            SELECT apm.partnerId 
            FROM AdminPartnerMapping apm 
            WHERE apm.adminUser.email = :email
        )
    """)
    fun findAllByAdminEmail(email: String, pageable: Pageable): Page<Partner>
}