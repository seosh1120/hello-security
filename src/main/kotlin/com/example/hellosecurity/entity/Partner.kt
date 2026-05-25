package com.example.hellosecurity.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "partners")
class Partner(
    @Id
    @Column(name = "partner_id")
    val id: String, // 예: "partner-123"

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    val location: String, // 지점 주소나 위치

    @Enumerated(EnumType.STRING)
    var status: PartnerStatus = PartnerStatus.INACTIVE
) : BaseEntity() {
    fun changeStatus(newStatus: PartnerStatus) {
        this.status = newStatus
    }
}