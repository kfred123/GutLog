import SwiftUI
import UIKit
import GutLogShared

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct GutLogIOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
