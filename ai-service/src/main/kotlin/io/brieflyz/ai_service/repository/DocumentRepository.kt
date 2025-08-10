package io.brieflyz.ai_service.repository

import io.brieflyz.ai_service.model.entity.Document
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface DocumentRepository : R2dbcRepository<Document, String>