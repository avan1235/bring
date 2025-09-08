import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {

    let initListId: String?

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(initListId: initListId)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

struct ContentView: View {
    @State private var viewKey = UUID()
    @State private var initListId: String?

    var body: some View {
        ComposeView(initListId: initListId)
        .id(viewKey)
        .ignoresSafeArea()
        .onAppear {
            initListId = nil
        }
        .onOpenURL { incomingURL in
            handleIncomingURL(incomingURL)
        }
    }

    private func handleIncomingURL(_ url: URL) {
        guard url.scheme == "bring" else {
            return
        }
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else {
            return
        }
        guard let listId = components.host, isValidUUIDv4(listId) else {
            return
        }
        initListId = listId
        viewKey = UUID()
    }
}

private let pattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-4[0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"

private func isValidUUIDv4(_ string: String) -> Bool {
    return string.range(of: pattern, options: .regularExpression) != nil
}