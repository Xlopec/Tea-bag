//
//  AppView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 11.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib
import SwiftUINavigation

// todo add custom App protocol implementation

class ObservableAppComponent : ObservableObject {
    
    @Published public var appState: AppState? = nil
    
    private var cancellation: Cancellation?
    let component: IosComponent
    
    init(systemDarkModeEnabled: Bool) {
        component = IosComponent(systemDarkModeEnabled: systemDarkModeEnabled)
        
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
    
    @SwiftUI.Environment(\.colorScheme) var colorScheme: ColorScheme
    
    @ObservedObject private(set) var appComponent: ObservableAppComponent
    
    private let detailsState: Binding<ArticleDetailsState?>
    
    let handler: MessageHandler
    
    init(appComponent: ObservableAppComponent) {
        self.appComponent = appComponent
        self.handler = appComponent.dispatch
        self.detailsState = Binding<ArticleDetailsState?>(
            get: { appComponent.appState?.screen as? ArticleDetailsState},
            set: {_,_ in }
        )
    }
    
    var body: some View {
        NavigationStack {
            ZStack {
                if let appState = appComponent.appState {
                    AppTabView(initialTab: appState.homeScreen, appState: appState, handler: handler)
                } else {
                    // todo: show splash screen
                    Text("News Reader")
                        .font(.headline)
                }
            }.navigationDestination(unwrapping: detailsState) { details in
                ArticleDetailsView(state: details.wrappedValue, handler: handler)
                    .navigationBarBackButtonHidden(true)
            }
        }
        .onChange(of: appComponent.appState?.settings) {
            if let darkMode = $0?.appDarkModeEnabled {
                updateDarkMode(darkModeEnabled: darkMode)
            }

        }.onChange(of: colorScheme) {
            handler(SystemDarkModeChanged(enabled: $0 == .dark))
        }
    }
    
    private func updateDarkMode(darkModeEnabled: Bool) {
        let window = UIApplication.shared.windows.first
        
        window?.overrideUserInterfaceStyle = darkModeEnabled ? .dark : .light
    }
    
}

struct AppView_Previews: PreviewProvider {
    static var previews: some View {
        Text("abc")
        //AppView()
    }
}
