package com.example.hellosecurity.service

import com.example.hellosecurity.controller.PartnerResponse
import com.example.hellosecurity.repository.PartnerRepository
import com.example.hellosecurity.util.CurrentAdmin
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PartnerQueryService(
    private val partnerRepository: PartnerRepository
) {

    @Transactional(readOnly = true)
    fun getPartnersPage(pageable: Pageable): Page<PartnerResponse> {
        // MANAGER 권한이 있는지 판단
        // 2. 권한에 따라 쿼리 분기 처리 (DB 레벨에서 페이징 처리되므로 효율적!)
        val partnerPage = if (CurrentAdmin.isManager()) {
            partnerRepository.findAll(pageable)
        } else {
            val email = CurrentAdmin.getCurrentEmail()
            partnerRepository.findAllByAdminEmail(email, pageable)
        }

        // 3. Page 엔티티 구조를 그대로 유지하면서 응답 DTO로 변환 (.map 사용)
        return partnerPage.map {
            PartnerResponse(id = it.id, name = it.name, location = it.location)
        }
    }
}