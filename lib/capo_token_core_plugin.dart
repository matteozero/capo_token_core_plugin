// You have generated a new plugin project without
// specifying the `--platforms` flag. A plugin project supports no platforms is generated.
// To add platforms, run `flutter create -t plugin --platforms <platforms> .` under the same
// directory. You can also find a detailed instruction on how to add platforms in the `pubspec.yaml` at https://flutter.dev/docs/development/packages-and-plugins/developing-packages#plugin-platforms.

import 'dart:async';

import 'package:flutter/services.dart';

class CapoTokenCorePlugin {
  static const MethodChannel _channel =
      const MethodChannel('capo_token_core_plugin');

  static Future<String> get randomMnemonic async {
    final String mnemonic = await _channel.invokeMethod('randomMnemonic');
    return mnemonic;
  }

  static Future<String> importPrivateKey(
      String privateKey, String password) async {
    final String keystore = await _channel.invokeMethod(
        'importPrivateKey', {"privateKey": privateKey, "password": password});

    return keystore;
  }

  static Future<String> importMnenonic(String mnemonic, String password) async {
    final String keystore = await _channel.invokeMethod(
        'importMnenonic', {"mnemonic": mnemonic, "password": password});
    return keystore;
  }

  static Future<String> importKeystore(
      String keystoreJson, String password) async {
    final String keystore = await _channel.invokeMethod(
        'importKeystore', {"keystore": keystoreJson, "password": password});
    return keystore;
  }

  static Future<String> exportPrivateKey(
      String keystoreJson, String password) async {
    final String privateKey = await _channel.invokeMethod(
        'exportPrivateKey', {"keystore": keystoreJson, "password": password});
    return privateKey;
  }

  static Future<String> exportMnemonic(
      String keystoreJson, String password) async {
    final String mnemonic = await _channel.invokeMethod(
        'exportMnemonic', {"keystore": keystoreJson, "password": password});
    return mnemonic;
  }

  static Future<bool> verifyPassword(
      String keystoreJson, String password) async {
    final String verify = await _channel.invokeMethod(
        'verifyPassword', {"keystore": keystoreJson, "password": password});
    if (verify == "true") {
      return true;
    } else {
      return false;
    }
  }
}
