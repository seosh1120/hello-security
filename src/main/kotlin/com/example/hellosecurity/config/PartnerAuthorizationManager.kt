package com.example.hellosecurity.config

import com.example.hellosecurity.security.AdminUserDetails
import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.access.intercept.RequestAuthorizationContext
import org.springframework.stereotype.Component
import java.util.function.Supplier

@Component
class PartnerAuthorizationManager : AuthorizationManager<RequestAuthorizationContext> {

    override fun check(
        authentication: Supplier<Authentication>,
        context: RequestAuthorizationContext
    ): AuthorizationDecision {
        val partnerId = context.variables["partnerId"]
            ?: return AuthorizationDecision(false)

        val auth = authentication.get()

        val allowed =
            auth.authorities.any { it.authority == "ROLE_MANAGER" } ||
                    auth.authorities.any { it.authority == "ROLE_STAFF" } &&
                    hasAccess(auth, partnerId)

        return AuthorizationDecision(allowed)
    }

    fun hasAccess(authentication: Authentication, partnerId: String): Boolean {
        val userDetails = authentication.principal as? AdminUserDetails ?: return false
        val hasPermission = userDetails.accessiblePartnerIds.contains(partnerId)

        // 💡 매핑 권한이 없을 때 구체적인 사유를 서버 로그에 기록!
        if (!hasPermission) {
            log.warn("🚨 [403 인가 실패 사유] 유저(${userDetails.email})는 STAFF 권한을 가졌으나, 요청한 회사($partnerId)에 대한 열람/수정 매핑 권한이 없습니다.")
        }

        return hasPermission
    }

    companion object {
        private val log = LoggerFactory.getLogger(javaClass)
    }
}
