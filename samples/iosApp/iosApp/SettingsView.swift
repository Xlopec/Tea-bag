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
    
    @State private var userDarkMode: Bool
    @State private var syncWithSystemDarkMode: Bool
    
    let handler: MessageHandler
    
    init(settings: Settings, handler: @escaping MessageHandler) {
        self.handler = handler
        self.userDarkMode = settings.userDarkModeEnabled
        self.syncWithSystemDarkMode = settings.syncWithSystemDarkModeEnabled
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            
            Text("Settings")
                .font(.title)
                .padding()
            Form {
                Section("Appearance") {
                    Toggle("Use dark mode in the app", isOn: $userDarkMode)
                        .disabled(syncWithSystemDarkMode)
                    Toggle("Use system dark mode", isOn: $syncWithSystemDarkMode)
                }
            }.onChange(of: userDarkMode) {
                handler(ToggleDarkMode(userDarkModeEnabled: $0, syncWithSystemDarkModeEnabled: syncWithSystemDarkMode))
            }.onChange(of: syncWithSystemDarkMode) {
                handler(ToggleDarkMode(userDarkModeEnabled: userDarkMode, syncWithSystemDarkModeEnabled: $0))
            }
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(settings: Settings(userDarkModeEnabled: true, systemDarkModeEnabled: true, syncWithSystemDarkModeEnabled: true), handler: {_ in })
    }
 }
