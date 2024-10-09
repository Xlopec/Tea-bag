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
            AppView(component: appDelegate.component)
        }
    }
}

class NewsReaderAppDelegate: NSObject, UIApplicationDelegate {
    
    let component: IosComponent
    
    override init() {
        component = IosComponent(systemDarkModeEnabled: UIViewController().isDarkMode)
    }
    
    deinit {
        component.destroy()
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
