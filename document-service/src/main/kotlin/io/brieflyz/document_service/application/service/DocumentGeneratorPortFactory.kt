package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.port.out.DocumentGeneratorPort
import org.springframework.stereotype.Component

@Component
class DocumentGeneratorPortFactory(ports: Set<DocumentGeneratorPort>) {

    private val portMap: Map<DocumentType, DocumentGeneratorPort> = ports.associateBy { it.getDocumentType() }

    fun createByDocumentType(documentType: DocumentType): DocumentGeneratorPort = portMap[documentType]
        ?: throw IllegalArgumentException("해당 문서 생성 서비스가 존재하지 않습니다.")
}