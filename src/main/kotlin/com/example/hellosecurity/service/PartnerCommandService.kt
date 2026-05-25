package com.example.hellosecurity.service

import com.example.hellosecurity.controller.PartnerResponse
import com.example.hellosecurity.dto.PartnerStatusUpdateRequest
import com.example.hellosecurity.entity.Partner
import com.example.hellosecurity.repository.PartnerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartnerCommandService(
    private val partnerRepository: PartnerRepository,
) {
    // 💡 데이터 등록 예시
    @Transactional
    fun createPartner(id: String, name: String, location: String): PartnerResponse {
        val newPartner = Partner(id = id, name = name, location = location)

        // 🌟 저장하는 순간 JPA 리스너가 가로채서 createdBy에 'CurrentAdmin.email'을 슥 채워넣어!
        val saved = partnerRepository.save(newPartner)

        return PartnerResponse(saved.id, saved.name, saved.location)
    }

    // 💡 데이터 수정 예시
    @Transactional
    fun updatePartner(id: String, newName: String): String {
        val partner = partnerRepository.findById(id).orElseThrow { Exception("Not Found") }
        partner.name = newName

        // 🌟 트랜잭션이 끝나면서 더티 체킹(변경 감지)으로 flush될 때
        // lastModifiedBy 컬럼에 지금 수정한 사람 이메일이 자동으로 업데이트돼!
        return "수정 완료"
    }

    @Transactional
    fun updateStatus(id: String, request: PartnerStatusUpdateRequest) {
        // [Data Loading] 1단계: 엔티티 조회 (H2든 MySQL이든 영속 상태로 스냅샷 확보)
        val partner = partnerRepository.findByIdOrNull(id)
            ?: throw NoSuchElementException("해당 파트너를 찾을 수 없습니다. ID: $id")

        // 2단계: 핵심 상태 전이(State Transition) 수행
        // 엔티티 내부에서 상태 변경 시 필요한 비즈니스 검증 규칙이 있다면 여기서 수행됨
        partner.changeStatus(request.status)

        // Transaction 종료 시점에 JPA가 변경 사항을 감지하여 자동으로 깔끔하게 SQL Update를 날려줌!
    }
}