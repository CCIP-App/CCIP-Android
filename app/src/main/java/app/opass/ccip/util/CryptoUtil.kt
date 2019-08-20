package app.opass.ccip.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object CryptoUtil {
    fun toPublicToken(privateToken: String?): String? {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-1")
            val data = messageDigest.digest(privateToken!!.toByteArray())
            val buffer = StringBuilder()

            for (b in data) {
                buffer.append("%02x".format(b))
            }

            return buffer.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return null
    }
}
