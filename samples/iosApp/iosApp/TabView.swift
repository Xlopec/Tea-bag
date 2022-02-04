//
//  TabView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 07.11.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib

// This is custom implementation of TabView since default implementation
// heavily relies on tab state binding which sometimes conflicts with screen state and
// results in visual glitches
struct AppTabView: View {
    
    private let tab: TabScreen
    private let appState: AppState
    
    private let icons = ["globe", "heart", "chart.line.uptrend.xyaxis", "gear"]
    private let titles = ["Articles", "Favorites", "Trending", "Settings"]
    
    private let handler: MessageHandler
    
    @State private var darkModeEnabled = false
        
    init(initialTab: TabScreen, appState: AppState, handler: @escaping MessageHandler) {
        self.tab = initialTab
        self.appState = appState
        self.handler = handler
    }
    
    var body: some View {
        VStack(spacing: 0) {
            
            ZStack {
                if let articlesState = tab as? ArticlesState {
                    ArticlesView(state: articlesState, handler: handler, searchHintText: articlesState.searchHintText, headingText: articlesState.headingText)
                } else {
                    SettingsView(darkMode: $darkModeEnabled)
                        .onChange(of: darkModeEnabled) {_ in
                            handler(OnToggleDarkMode.shared)
                        }
                }
            }
                        
            Divider()
                .padding(.bottom, 10)
            
            HStack {
                
                ForEach(0..<4, id: \.self) { number in
                    Spacer()
                    Button(action: {
                        
                        switch number {
                        case 0:
                            handler(NavigateToFeed.shared)
                        case 1:
                            handler(NavigateToFavorite.shared)
                        case 2:
                            handler(NavigateToTrending.shared)
                        default:
                            handler(NavigateToSettings.shared)
                        }
                        
                    }, label: {
                        
                        VStack {
                            
                            Image(systemName: icons[number])
                                .font(.system(size: 25, weight: .regular, design: .default))
                                .foregroundColor(number == tab.tabId ? .blue : Color(UIColor.lightGray))
                            
                            Text(titles[number])
                                .font(.caption)
                                .foregroundColor(number == tab.tabId ? .blue : Color(UIColor.lightGray))
                        }
                    })
                    
                    Spacer()
                }
            }
        }
    }
}

struct AppTabView_Previews: PreviewProvider {
    static var previews: some View {
        AppTabView(initialTab: SettingsState.shared, appState: AppState(isInDarkMode: true, screens: []), handler: {_ in })
    }
}

private extension TabScreen {
    
    var tabId: Int {
        
        var initialTab = 3
        
        if let articlesState = self as? ArticlesState {
            if articlesState.query.type.description() == "Regular" {
                initialTab = 0
            } else if articlesState.query.type.description() == "Favorite" {
                initialTab = 1
            } else if articlesState.query.type.description() == "Trending" {
                initialTab = 2
            }
        }
        
        return initialTab
        
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
