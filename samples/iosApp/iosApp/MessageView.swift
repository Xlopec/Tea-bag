//
//  MessageView.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 27.10.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import Foundation
import SwiftUI

// Message view that displays message with action button
struct MessageView: View {
    
    let message: String
    let actionButtonMessage: String
    let action: () -> Void
    
    var body: some View {
        VStack(spacing: 8) {
            Text(message)
                .font(.body)
                .multilineTextAlignment(.center)
            Button(action: action) {
                Text(actionButtonMessage)
            }
        }
    }
}

struct MessageView_Previews: PreviewProvider {
    
    static var previews: some View {
        MessageView(message: "Oooops... Couldn't load data because of the network exception", actionButtonMessage: "Retry") {
            
        }
    }
}
