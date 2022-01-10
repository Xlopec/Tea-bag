//
//  TabView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 07.11.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib

struct AppTabView<Content : View>: View {
    
    @State private var tab = 0
    
    let handler: MessageHandler
    
    @ViewBuilder let contentForTab: (Int) -> Content
    
    init(initialTab: Int, handler: @escaping MessageHandler, @ViewBuilder contentForTab: @escaping (Int) -> Content) {
        self.tab = initialTab
        self.handler = handler
        self.contentForTab = contentForTab
    }
    
    var body: some View {
        TabView(selection: $tab) {
            
            contentForTab(0)
                .tabItem {
                    Image(systemName: "globe")
                    Text("Articles")
                }.tag(0)
            
            contentForTab(1)
                .tabItem {
                    Image(systemName: "heart")
                    Text("Favorites")
                }.tag(1)
            
            contentForTab(2)
                .tabItem {
                    Image(systemName: "chart.line.uptrend.xyaxis")
                    Text("Trending")
                }.tag(2)
            
            contentForTab(3)
                .tabItem {
                    Image(systemName: "gear")
                    Text("Settings")
                }.tag(3)
            
        }
        .onChange(of: tab) { newTab in
            
            switch newTab {
            case 0:
                handler(NavigateToFeed.shared)
            case 1:
                handler(NavigateToFavorite.shared)
            case 2:
                handler(NavigateToTrending.shared)
            case 3:
                handler(NavigateToSettings.shared)
            default:
                handler(NavigateToSettings.shared)
            }
        }
    }
}

//struct AppTabView_Previews: PreviewProvider {
//    static var previews: some View {
// AppTabView()
//    }
//}
