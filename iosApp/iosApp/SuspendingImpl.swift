//
//  SuspendingImpl.swift
//  iosApp
//
//  Created by Maksym Oliinyk2 on 15.06.2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import Foundation
import TeaCore

class SuspendingImpl : KotlinSuspendFunction1 { 
    
    func invoke(p1: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
        completionHandler(p1.debugDescription, nil)
    }
    
    
}

class SuspendingImpl0: KotlinSuspendFunction0 {
    
    func invoke(completionHandler: @escaping (Any?, Error?) -> Void) {
        completionHandler("some result", nil)
    }
}


class CollectorImpl : Kotlinx_coroutines_coreFlowCollector {
    
    func emit(value: Any?, completionHandler: @escaping (KotlinUnit?, Error?) -> Void) {
        
        print("on emit \(value)")
        
        completionHandler(KotlinUnit.init(), nil)
    }
    
    
}
