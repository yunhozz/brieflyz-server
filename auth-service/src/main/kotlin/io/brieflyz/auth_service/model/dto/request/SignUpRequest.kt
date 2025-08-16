package io.brieflyz.auth_service.model.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignUpRequest(
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "정확한 형식의 이메일을 입력해주세요.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}\$",
        message = "비밀번호는 8자 이상이며, 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    val password: String,

    @field:NotBlank(message = "사용하실 별명을 입력해주세요.")
    val nickname: String
)