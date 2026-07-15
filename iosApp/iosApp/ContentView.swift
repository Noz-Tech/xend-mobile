import UIKit
import SwiftUI
import LibSignalClient
import Shared

private let apiBaseUrl = "https://api.xend.space"

final class IOSSignalBootstrapProvider: SignalBootstrapProvider {
    func create() -> SignalBootstrap {
        let identityKeyPair = IdentityKeyPair.generate()
        let registrationId = Int32(UInt32.random(in: 1...0x3FFF))

        return SignalBootstrap(
            registrationId: registrationId,
            identityKeyPublicBase64: identityKeyPair.publicKey.serialize().base64EncodedString(),
            identityKeyPrivateBase64: identityKeyPair.privateKey.serialize().base64EncodedString()
        )
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Self.Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(
            appConfig: AppConfig(apiBaseUrl: apiBaseUrl),
            signalBootstrapProvider: IOSSignalBootstrapProvider()
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
