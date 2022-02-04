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
    
    @Binding public var darkMode: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            
            Text("Settings")
                .font(.title)
                .padding()
            Form {
                Section("Theming") {
                    Toggle("Enable dark mode", isOn: $darkMode)
                }
            }
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView(darkMode: Binding.constant(true))
    }
}
