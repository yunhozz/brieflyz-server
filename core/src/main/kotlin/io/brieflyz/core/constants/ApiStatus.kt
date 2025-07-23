package io.brieflyz.core.constants

import io.brieflyz.core.constants.ApiStatusBuilder.badRequest
import io.brieflyz.core.constants.ApiStatusBuilder.conflict
import io.brieflyz.core.constants.ApiStatusBuilder.created
import io.brieflyz.core.constants.ApiStatusBuilder.forbidden
import io.brieflyz.core.constants.ApiStatusBuilder.internalServerError
import io.brieflyz.core.constants.ApiStatusBuilder.methodNotAllowed
import io.brieflyz.core.constants.ApiStatusBuilder.noContent
import io.brieflyz.core.constants.ApiStatusBuilder.notFound
import io.brieflyz.core.constants.ApiStatusBuilder.ok
import io.brieflyz.core.constants.ApiStatusBuilder.serviceUnavailable
import io.brieflyz.core.constants.ApiStatusBuilder.unauthorized

sealed interface ApiStatus {
    val statusCode: Int
    val message: String
}

enum class SuccessStatus(
    override val statusCode: Int,
    override val message: String
) : ApiStatus {
    // 200 OK
    USER_INFORMATION_READ_SUCCESS(200 ok "유저 정보 조회 성공"),
    SUBSCRIPTION_INFO_READ_SUCCESS(200 ok "구독 정보 조회 성공"),

    // 201 Created
    SIGN_UP_SUCCESS(201 created "회원가입 성공"),
    SIGN_IN_SUCCESS(201 created "로그인 성공"),
    TOKEN_REFRESH_SUCCESS(201 created "토큰 재발급 성공"),
    SUBSCRIBE_SUCCESS(201 created "구독 성공"),
    SUBSCRIPTION_UPDATE_SUCCESS(201 created "구독 정보 수정 성공"),

    // 204 No Content
    LOGOUT_SUCCESS(204 noContent "로그아웃 성공"),
    USER_WITHDRAW_SUCCESS(204 noContent "회원 탈퇴 성공"),
    SUBSCRIBE_CANCEL_SUCCESS(204 noContent "구독 취소 성공")
    ;

    constructor(status: ApiStatusBuilder.ApiStatus) : this(status.statusCode, status.message)
}

enum class ErrorStatus(
    override val statusCode: Int,
    override val message: String
) : ApiStatus {
    // 400 Bad Request
    BAD_REQUEST(400 badRequest "잘못된 요청입니다."),
    PASSWORD_NOT_MATCH(400 badRequest "비밀번호가 일치하지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(401 unauthorized "인증되지 않은 사용자입니다."),

    // 403 Forbidden
    FORBIDDEN(403 forbidden "권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(404 notFound "해당 유저를 찾을 수 없습니다."),
    USER_INFORMATION_NOT_FOUND(404 notFound "해당 유저에 대한 상세 정보를 조회할 수 없습니다."),
    REDIS_KEY_NOT_FOUND(404 notFound "Redis에 해당 key가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(404 notFound "로그인을 다시 진행해주세요."),
    SUBSCRIPTION_NOT_FOUND(404 notFound "해당 구독 정보를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(405 methodNotAllowed "허용되지 않은 메소드입니다."),

    // 409 Conflict
    USER_ALREADY_EXIST(409 conflict "유저 정보가 이미 존재합니다."),
    USER_REGISTERED_BY_SOCIAL_LOGIN(409 conflict "소셜 로그인 가입 계정입니다. 소셜 로그인을 이용해주세요."),
    ALREADY_UNLIMITED_PLAN_EXCEPTION(409 conflict "이미 무제한 구독 중입니다. 대신 결제 방식 업데이트를 진행해주세요."),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(500 internalServerError "서버 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요."),
    SERVICE_UNAVAILABLE(503 serviceUnavailable "서비스 접속이 원활하지 않습니다. 잠시 후 다시 시도해 주세요.")
    ;

    constructor(status: ApiStatusBuilder.ApiStatus) : this(status.statusCode, status.message)
}