package com.example.hellosecurity.config

import com.example.hellosecurity.repository.AdminPartnerMappingRepository
import com.example.hellosecurity.security.AdminUserDetails
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component

@Component("partnerChecker")
class PartnerPermissionChecker(
    private val adminPartnerMappingRepository: AdminPartnerMappingRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)
    fun hasAccess(authentication: Authentication, partnerId: String): Boolean {
        val userDetails = authentication.principal as? AdminUserDetails ?: return false
        val hasPermission = userDetails.accessiblePartnerIds.contains(partnerId)

        // 💡 매핑 권한이 없을 때 구체적인 사유를 서버 로그에 기록!
        if (!hasPermission) {
            log.warn("🚨 [403 인가 실패 사유] 유저(${userDetails.email})는 STAFF 권한을 가졌으나, 요청한 회사($partnerId)에 대한 열람/수정 매핑 권한이 없습니다.")
        }

        return hasPermission
    }
}