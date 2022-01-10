//
//  WebView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 31.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import WebKit

// Small class to hold variables that we'll use in the View body
class observable: ObservableObject {
    @Published var observation:NSKeyValueObservation?
    @Published var loggedIn = false
}

// UIViewRepresentable, wraps UIKit views for use with SwiftUI
struct WebView: UIViewRepresentable {
    var pageURL:String     // Page to load
    @ObservedObject var observe = observable()     // Variables
    
    func makeUIView(context: Context) -> WKWebView {
        return WKWebView() // Just make a new WKWebView, we don't need to do anything else here.
    }
    
    func updateUIView(_ uiView: WKWebView, context: Context) {
        // Set up our key-value observer - we're checking for WKWebView.title changes here
        // which indicates a new page has loaded.
        observe.observation = uiView.observe(\WKWebView.title, options: .new) { view, change in
            if let title = view.title {
                observe.loggedIn = true     // We loaded the page
                print("Page loaded: \(title)")
            }
        }
        uiView.load(pageURL)     // Send the command to WKWebView to load our page
    }
}

struct WebView_Previews: PreviewProvider {
    static var previews: some View {
        WebView(pageURL: "https://apple.com")
    }
}

// Extension for WKWebView so we can just pass a URL string to .load() instead of all the boilerplate
extension WKWebView {
    func load(_ urlString: String) {
        if let url = URL(string: urlString) {
            let request = URLRequest(url: url)
            load(request)
        }
    }
}
