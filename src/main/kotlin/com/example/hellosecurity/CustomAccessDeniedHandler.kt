package com.example.hellosecurity

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler : AccessDeniedHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: org.springframework.security.access.AccessDeniedException?
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val email = authentication?.name ?: "Unknown"
        val authorities = authentication?.authorities?.map { it.authority } ?: emptyList()
        val requestURI = request.requestURI

        // 1. 상세 거부 사유 추론 및 로깅
        val reason = if (!authorities.contains("ROLE_MANAGER") && !authorities.contains("ROLE_STAFF")) {
            "어드민 권한(ROLE_MANAGER 또는 ROLE_STAFF)이 완전히 누락되었습니다."
        } else if (authorities.contains("ROLE_STAFF")) {
            "STAFF 권한을 보유 중이나, 해당 API 리소스에 매핑된 세부 회사 권한 검증에 실패했습니다."
        } else {
            "요청 조건 정책에 부합하지 않는 권한입니다."
        }

        log.error("❌ [403 Forbidden] 접근 거부 발생 - 유저: $email, 권한: $authorities, 요청경로: $requestURI, 추론사유: $reason")

        // 2. 클라이언트(프론트엔드)에게 보낼 예쁜 JSON 응답 설정
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = "application/json;charset=UTF-8"

        val jsonResponse = """
            {
              "error": "Forbidden",
              "status": 403,
              "path": "$requestURI",
              "message": "해당 리소스에 접근할 권한이 없습니다.",
              "details": "$reason"
            }
        """.trimIndent()

        response.writer.write(jsonResponse)
    }
}