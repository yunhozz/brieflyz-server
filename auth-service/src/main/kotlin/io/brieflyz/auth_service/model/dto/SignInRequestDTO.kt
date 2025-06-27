package io.brieflyz.auth_service.model.dto

import jakarta.validation.constraints.NotBlank

data class SignInRequestDTO(
    @field:NotBlank(message = "이메일을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    val password: String
)