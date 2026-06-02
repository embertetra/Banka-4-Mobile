package rs.raf.banka4mobile.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "banka4mobile_session_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE_BYTES = 12
        private const val AUTH_TAG_LENGTH_BITS = 128
        private const val ENCRYPTED_PREFIX = "enc:"
        private const val LOG_TAG = "CryptoManager"
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        val payload = cipher.iv + encryptedBytes
        val base64 = Base64.encodeToString(payload, Base64.NO_WRAP)

        return ENCRYPTED_PREFIX + base64
    }

    fun decrypt(cipherText: String): String {
        if (!cipherText.startsWith(ENCRYPTED_PREFIX)) return cipherText

        val payload = Base64.decode(cipherText.removePrefix(ENCRYPTED_PREFIX), Base64.NO_WRAP)
        if (payload.size <= IV_SIZE_BYTES) {
            throw IllegalArgumentException("Encrypted payload is invalid.")
        }

        val iv = payload.copyOfRange(0, IV_SIZE_BYTES)
        val encryptedBytes = payload.copyOfRange(IV_SIZE_BYTES, payload.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(AUTH_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    fun decryptOrNull(cipherText: String?): String? {
        if (cipherText == null) return null
        return runCatching { decrypt(cipherText) }
            .onFailure { Timber.tag(LOG_TAG).w(it, "Failed to decrypt a stored value.") }
            .getOrNull()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }
}

