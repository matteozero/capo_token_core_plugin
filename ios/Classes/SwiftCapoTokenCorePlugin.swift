import Flutter
import TokenCoreCapo
import UIKit
public class SwiftCapoTokenCorePlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "capo_token_core_plugin", binaryMessenger: registrar.messenger())
        let instance = SwiftCapoTokenCorePlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case CallMethod.randomMnemonic.rawValue:
            onRandomMnemonic(call, result: result)

        case CallMethod.verifyPassword.rawValue:
            onVerityPassword(call, result: result)

        case CallMethod.importMnenonic.rawValue:
            onImportMnemonic(call, result: result)

        case CallMethod.importPrivateKey.rawValue:
            onImportPrivateKey(call, result: result)

        case CallMethod.importKeystore.rawValue:
            onImportKeystore(call, result: result)

        case CallMethod.exportMnemonic.rawValue:
            onExportMnemonic(call, result: result)

        case CallMethod.exportPrivateKey.rawValue:
            onExportPrivateKey(call, result: result)

        default: result(FlutterMethodNotImplemented)
        }
    }

    private func onRandomMnemonic(_ call: FlutterMethodCall, result:  (Any?) -> Void) {
        let mnemonic = MnemonicUtil.generateMnemonic(words: Words.twelve)
        result(mnemonic)
    }

    private func onImportMnemonic(_ call: FlutterMethodCall, result: @escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async {
            do {
                let mnemonic = arguments["mnemonic"] as! String
                let password = arguments["password"] as! String
                let walletMeta = WalletMeta(chain: ChainType.eth, from: WalletFrom.mnemonic)
                let identity = try Identity.recoverIdentity(metadata: walletMeta, mnemonic: mnemonic, password: password)

                let importResult = identity.ethereumWallet.keystore.dump()
                DispatchQueue.main.async {
                    result(importResult)
                }
            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.error.rawValue, message: "\(error)", details: nil))
                }
               
            }
        }
       
    }

    private func onExportMnemonic(_ call: FlutterMethodCall, result:@escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async {[weak self] in
            guard let self = self else{return}
            do {
                let keystore = arguments["keystore"] as! String
                let password = arguments["password"] as! String

                let jsonData: Data? = keystore.data(using: .utf8)
                let json = try JSONSerialization.jsonObject(with: jsonData!) as! JSONObject
                guard let _ = json["encMnemonic"] else {
                    DispatchQueue.main.async {
                        result(FlutterError(code: ErrorCode.illegalOperation.rawValue, message: "The keystore does not have mnemonic.", details: nil))
                    }
                   
                    return
                }

                let mapResult = try self.mapKeystoreString2Object(json: json)
                if let identityKeystore = mapResult.0 {
                    let mnemonic: String = try identityKeystore.mnemonic(from: password)
                    result(mnemonic)
                }

                if let wallet = mapResult.1 {
                    let mnemonic: String = try wallet.exportMnemonic(password: password)
                    DispatchQueue.main.async {
                        result(mnemonic)
                    }
                   
                }

            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.exportError.rawValue, message: "\(error.localizedDescription)", details: nil))
                }
               
            }
        }
       
    }

    private func onImportKeystore(_ call: FlutterMethodCall, result: @escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async {[weak self] in
            guard let self = self else {return}
            do {
                let keystore = arguments["keystore"] as! String
                let password = arguments["password"] as! String
                let mapResult = try self.mapKeystoreString2Object(json: nil, keystoreString: keystore)

                if let wallet = mapResult.1 {
                    let privateKey: String = try wallet.privateKey(password: password, isHDWalletExportWif: false)

                    let walletMeta = WalletMeta(chain: ChainType.eth, from: WalletFrom.keystore)
                    let wallet = try BasicWallet.importFromPrivateKey(privateKey, encryptedBy: password, metadata: walletMeta)

                    let keystore: String = wallet.keystore.dump()
                    DispatchQueue.main.async {
                        result(keystore)
                    }
                   
                }

                if let _ = mapResult.0 {
                    DispatchQueue.main.async {
                        result(FlutterError(code: ErrorCode.illegalOperation.rawValue, message: "Keystore type not match..", details: nil))
                    }
                   
                }
            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.error.rawValue, message: "\(error)", details: nil))
                }
               
            }
        }
       
    }

    private func onImportPrivateKey(_ call: FlutterMethodCall, result: @escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async {
            do {
                let privateKey = arguments["privateKey"] as! String
                let password = arguments["password"] as! String

                let walletMeta = WalletMeta(chain: ChainType.eth, from: WalletFrom.privateKey)
                let wallet = try BasicWallet.importFromPrivateKey(privateKey, encryptedBy: password, metadata: walletMeta)

                let keystore: String = wallet.keystore.dump()
                DispatchQueue.main.async {
                    result(keystore)

                }
            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.error.rawValue, message: "\(error)", details: nil))
                }
               
            }
        }
       
    }

    private func onExportPrivateKey(_ call: FlutterMethodCall, result: @escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async { [weak self] in
            guard let self = self else { return }
            do {
                let keystore = arguments["keystore"] as! String
                let password = arguments["password"] as! String
                let mapResult = try self.mapKeystoreString2Object(json: nil, keystoreString: keystore, isExportPrivate: true)

                if let wallet = mapResult.1 {
                    let privateKey: String = try wallet.privateKey(password: password, isHDWalletExportWif: true)
                    DispatchQueue.main.async {
                        result(privateKey)
                    }
                }

                if let _ = mapResult.0 {
                    DispatchQueue.main.async {
                        result(FlutterError(code: ErrorCode.illegalOperation.rawValue, message: "Identity doesn't support export private key.", details: nil))
                    }
                }
            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.exportError.rawValue, message: "\(error)", details: nil))
                }
            }
        }
    }

    private func onVerityPassword(_ call: FlutterMethodCall, result:@escaping (Any?) -> Void) {
        guard let arguments = isArgumentIllegal(call, result: result) else {
            return
        }
        DispatchQueue.global().async {[weak self] in
            guard let self = self else{return}
            do {
                let keystore = arguments["keystore"] as! String
                let password = arguments["password"] as! String

                let mapResult = try self.mapKeystoreString2Object(json: nil, keystoreString: keystore)
                if let identityKeystore = mapResult.0 {
                    let verify: Bool = identityKeystore.verify(password: password)
                    DispatchQueue.main.async {
                        result("\(verify)")
                    }
                   
                    return
                }

                if let wallet = mapResult.1 {
                    let verify: Bool = wallet.verifyPassword(password)
                    DispatchQueue.main.async {
                        result("\(verify)")
                    }
                   
                    return
                }
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.keystoreError.rawValue, message: "Keystore type not match.", details: nil))
                }
               
            } catch {
                DispatchQueue.main.async {
                    result(FlutterError(code: ErrorCode.keystoreError.rawValue, message: "\(error)", details: nil))
                }
               
            }
        }
       
    }

    // only accept map param
    private func isArgumentIllegal(_ call: FlutterMethodCall, result: FlutterResult) -> Dictionary<String, Any?>? {
        guard let arguments = call.arguments as? Dictionary<String, Any?> else {
            result(FlutterError(
                code: ErrorCode.argsError.rawValue,
                message: String(format: "arguments in %s method type error.need map", arguments: [call.method]),
                details: nil
            ))
            return nil
        }
        return arguments
    }

    func mapKeystoreString2Object(json: JSONObject?, keystoreString: String? = nil, isExportPrivate: Bool = false) throws -> (IdentityKeystore?, BasicWallet?) {
        var theJson: JSONObject
        if let keystoreStr = keystoreString {
            let jsonData: Data? = keystoreStr.data(using: .utf8)
            let json = try JSONSerialization.jsonObject(with: jsonData!) as! JSONObject
            theJson = json
        } else {
            theJson = json!
        }
        if let _ = theJson["identifier"] {
            let identityKeystore = try IdentityKeystore(json: theJson)
            return (identityKeystore, nil)
        } else {
            if (theJson[WalletMeta.key] as? JSONObject) == nil {
                let mata = WalletMeta(chain: ChainType.eth, from: WalletFrom.keystore, network: Network.mainnet, segwit: SegWit.none)
                theJson[WalletMeta.key] = mata.toJSON()
            }

            let wallet = try BasicWallet(json: theJson)
            return (nil, wallet)
        }
    }
}

