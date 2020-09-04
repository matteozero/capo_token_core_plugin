//
// Created by lhalcyon on 2019-02-23.
//

import Foundation

enum CallMethod: String {

    case randomMnemonic = "randomMnemonic"
        
    case importPrivateKey = "importPrivateKey"

    case importMnenonic = "importMnenonic"
    
    case importKeystore = "importKeystore"
    
    case exportPrivateKey = "exportPrivateKey"

    case exportMnemonic = "exportMnemonic"

    case verifyPassword = "verifyPassword"
    
}
