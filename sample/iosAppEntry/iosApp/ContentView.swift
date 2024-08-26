import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    var delegate: ApplicationDelegate
    
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(
            navigationRootData: delegate.rootHolder.navigationRootData
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var delegate: ApplicationDelegate
    
    var body: some View {
        ComposeView(delegate: delegate)
            .ignoresSafeArea()
    }
}

class ApplicationDelegate: NSObject, UIApplicationDelegate {
    let rootHolder = RootHolder()
}

class RootHolder: ObservableObject {
    let appLifecycle: LifecycleRegistry
    let navigationRootData: NavigationRootData
    
    init() {
        appLifecycle = LifecycleRegistryKt.LifecycleRegistry()
        
        navigationRootData = NavigationRootDataKt.defaultNavigationRootData(lifecycleRegistry: appLifecycle)
        
        LifecycleRegistryExtKt.create(appLifecycle)
    }
    
    deinit {
        LifecycleRegistryExtKt.destroy(appLifecycle)
    }
}



