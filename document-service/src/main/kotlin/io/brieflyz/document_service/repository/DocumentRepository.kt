package io.brieflyz.document_service.repository

import io.brieflyz.document_service.model.entity.Document
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface DocumentRepository : R2dbcRepository<Document, String>