package com.example.hellosecurity.security

import com.example.hellosecurity.entity.AdminUserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AdminUserDetails(
    val adminId: Long,
    val email: String,
    val role: AdminUserRole,
    // 💡 이번 API 요청 동안 재사용할 지점 ID 리스트를 필터에서 조회해 꽂아둠!
    val accessiblePartnerIds: List<String>,
) : UserDetails {
    /**
     * 스프링 시큐리티 인가 체계(hasRole 등)와 연동되는 권한 리스트를 반환해.
     * "ROLE_MANAGER", "ROLE_STAFF" 형태로 시큐리티 규격에 맞춰 변환해준다!
     */
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    // 💡 우리 시스템은 JWT 기반의 Stateless 환경이라 시큐리티 내장 패스워드 검증이 필요 없으므로 빈 값 처리!
    override fun getPassword(): String = ""

    // 💡 시큐리티가 유저를 식별할 때 쓰는 고유 Key로 우리는 email을 사용해.
    override fun getUsername(): String = email

    // --- 아래 계정 만료/잠금 관련 설정들은 전부 true(정상)로 하드코딩해두면 돼 ---

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}