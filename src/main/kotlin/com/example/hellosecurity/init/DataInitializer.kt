package com.example.hellosecurity.init

import com.example.hellosecurity.entity.AdminPartnerMapping
import com.example.hellosecurity.entity.AdminUser
import com.example.hellosecurity.entity.AdminUserRole
import com.example.hellosecurity.entity.Partner
import com.example.hellosecurity.repository.AdminPartnerMappingRepository
import com.example.hellosecurity.repository.AdminUserRepository
import com.example.hellosecurity.repository.PartnerRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DataInitializer(
    private val partnerRepository: PartnerRepository,
    private val adminUserRepository: AdminUserRepository,
    private val adminPartnerMappingRepository: AdminPartnerMappingRepository
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments?) {
        // 1. 기초 파트너 데이터 무조건 생성
        val gangnam = partnerRepository.save(Partner(id = "partner-1", name = "강남점", location = "서울 강남구"))
        val seongbok = partnerRepository.save(Partner(id = "partner-2", name = "성복점", location = "경기 용인시"))
        val hongdae = partnerRepository.save(Partner(id = "partner-3", name = "홍대점", location = "서울 마포구"))

        // 2. 최고 관리자 계정 무조건 생성
        val manager = adminUserRepository.save(
            AdminUser(email = "manager@test.com", role = AdminUserRole.MANAGER)
        )

        // 3. 멀티 지점 관리 스태프 계정 무조건 생성 (강남점 + 성복점 관리)
        val multiStaff = adminUserRepository.save(
            AdminUser(email = "multi_staff@test.com", role = AdminUserRole.STAFF)
        )

        // 💡 DB가 매번 리셋되므로 exists 체크 없이 매핑 정보 바로 삽입!
        adminPartnerMappingRepository.save(AdminPartnerMapping(adminUser = multiStaff, partnerId = gangnam.id))
        adminPartnerMappingRepository.save(AdminPartnerMapping(adminUser = multiStaff, partnerId = seongbok.id))

        // 4. 단일 지점 관리 스태프 계정 무조건 생성 (홍대점만 관리)
        val hongdaeStaff = adminUserRepository.save(
            AdminUser(email = "hongdae_staff@test.com", role = AdminUserRole.STAFF)
        )

        // 💡 홍대 스태프 매핑 정보도 바로 삽입!
        adminPartnerMappingRepository.save(AdminPartnerMapping(adminUser = hongdaeStaff, partnerId = hongdae.id))
    }
}