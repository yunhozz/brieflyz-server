package io.brieflyz.auth_service.infra.security.user

import io.brieflyz.auth_service.model.entity.Member

class UserDetailsAdapter(
    member: Member,
    attributes: Map<String, Any>? = null
) : CustomUserDetails(
    member.email,
    member.password,
    member.roles,
    attributes
)