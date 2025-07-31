package io.brieflyz.auth_service.model.security

import io.brieflyz.auth_service.model.entity.Member

class UserDetailsAdapter(
    member: Member,
    attributes: Map<String, Any>? = null
) : CustomUserDetails(
    member.email,
    member.roles.split("|"),
    attributes
)