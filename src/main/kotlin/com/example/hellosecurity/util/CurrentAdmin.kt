package com.example.hellosecurity.util

import org.springframework.security.core.context.SecurityContextHolder

object CurrentAdmin {
    /**
     * 💡 현재 로그인한 사용자가 최상위 관리자(MANAGER) 권한인지 체크
     */
    fun isManager(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        if (!authentication.isAuthenticated) return false
        return authentication.authorities.any { it.authority == "ROLE_MANAGER" }
    }

    /**
     * 💡 현재 로그인한 사용자가 일반 관리자(STAFF) 권한인지 체크
     */
    fun isStaff(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication ?: return false
        if (!authentication.isAuthenticated) return false
        return authentication.authorities.any { it.authority == "ROLE_STAFF" }
    }

    /**
     * 💡 (부차적) 스태프 조회 쿼리에 필요한 이메일을 안전하게 꺼내오는 내부 기능
     */
    fun getCurrentEmail(): String {
        val authentication = SecurityContextHolder.getContext().authentication ?: return ""
        return authentication.name // 시큐리티 User의 username(이메일) 반환
    }
}