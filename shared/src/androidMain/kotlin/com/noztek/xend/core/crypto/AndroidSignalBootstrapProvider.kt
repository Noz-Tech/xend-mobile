package com.noztek.xend.core.crypto

import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.util.KeyHelper

class AndroidSignalBootstrapProvider : SignalBootstrapProvider {
    override fun create(): SignalBootstrap {
        val identityKeyPair = IdentityKeyPair.generate()
        val identityKeyPublic = identityKeyPair.publicKey.serialize()
        val identityKeyPrivate = identityKeyPair.privateKey.serialize()

        return SignalBootstrap(
            registrationId = KeyHelper.generateRegistrationId(false),
            identityKeyPublicBase64 = encodeBase64(identityKeyPublic),
            identityKeyPrivateBase64 = encodeBase64(identityKeyPrivate),
        )
    }
}
