package com.example.hellosecurity.config

import com.example.hellosecurity.util.CurrentAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.Optional

@Configuration
@EnableJpaAuditing // 💡 프로젝트 전체에 JPA Auditing 기능을 켜는 핵심 스위치!
class JpaAuditingConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AdminAuditorAware()
    }
}

/**
 * 💡 스프링 시큐리티 세션에서 현재 작업자의 이메일을 조회하여 JPA에 던져주는 배달원 역할
 */
class AdminAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val email = CurrentAdmin.getCurrentEmail()

        // 로그인하지 않은 상태(비어있는 상태)면 공백이나 "SYSTEM"으로 대체
        if (email.isBlank()) {
            return Optional.of("SYSTEM")
        }

        return Optional.of(email)
    }
}