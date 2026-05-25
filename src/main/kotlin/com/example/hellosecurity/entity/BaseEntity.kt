package com.example.hellosecurity.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class) // 💡 우리가 방금 찾은 소중한 리스너!
abstract class BaseEntity {

    // 💡 생성 시간 (최초 저장 시 자동 입력, 수정 불가능)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    var createdAt: LocalDateTime? = null

    // 💡 수정 시간 (데이터 바뀔 때마다 실시간 갱신)
    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    var lastModifiedAt: LocalDateTime? = null

    // 💡 생성자 이메일
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    var createdBy: String? = null

    // 💡 수정자 이메일
    @LastModifiedBy
    @Column(name = "last_modified_by")
    var lastModifiedBy: String? = null
}