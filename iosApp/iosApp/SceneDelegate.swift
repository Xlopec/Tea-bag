import UIKit
import SwiftUI
//import tea_core

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        
        /* TestFunctionsKt.foo1()
        TestFunctionsKt.foo2 { (xy) -> String in
            xy.stringValue
        }
        
        TestFunctionsKt.foo3 { (a: Any?) -> String in
            a.debugDescription
        }
        
        TestFunctionsKt.fooTakingSuspendingFun(f: SuspendingImpl())
        
        TestFunctionsKt.suspendingFooTakingSuspendingFun(f: SuspendingImpl()) { r, e in
            if e != nil {
                print("error \(e)")
            } else {
                print("success \(r)")
            }
        }
        
        
        let env = Env<AnyObject, NSString, NSString>(
            initializer: SuspendingImpl0(),
            resolver: SuspendingImpl(), updater: { a, b in
            KotlinPair(first: a as? NSString, second: [])
            }, scope: TestFunctionsKt.testScope(), io: TestFunctionsKt.io(), computation: TestFunctionsKt.io(), shareOptions: TestFunctionsKt.shareStateWhileSubscribed())
        
        print("env is \(env)")
        
        let component = ComponentKt.Component(
            initializer: SuspendingImpl0(),
            resolver: SuspendingImpl(), updater: { a, b in
            KotlinPair(first: a as? NSString, second: [])
            }, scope: TestFunctionsKt.testScope(), io: TestFunctionsKt.io(), computation: TestFunctionsKt.io(), shareOptions: TestFunctionsKt.shareStateWhileSubscribed()
        )
        
        component(TestFunctionsKt.someFlow())
            .collect(collector: CollectorImpl()) { r, e in
                print("Collected r=\(r), e=\(e)")
            }*/
        /*TestFunctionsKt.suspendingFooTakingSuspendingFun(
            f: {KotlinSuspendFunction1},
            
            { (_: KotlinUnit?, e: Error?) in
            
            if (e != nil) {
                log("pizda \(e)")
            }
        }*/
        
        // Use this method to optionally configure and attach the UIWindow `window` to the provided UIWindowScene `scene`.
        // If using a storyboard, the `window` property will automatically be initialized and attached to the scene.
        // This delegate does not imply the connecting scene or session are new (see `application:configurationForConnectingSceneSession` instead).

        // Create the SwiftUI view that provides the window contents.
        //let contentView = MyContentView(viewModel: .init(sdk: sdk))
        
        
        
        /*let som1 = { (e: Error?) -> NSString in ""
            
        }
        
        */
        let contentView = ArticlesView(articles: articles)//RowItem(article: article)

        // Use a UIHostingController as window root view controller.
        if let windowScene = scene as? UIWindowScene {
            let window = UIWindow(windowScene: windowScene)
            window.rootViewController = UIHostingController(rootView: contentView)
            self.window = window
            window.makeKeyAndVisible()
        }
    }

    func sceneDidDisconnect(_ scene: UIScene) {
        // Called as the scene is being released by the system.
        // This occurs shortly after the scene enters the background, or when its session is discarded.
        // Release any resources associated with this scene that can be re-created the next time the scene connects.
        // The scene may re-connect later, as its session was not neccessarily discarded (see `application:didDiscardSceneSessions` instead).
    }

    func sceneDidBecomeActive(_ scene: UIScene) {
        // Called when the scene has moved from an inactive state to an active state.
        // Use this method to restart any tasks that were paused (or not yet started) when the scene was inactive.
    }

    func sceneWillResignActive(_ scene: UIScene) {
        // Called when the scene will move from an active state to an inactive state.
        // This may occur due to temporary interruptions (ex. an incoming phone call).
    }

    func sceneWillEnterForeground(_ scene: UIScene) {
        // Called as the scene transitions from the background to the foreground.
        // Use this method to undo the changes made on entering the background.
    }

    func sceneDidEnterBackground(_ scene: UIScene) {
        // Called as the scene transitions from the foreground to the background.
        // Use this method to save data, release shared resources, and store enough scene-specific state information
        // to restore the scene back to its current state.
    }


}

