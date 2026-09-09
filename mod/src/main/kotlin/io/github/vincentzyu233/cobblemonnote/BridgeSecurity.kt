package io.github.vincentzyu233.cobblemonnote

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object BridgeSecurity {
    fun sign(secret: String, timestamp: String, nonce: String, method: String, path: String, body: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(listOf(timestamp, nonce, method, path, body).joinToString("\n").toByteArray()).joinToString("") { "%02x".format(it) }
    }
    fun matches(expected: String, actual: String?) = actual != null && MessageDigest.isEqual(expected.toByteArray(), actual.toByteArray())
}
