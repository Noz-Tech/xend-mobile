package com.noztek.xend.core.notify

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebasePushTokenProvider : PushTokenProvider {
    override suspend fun getTokenOrNull(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener { continuation.resume(null) }
            .addOnCanceledListener { continuation.resume(null) }
    }
}
