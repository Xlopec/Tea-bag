//
//  WebView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 31.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import WebKit

// UIViewRepresentable, wraps UIKit views for use with SwiftUI
struct WebView: UIViewRepresentable {
    var url: URL     // Page to load
    
    func makeUIView(context: Context) -> WKWebView {
        return WKWebView() // Just make a new WKWebView, we don't need to do anything else here.
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        let request = URLRequest(url: url)
        uiView.load(request)     // Send the command to WKWebView to load our page
    }
}

struct WebView_Previews: PreviewProvider {
    static var previews: some View {
        WebView(url: URL(string: "https://apple.com")!)
    }
}
