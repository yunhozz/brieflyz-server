package io.brieflyz.core.enums

import io.brieflyz.core.constants.ApiStatus

sealed interface ApiResponseCode

enum class SuccessCode(
    val status: Int,
    val message: String
) : ApiResponseCode {
    // 200 OK
    USER_INFORMATION_READ_SUCCESS(ApiStatus.OK, "유저 정보 조회 성공"),

    // 201 Created
    SIGN_UP_SUCCESS(ApiStatus.CREATED, "회원가입 성공"),
    SIGN_IN_SUCCESS(ApiStatus.CREATED, "로그인 성공"),
    TOKEN_REFRESH_SUCCESS(ApiStatus.CREATED, "토큰 재발급 성공"),

    // 204 No Content
    LOGOUT_SUCCESS(ApiStatus.NO_CONTENT, "로그아웃 성공"),
    USER_WITHDRAW_SUCCESS(ApiStatus.NO_CONTENT, "회원 탈퇴 성공")
}

enum class ErrorCode(
    val status: Int,
    val message: String
) : ApiResponseCode {
    // 400 Bad Request
    BAD_REQUEST(ApiStatus.BAD_REQUEST, "잘못된 요청입니다."),
    PASSWORD_NOT_MATCH(ApiStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(ApiStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    // 403 Forbidden
    FORBIDDEN(ApiStatus.FORBIDDEN, "권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(ApiStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    USER_INFORMATION_NOT_FOUND(ApiStatus.NOT_FOUND, "해당 유저에 대한 상세 정보를 조회할 수 없습니다."),
    REDIS_KEY_NOT_FOUND(ApiStatus.NOT_FOUND, "Redis에 해당 key가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(ApiStatus.NOT_FOUND, "로그인을 다시 진행해주세요."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(ApiStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메소드입니다."),

    // 409 Conflict
    USER_ALREADY_EXIST(ApiStatus.CONFLICT, "유저 정보가 이미 존재합니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(ApiStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생하였습니다.")
}