//
//  SettingsView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 03.02.2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib

struct SettingsView: View {
    
    @State private var darkModeEnabled = false
    
    let handler: MessageHandler
    
    init(state: AppState, handler: @escaping MessageHandler) {
        self.darkModeEnabled = state.isInDarkMode
        self.handler = handler
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            
            Text("Settings")
                .font(.title)
                .padding()
            Form {
                Section("Theming") {
                    Toggle("Enable dark mode", isOn: $darkModeEnabled)
                }
            }
        }
        .onChange(of: darkModeEnabled) { _ in
            handler(OnToggleDarkMode.shared)
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(state: AppState(isInDarkMode: true, screens: [])) { _ in }
    }
}
