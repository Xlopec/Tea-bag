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
                    AppTabViewContent(tabScreen: tabScreen)
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
    
    @ViewBuilder
    private func AppTabViewContent(tabScreen: TabScreen) -> some View {
        AppTabView(initialTab: (tabScreen as? ArticlesState)?.displayTab ?? 3, handler: handler) { tab in
            
            if let articlesState = tabScreen as? ArticlesState {
                ArticlesView(state: articlesState, handler: handler, searchHintText: tab.searchHintText, headingText: articlesState.headingText)
            } else {
                Text("App Settings")
            }
        }
    }
    
}

private extension Int {
    
    var searchHintText: String {
        var text = "Search in articles..."
        
        if self == 0 {
            text = "Search in articles..."
        } else if self == 1 {
            text = "Search in favorite..."
        } else if self == 2 {
            text = "Search in trending..."
        }
        
        return text
    }
    
}

private extension ArticlesState {
    
    var headingText: String {
        var text = "Feed"
        
        if query.type.description() == "Regular" {
            text = "Feed"
        } else if query.type.description() == "Favorite" {
            text = "Favorite"
        } else if query.type.description() == "Trending" {
            text = "Trending"
        }
        
        return text
    }
    
    var displayTab: Int {
        
        var initialTab = 0
        
        if query.type.description() == "Regular" {
            initialTab = 0
        } else if query.type.description() == "Favorite" {
            initialTab = 1
        } else if query.type.description() == "Trending" {
            initialTab = 2
        }
        
        return initialTab
    }
    
    var searchHintText: String {
        var text = "Search in articles..."
        
        if query.type.description() == "Regular" {
            text = "Search in articles..."
        } else if query.type.description() == "Favorite" {
            text = "Search in favorite..."
        } else if query.type.description() == "Trending" {
            text = "Search in trending..."
        }
        
        return text
    }
}

struct AppView_Previews: PreviewProvider {
    static var previews: some View {
        Text("abc")
        //AppView()
    }
}
