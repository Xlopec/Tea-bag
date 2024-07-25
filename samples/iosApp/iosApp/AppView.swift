//
//  AppView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 11.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import SharedAppLib

struct AppView: View {
    
    @SwiftUI.Environment(\.colorScheme) private var colorScheme: ColorScheme
    
    let component: IosComponent
    
    var body: some View {
        ComposeViewController(component: component)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .ignoresSafeArea()
    }
    
    private func updateDarkMode(darkModeEnabled: Bool) {
        let window = UIApplication.shared.windows.first
        
        window?.overrideUserInterfaceStyle = darkModeEnabled ? .dark : .light
    }
    
}

struct ComposeViewController: UIViewControllerRepresentable {
    
    let component: IosComponent
    
    func makeUIViewController(context: Context) -> UIViewController {
        App_iosKt.appController(component: component)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
