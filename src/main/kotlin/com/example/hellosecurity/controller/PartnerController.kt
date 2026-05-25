package com.example.hellosecurity.controller

import com.example.hellosecurity.dto.PartnerCreateRequest
import com.example.hellosecurity.dto.PartnerStatusUpdateRequest
import com.example.hellosecurity.service.PartnerCommandService
import com.example.hellosecurity.service.PartnerQueryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/partners")
class PartnerController(
    private val partnerQueryService: PartnerQueryService,
    private val partnerCommandService: PartnerCommandService,

) {
    /**
     * 💡 2. 파트너 신규 생성 API
     * 최상위 관리자(MANAGER)만 새로운 파트너사를 등록할 수 있도록 격리했어.
     */
    @PostMapping
    fun createPartner(
        @RequestBody request: PartnerCreateRequest
    ): PartnerResponse {
        return partnerCommandService.createPartner(request.id, request.name, request.location)
    }

    /**
     * 💡 파트너 페이징 조회 API
     * @PageableDefault를 쓰면 프론트에서 값을 안 보냈을 때 기본 페이징 스펙(0페이지, 10개씩)이 작동해.
     */
    @GetMapping
    fun getAllPartners(
        @PageableDefault(size = 10, page = 0) pageable: Pageable
    ): Page<PartnerResponse> {
        return partnerQueryService.getPartnersPage(pageable)
    }

    @PostMapping("/{id}")
    fun update(
        @PathVariable("id") id: String, @RequestBody request: PartnerUpdateRequest
    ): String {
        partnerCommandService.updatePartner(id, request.name)
        return "Partner $id 가 성공적으로 업데이트되었습니다."
    }

    @PostMapping("/{id}/status")
    fun updatePartnerStatus(
        @PathVariable("id") id: String,
        @RequestBody request: PartnerStatusUpdateRequest
    ): ResponseEntity<Unit> {
        partnerCommandService.updateStatus(id, request)
        return ResponseEntity.noContent().build()
    }
}

data class PartnerResponse(val id: String, val name: String, val location: String)
data class PartnerUpdateRequest(val name: String)