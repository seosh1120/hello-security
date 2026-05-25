package com.example.hellosecurity.config

import com.example.hellosecurity.security.JwtAuthenticationFilter
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val applicationContext: ApplicationContext
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 💡 1. H2 콘솔 화면이 정상적으로 나오도록 X-Frame-Options 해제 (같은 도메인 허용)
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }

            .authorizeHttpRequests { auth ->
                // 💡 2. H2 콘솔 관련 모든 요청은 토큰 인증 없이 통과(permitAll)되도록 설정
                auth.requestMatchers("/h2-console/**").permitAll()
                auth.requestMatchers("/api/test/token").permitAll()

                auth// 💡 [우선순위 1] 상태 변경 API: 오직 최고 관리자(MANAGER)만 통과 가능!
                    .requestMatchers(HttpMethod.POST, "/api/partners/{id}/status")
                    .hasRole("MANAGER")

                    // 💡 [우선순위 2] 일반 수정 API: 매니저 프리패스 / 스태프는 자기 지점 검증 수행
                    .requestMatchers(HttpMethod.POST, "/api/partners/{partnerId}").check(
                        "hasRole('MANAGER') or (hasRole('STAFF') and @partnerChecker.hasAccess(authentication, #partnerId))",
                        applicationContext
                    )

                    // 💡 [우선순위 3] 나머지 파트너 API 통합 관문
                    .requestMatchers("/api/partners/**")
                    .hasAnyRole("MANAGER", "STAFF")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

// 💡 승호가 구현한 깔끔한 표현식 확장 함수 구조 그대로 유지!
fun AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl.check(
    expression: String,
    applicationContext: ApplicationContext
) = this.access(
    WebExpressionAuthorizationManager(expression).apply {
        // 🛡️ 시큐리티 6.x 공식 정석 핸들러 세팅
        val handler = DefaultHttpSecurityExpressionHandler()
        handler.setApplicationContext(applicationContext)

        // 이 팩토리를 거쳐야 내부적으로 #pathVariables 맵이 null 없이 생성돼!
        this.setExpressionHandler(handler)
    }
)