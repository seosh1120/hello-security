package com.example.hellosecurity.controller

import com.example.hellosecurity.security.JwtProvider
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("local", "test") // 💡 운영 환경(prod)에 이 위험한 치트키가 올라가면 안 되니까 로컬/테스트 프로필만 켜지게 설정!
class TestTokenController(
    // 💡 승호가 프로젝트에서 실제 쓰고 있는 JWT 토큰 생성 컴포넌트 이름을 주입해줘!
    private val jwtProvider: JwtProvider
) {

    /**
     * 특정 이메일과 권한을 파라미터로 주면, 즉석에서 유효한 런타임 JWT 토큰을 찍어내는 API
     * 예: GET /api/test/token?email=manager@test.com&role=MANAGER
     */
    @GetMapping("/api/test/token")
    fun createTestToken(
        @RequestParam("email") email: String,
    ): ResponseEntity<TokenResponse> {

        // 승호가 기존에 만들어둔 JWT 생성 로직을 호출해줘.
        // 유틸 스펙에 따라 이메일만 받거나, Claims에 Role을 수동으로 넣어야 할 수도 있어!
        val token = jwtProvider.createToken(email)

        return ResponseEntity.ok(TokenResponse(token = token))
    }
}

// 응답 포맷용 심플 DTO
data class TokenResponse(val token: String)