package io.brieflyz.auth_service.adapter.`in`.web.dto.request

import jakarta.validation.constraints.NotBlank

data class SignInRequest(
    @field:NotBlank(message = "이메일을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String
)