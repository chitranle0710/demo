package vn.hiep.demobilling.utils

import android.text.TextUtils
import android.util.Base64
import java.io.IOException
import java.lang.RuntimeException
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec


object Security {

    private const val KEY_FACTORY_ALGORITHM = "RSA"
    private const val SIGNATURE_ALGORITHM = "SHA1withRSA"

    /**
     * Verifies that the data was signed with the given signature, and returns the verified
     * purchase.
     * @param base64PublicKey the base64-encoded public key to use for verifying.
     * @param signedData the signed JSON string (signed, not encrypted)
     * @param signature the signature for the data, signed with the private key
     * @throws IOException if encoding algorithm is not supported or key specification
     * is invalid
     */

    fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlsSkPY9i/vOWJQsnB3" +
                    "Rg9UNvjKvNQgD7m+OdarHW4d0s/Jvzh3KUoQw/P6L/QHXCWwX0ssefr//nvjeDyDqlbC" +
                    "9k60lTzZsJmI30XiTWJUdD4nQT00KQBvpDKDcJq7zPudfjBCG1r7HcBrNxHR3QeT" +
                    "kVx/2ch5hXbbjst+4NA+MKpIWisGEIcUimHB5De13bFHglb1HJZhY75N89q/tZFopEaxzVIZNxi" +
                    "prkt/rWbagqdqwOE4g9LNEqW+bh1Cd0eYypOl1/TPSt+DstZppVDzZnsz6a5r6UdqqBZoupbG2KG0rJ5" +
                    "Xff6z2Hbm70h0cfGAtCUPVnBv+GC/qeI53J0QIDAQAB"
            verifyPurchase(base64key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }

    @Throws(IOException::class)
    fun verifyPurchase(
        base64PublicKey: String?, signedData: String,
        signature: String?
    ): Boolean {
        if (TextUtils.isEmpty(signedData) || TextUtils.isEmpty(base64PublicKey)
            || TextUtils.isEmpty(signature)
        ) {
            //Purchase verification failed: missing data
            return false
        }
        val key: PublicKey = generatePublicKey(base64PublicKey)
        return verify(key, signedData, signature)
    }

    /**
     * Generates a PublicKey instance from a string containing the Base64-encoded public key.
     *
     * @param encodedPublicKey Base64-encoded public key
     * @throws IOException if encoding algorithm is not supported or key specification
     * is invalid
     */
    @Throws(IOException::class)
    fun generatePublicKey(encodedPublicKey: String?): PublicKey {
        return try {
            val decodedKey: ByteArray = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory: KeyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            throw RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            val msg = "Invalid key specification: $e"
            throw IOException(msg)
        }
    }

    /**
     * Verifies that the signature from the server matches the computed signature on the data.
     * Returns true if the data is correctly signed.
     *
     * @param publicKey public key associated with the developer account
     * @param signedData signed data from server
     * @param signature server signature
     * @return true if the data and signature match
     */
    private fun verify(publicKey: PublicKey?, signedData: String, signature: String?): Boolean {
        val signatureBytes: ByteArray = try {
            Base64.decode(signature, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            //Base64 decoding failed
            return false
        }
        try {
            val signatureAlgorithm: Signature = Signature.getInstance(SIGNATURE_ALGORITHM)
            signatureAlgorithm.initVerify(publicKey)
            signatureAlgorithm.update(signedData.toByteArray())
            return signatureAlgorithm.verify(signatureBytes)
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            //Invalid key specification
        } catch (e: SignatureException) {
            //Signature exception
        }
        return false
    }
}