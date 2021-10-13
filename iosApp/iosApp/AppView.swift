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
        component = IosComponent(closeCommandsSink: { close in
            print("Close app \(close)")
        })
        
        cancellation = component.render { state in
            print("New app state \(state)")
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
    
    var body: some View {
        ZStack {
            if let appState = appComponent.appState {
                
                let screen = appState.screen
                let handler: MessageHandler = appComponent.dispatch
                
                switch screen {
                case let articlesState as ArticlesState:
                    ArticlesView(state: articlesState, handler: handler)
                case let settingsState as SettingsState:
                    Text(/*@START_MENU_TOKEN@*/"Hello, World!"/*@END_MENU_TOKEN@*/)
                case let articleDetailsState as ArticleDetailsState:
                    Text(/*@START_MENU_TOKEN@*/"Hello, World!"/*@END_MENU_TOKEN@*/)
                default:
                    fatalError("Unhandled app state: \(appState), screen: \(screen)")
                }
            } else {
                // todo: show splash screen
                Text("Splash screen!")
            }
        }
    }
}

struct AppView_Previews: PreviewProvider {
    static var previews: some View {
        Text("abc")
        //AppView()
    }
}
