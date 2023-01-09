//
//  ArticleDetailsView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 09.01.2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import SharedAppLib

struct ArticleDetailsView : View {
    
    private let state: ArticleDetailsState
    private let handler: MessageHandler
    
    init(state: ArticleDetailsState, handler: @escaping MessageHandler) {
        self.state = state
        self.handler = handler
    }
    
    var body: some View {
        NavigationView {
            WebView(url: state.article.url)
                .navigationTitle(state.article.title as! String)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button {
                            handler(Pop.shared)
                        } label: {
                            Label("Back", systemImage: "chevron.backward")
                                .labelStyle(.titleAndIcon)
                        }
                    }
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button {
                            handler(OpenInBrowser(id: state.id))
                        } label: {
                            Label("Open in Safari", systemImage: "safari")
                        }
                    }
                }.navigationBarTitleDisplayMode(.inline)
        }
    }
}
