//
//  AppView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 11.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib

// todo add custom App protocol implementation

class ObservableAppComponent : ObservableObject {
    
    @Published public var appState: AppState? = nil
    
    private var cancellation: Cancellation?
    private let component: IosComponent
    
    init() {
        component = IosComponent()
        
        cancellation = component.render { state in
            self.appState = state
        }
    }
    
    func dispatch(_ message: Message) {
        component.dispatch(message: message)
    }
    
    deinit {
        cancellation?.cancel()
    }
    
}

typealias MessageHandler = (Message) -> Void

struct AppView: View {
    
    @ObservedObject private(set) var appComponent: ObservableAppComponent
    
    let handler: MessageHandler
    
    init(appComponent: ObservableAppComponent) {
        self.appComponent = appComponent
        self.handler = appComponent.dispatch
    }
    
    var body: some View {
        ZStack {
            if let appState = appComponent.appState {
                
                let screen = appState.screen
                
                switch screen {
                    // fixme refactor in truly Swift fashion
                case let tabScreen as TabScreen:
                    AppTabView(initialTab: tabScreen, appState: appState, handler: handler)
                case let articleDetailsState as ArticleDetailsState:
                    Text(/*@START_MENU_TOKEN@*/"Hello, World!"/*@END_MENU_TOKEN@*/)
                default:
                    fatalError("Unhandled app state: \(appState), screen: \(screen)")
                }
            } else {
                // todo: show splash screen
                Text("Splash screen!")
            }
        }.onChange(of: appComponent.appState?.isInDarkMode) { newDarkMode in

            guard let darkMode = newDarkMode else {
                return
            }
            
            let window = UIApplication.shared.windows.first
            
            window?.overrideUserInterfaceStyle = darkMode ? .dark : .light
        }
    }
    
}

struct AppView_Previews: PreviewProvider {
    static var previews: some View {
        Text("abc")
        //AppView()
    }
}
