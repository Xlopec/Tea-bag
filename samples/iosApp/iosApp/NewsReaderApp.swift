import UIKit
import SwiftUI
import SharedAppLib

@main
struct NewsReaderApp: App {
    
    @UIApplicationDelegateAdaptor(NewsReaderAppDelegate.self) private var appDelegate: NewsReaderAppDelegate

    init() {
        UITabBar.appearance().isTranslucent = false
    }

    var body: some Scene {
        WindowGroup {
            AppView(entryPoint: appDelegate.entryPoint)
        }
    }
}

class NewsReaderAppDelegate: NSObject, UIApplicationDelegate {
    
    let entryPoint: AppEntryPoint
    
    override init() {
        entryPoint = AppEntryPoint(systemDarkModeEnabled: UIViewController().isDarkMode)
    }
    
    deinit {
        entryPoint.destroy()
    }
}

private extension UIViewController {
    var isDarkMode: Bool {
        if #available(iOS 13.0, *) {
            return self.traitCollection.userInterfaceStyle == .dark
        }
        else {
            return false
        }
    }
}
