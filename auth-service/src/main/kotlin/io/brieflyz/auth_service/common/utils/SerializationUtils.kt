package io.brieflyz.auth_service.common.utils

import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.util.Base64

object SerializationUtils {
    fun serialize(obj: Any): String {
        val bytes = SerializationUtils.serialize(obj)
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    fun <T> deserialize(str: String, clazz: Class<T>): T {
        val bytes = Base64.getUrlDecoder().decode(str)
        ByteArrayInputStream(bytes).use { bais ->
            ObjectInputStream(bais).use { ois ->
                return clazz.cast(ois.readObject())
            }
        }
    }
}