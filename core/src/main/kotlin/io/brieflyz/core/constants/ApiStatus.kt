package io.brieflyz.core.constants

data class ApiResult(val statusCode: Int, val message: String)

infix fun Int.ok(message: String) = ApiResult(this, message)
infix fun Int.created(message: String) = ApiResult(this, message)
infix fun Int.noContent(message: String) = ApiResult(this, message)
infix fun Int.badRequest(message: String) = ApiResult(this, message)
infix fun Int.unauthorized(message: String) = ApiResult(this, message)
infix fun Int.forbidden(message: String) = ApiResult(this, message)
infix fun Int.notFound(message: String) = ApiResult(this, message)
infix fun Int.methodNotAllowed(message: String) = ApiResult(this, message)
infix fun Int.conflict(message: String) = ApiResult(this, message)
infix fun Int.internalServerError(message: String) = ApiResult(this, message)
infix fun Int.serviceUnavailable(message: String) = ApiResult(this, message)

sealed interface ApiStatus {
    val statusCode: Int
}

enum class SuccessStatus(
    override val statusCode: Int,
    val message: String
) : ApiStatus {
    // 200 OK
    USER_INFORMATION_READ_SUCCESS(200 ok "유저 정보 조회에 성공하였습니다."),
    SUBSCRIPTION_INFO_READ_SUCCESS(200 ok "구독 정보 조회에 성공하였습니다."),
    USER_SIGNUP_VERIFY_SUCCESS(200 ok "회원가입 인증 성공하였습니다. 다시 로그인을 진행해주세요."),
    DOCUMENT_LIST_READ_SUCCESS(200 ok "문서 리스트 조회에 성공하였습니다."),

    // 201 Created
    SIGN_UP_SUCCESS(201 created "회원가입에 성공하였습니다. 이메일 인증을 진행해주세요."),
    SIGN_IN_SUCCESS(201 created "로그인 성공하였습니다."),
    TOKEN_REFRESH_SUCCESS(201 created "토큰 재발급에 성공하였습니다."),
    SUBSCRIBE_SUCCESS(201 created "구독에 성공하였습니다."),
    DOCUMENT_CREATION_REQUEST_SUCCESS(201 created "문서 생성 요청에 성공하였습니다."),

    // 204 No Content
    LOGOUT_SUCCESS(204 noContent "로그아웃 되었습니다."),
    USER_WITHDRAW_SUCCESS(204 noContent "회원 탈퇴 처리되었습니다."),
    SUBSCRIBE_CANCEL_SUCCESS(204 noContent "구독 취소 처리되었습니다."),
    SUBSCRIBE_DELETE_SUCCESS(204 noContent "구독이 삭제되었습니다.")
    ;

    constructor(result: ApiResult) : this(result.statusCode, result.message)
}

enum class ErrorStatus(
    override val statusCode: Int,
    val message: String
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
    REFRESH_TOKEN_NOT_FOUND(404 notFound "로그인을 다시 진행해주세요."),
    VERIFY_TOKEN_NOT_FOUND(404 notFound "인증 토큰이 만료되었습니다."),
    SUBSCRIPTION_NOT_FOUND(404 notFound "해당 구독 정보를 찾을 수 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(405 methodNotAllowed "허용되지 않은 메소드입니다."),

    // 409 Conflict
    USER_ALREADY_EXIST(409 conflict "유저 정보가 이미 존재합니다."),
    USER_REGISTERED_BY_SOCIAL_LOGIN(409 conflict "소셜 로그인 가입 계정입니다. 소셜 로그인을 이용해주세요."),
    ALREADY_HAVE_SUBSCRIPTION(409 conflict "이미 구독 상태입니다."),
    ALREADY_HAVE_UNLIMITED_SUBSCRIPTION(409 conflict "이미 무제한 구독 중입니다."),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(500 internalServerError "서버 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요."),
    SERVICE_UNAVAILABLE(503 serviceUnavailable "서비스 접속이 원활하지 않습니다. 잠시 후 다시 시도해 주세요.")
    ;

    constructor(result: ApiResult) : this(result.statusCode, result.message)
}