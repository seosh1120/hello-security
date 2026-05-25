package com.example.hellosecurity.handler

import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // 💡 JSON 바디를 DTO 및 Enum으로 파싱하다가 에러 났을 때 정밀 타격하는 핸들러
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {

        // 403으로 뭉뚱그려지지 않게 명확하게 400 Bad Request를 뱉어준다!
        return ResponseEntity.badRequest().body(
            ErrorResponse(
                code = "BAD_REQUEST",
                message = "잘못된 요청 데이터 양식입니다. 상태 값(status)을 확인해 주세요."
            )
        )
    }
}

data class ErrorResponse(val code: String, val message: String)