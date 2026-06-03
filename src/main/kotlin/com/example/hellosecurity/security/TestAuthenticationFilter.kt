package com.example.hellosecurity.security;

import com.example.hellosecurity.entity.AdminUserRole
import com.example.hellosecurity.repository.AdminPartnerMappingRepository
import com.example.hellosecurity.repository.AdminUserRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Profile("local")
@Component
class TestAuthenticationFilter(
    private val adminUserRepository: AdminUserRepository,
    private val adminPartnerMappingRepository: AdminPartnerMappingRepository
) : OncePerRequestFilter() {

    // 💡 SLF4J 로거 선언
    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (SecurityContextHolder.getContext().authentication == null) {
            val requestURI = request.requestURI
            val email = request.getHeader("X-TEST-EMAIL")

            if (!email.isNullOrBlank()) {
                log.info("==========testAuthenticationFilter 사용해서 로그인 시도합니다. email=$email")
                // 2. DB에서 유저 조회
                val adminUser = adminUserRepository.findByEmail(email) ?: run {
                    // ❌ DB 조회 실패 로그
                    log.warn("⚠️ [401 인증 실패] 토큰은 유효하지만 DB(admin_users)에 등록되지 않은 이메일입니다. -> email: $email, 경로: $requestURI")
                    return handleUnauthorized(response, "등록되지 않은 어드민 유저입니다.", requestURI)
                }


                val adminUserMappingList = when (adminUser.role) {
                    AdminUserRole.STAFF -> adminPartnerMappingRepository.findPartnerIdsByAdminUserId(adminUser.id)
                    else -> emptyList() // 💡 STAFF 외의 모든 케이스는 빈 리스트 처리!
                }

                val adminUserDetails = AdminUserDetails(
                    adminId = adminUser.id,
                    email = email,
                    role = adminUser.role,
                    accessiblePartnerIds = adminUserMappingList
                )

                // 3. Spring Security 권한(Role) 설정 (DB의 MANAGER, STAFF 앞에 ROLE_ 붙이기)
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${adminUser.role}"))

                val authentication = UsernamePasswordAuthenticationToken(
                    adminUserDetails, null, authorities
                )

                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().authentication = authentication

                //  인증 성공 로그
                log.info("✓ [인증 성공] 유저 로그인 완료 -> email: ${adminUser.email}, 권한: ${authorities.map { it.authority }}, 경로: $requestURI")
            }
        }

        filterChain.doFilter(request, response)
    }

    // 클라이언트에게 내려가는 401 JSON 응답도 포맷을 예쁘게 맞춰줬어!
    private fun handleUnauthorized(response: HttpServletResponse, message: String, path: String) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"

        val jsonResponse = """
            {
              "error": "Unauthorized",
              "status": 401,
              "path": "$path",
              "message": "$message"
            }
        """.trimIndent()

        response.writer.write(jsonResponse)
    }
}