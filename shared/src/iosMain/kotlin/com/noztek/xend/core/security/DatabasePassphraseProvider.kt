package com.noztek.xend.core.security

import cnames.structs.__CFData
import cnames.structs.__CFDictionary
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.arc4random_buf
import platform.posix.memcpy

private const val KEYCHAIN_SERVICE = "com.noztek.xend.db"
private const val KEYCHAIN_ACCOUNT = "sqlcipher_passphrase"
private const val PASSPHRASE_BYTES = 32

class DatabasePassphraseProvider {
    fun getPassphrase(): String {
        readPassphrase()?.let { return it }

        val generated = generatePassphrase()
        savePassphrase(generated)
        return generated
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun generatePassphrase(): String {
        val bytes = ByteArray(PASSPHRASE_BYTES)
        bytes.usePinned { pinned ->
            arc4random_buf(pinned.addressOf(0), bytes.size.toULong())
        }
        return bytes.joinToString(separator = "") { byte ->
            byte.toUByte().toString(radix = 16).padStart(2, '0')
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun savePassphrase(passphrase: String) {
        deletePassphrase()

        val query = createKeychainQuery(
            returnData = false,
            data = passphrase.encodeToByteArray(),
        )
        try {
            val status = SecItemAdd(query, null)
            check(status == 0) { "Failed to store SQLCipher passphrase in Keychain: $status" }
        } finally {
            CFRelease(query)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readPassphrase(): String? = memScoped {
        val query = createKeychainQuery(returnData = true)
        try {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)

            when (status) {
                0 -> {
                    result.value!!.reinterpret<__CFData>().toByteArray().decodeToString()
                }

                errSecItemNotFound -> null
                else -> error("Failed to read SQLCipher passphrase from Keychain: $status")
            }
        } finally {
            CFRelease(query)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deletePassphrase() {
        val query = createKeychainQuery(returnData = false)
        try {
            val status = SecItemDelete(query)
            check(status == 0 || status == errSecItemNotFound) {
                "Failed to clear SQLCipher passphrase in Keychain: $status"
            }
        } finally {
            CFRelease(query)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createKeychainQuery(
        returnData: Boolean,
        data: ByteArray? = null,
    ): kotlinx.cinterop.CPointer<__CFDictionary> {
        val query = CFDictionaryCreateMutable(null, 0, null, null)
            ?: error("Failed to allocate Keychain query dictionary")

        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, cfString(KEYCHAIN_SERVICE))
        CFDictionaryAddValue(query, kSecAttrAccount, cfString(KEYCHAIN_ACCOUNT))
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)

        if (returnData) {
            CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)
        }

        if (data != null) {
            CFDictionaryAddValue(query, kSecValueData, data.toCFData())
        }

        return query.reinterpret()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun cfString(value: String) = CFStringCreateWithCString(
    kCFAllocatorDefault,
    value,
    kCFStringEncodingUTF8,
)

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toCFData() = usePinned { pinned ->
    CFDataCreate(kCFAllocatorDefault, pinned.addressOf(0).reinterpret(), size.convert())
}

@OptIn(ExperimentalForeignApi::class)
private fun kotlinx.cinterop.CPointer<__CFData>.toByteArray(): ByteArray {
    val lengthInt = CFDataGetLength(this).toInt()
    val result = ByteArray(lengthInt)
    if (lengthInt > 0) {
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), CFDataGetBytePtr(this), lengthInt.convert())
        }
    }
    return result
}
