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
    
    init(initialTab: TabScreen, appState: AppState, handler: @escaping MessageHandler) {
        self.tab = initialTab
        self.appState = appState
        self.handler = handler
    }
    
    var body: some View {
        VStack(spacing: 0) {
            
            ScrollViewReader { proxy in
                
                ZStack {
                    if let articlesState = tab as? ArticlesState {
                        ArticlesView(state: articlesState, handler: handler, searchHintText: articlesState.searchHintText, headingText: articlesState.headingText)
                    } else {
                        SettingsView(settings: appState.settings, handler: handler)
                    }
                }
                
                Divider()
                    .padding(.bottom, 10)
                
                HStack {
                    
                    ForEach(icons.indices, id: \.self) { tabId in
                        Spacer()
                        Button(action: {
                            handleTabNavigation(proxy: proxy, forTabIdSelection: tabId)
                        }, label: {
                            
                            VStack {
                                
                                Image(systemName: icons[tabId])
                                    .font(.system(size: 25, weight: .regular, design: .default))
                                    .foregroundColor(tabId == tab.tabId ? .blue : Color(UIColor.lightGray))
                                
                                Text(titles[tabId])
                                    .font(.caption)
                                    .foregroundColor(tabId == tab.tabId ? .blue : Color(UIColor.lightGray))
                            }
                        })
                        
                        Spacer()
                    }
                }
            }
        }
    }
    
    private func handleTabNavigation(proxy: ScrollViewProxy, forTabIdSelection number: Int) {
        
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
        
        let data = (tab as? ArticlesState)?.loadable.data as? Array<Article> ?? []
        
        if number == tab.tabId, !data.isEmpty, let topItem = data.first {
            withAnimation {
                proxy.scrollTo(topItem.url)
            }
        }
    }
    
}

struct AppTabView_Previews: PreviewProvider {
    
    static let settings = Settings(userDarkModeEnabled: true, systemDarkModeEnabled: true, syncWithSystemDarkModeEnabled: true)
    static let appState = AppState(settings: settings, screens: [])
    
    static var previews: some View {
        AppTabView(initialTab: SettingsScreen.shared, appState: appState, handler: {_ in })
    }
}

private extension TabScreen {
    
    var tabId: Int {
        
        var initialTab = 3
        
        if let articlesState = self as? ArticlesState {
            if articlesState.filter.type.description() == "Regular" {
                initialTab = 0
            } else if articlesState.filter.type.description() == "Favorite" {
                initialTab = 1
            } else if articlesState.filter.type.description() == "Trending" {
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
        
        if filter.type.description() == "Regular" {
            text = "Feed"
        } else if filter.type.description() == "Favorite" {
            text = "Favorite"
        } else if filter.type.description() == "Trending" {
            text = "Trending"
        }
        
        return text
    }
    
    var searchHintText: String {
        var text = "Search in articles..."
        
        if filter.type.description() == "Regular" {
            text = "Search in articles..."
        } else if filter.type.description() == "Favorite" {
            text = "Search in favorite..."
        } else if filter.type.description() == "Trending" {
            text = "Search in trending..."
        }
        
        return text
    }
}
