package com.example.hellosecurity.security;

import com.example.hellosecurity.entity.AdminUserRole
import com.example.hellosecurity.repository.AdminPartnerMappingRepository
import com.example.hellosecurity.repository.AdminUserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val adminUserRepository: AdminUserRepository,
    private val jwtProvider: JwtProvider,
    private val adminPartnerMappingRepository: AdminPartnerMappingRepository
) : OncePerRequestFilter() {

    // 💡 SLF4J 로거 선언
    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        val requestURI = request.requestURI

        // Bearer 토큰 형식이 아닐 때 (비인증 퍼블릭 API나 H2 콘솔 접근 등)
        if (SecurityContextHolder.getContext().authentication != null || authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        log.info("==========jwtAuthenticationFilter 사용해서 로그인 시도합니다.")

        val token = authHeader.substring(7)

        try {
            // 1. JWT 토큰에서 이메일 추출 및 검증
            val email = jwtProvider.extractEmail(token)

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

        } catch (e: io.jsonwebtoken.ExpiredJwtException) {
            // ❌ 토큰 만료 로그
            log.warn("⚠️ [401 인증 실패] 만료된 JWT 토큰입니다. -> 경로: $requestURI, 메시지: ${e.message}")
            return handleUnauthorized(response, "만료된 토큰입니다.", requestURI)

        } catch (e: io.jsonwebtoken.security.SignatureException) {
            // ❌ 서명 위조 로그
            log.warn("⚠️ [401 인증 실패] JWT 토큰의 서명이 일치하지 않거나 변조되었습니다. -> 경로: $requestURI")
            return handleUnauthorized(response, "유효하지 않은 토큰 서명입니다.", requestURI)

        } catch (e: Exception) {
            // ❌ 기타 토큰 파싱 에러 로그
            log.error("❌ [401 인증 실패] JWT 토큰 처리 중 예외 발생 -> 경로: $requestURI, 에러: ${e.javaClass.simpleName}, 메시지: ${e.message}")
            return handleUnauthorized(response, "유효하지 않은 토큰입니다.", requestURI)
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