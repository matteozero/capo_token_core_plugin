import 'dart:async';

import 'package:capo_token_core_plugin/capo_token_core_plugin.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class Dialogs {
  static Future<void> showLoadingDialog(
      BuildContext context, GlobalKey key) async {
    return showDialog<void>(
        context: context,
        barrierDismissible: false,
        builder: (BuildContext context) {
          return new WillPopScope(
              onWillPop: () async => false,
              child: SimpleDialog(
                  key: key,
                  backgroundColor: Colors.black54,
                  children: <Widget>[
                    Center(
                      child: Column(children: [
                        CircularProgressIndicator(),
                        SizedBox(
                          height: 10,
                        ),
                        Text(
                          "Please Wait....",
                          style: TextStyle(color: Colors.blueAccent),
                        )
                      ]),
                    )
                  ]));
        });
  }
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  static const MethodChannel _channel =
      const MethodChannel('capo_token_core_plugin');

  final GlobalKey<State> _keyLoader = new GlobalKey<State>();
  String result = "Please waiting";

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  String wallet;

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) async {
      // Platform messages may fail, so we use a try/catch PlatformException.

      try {
        String keystoreString = """
    
{
	"mnemonicPath": "m\/44'\/60'\/0'\/0\/0",
	"address": "d32e794ab8441487d5155851fa2f1459c5802e0f",
	"encMnemonic": {
		"encStr": "85a5987f99e27fd38e2e48bd74b5f93a451e9f2695b128cf8a1822437e65f18bf7187da387b15187706a58c244ce08a8456da889133774949cddc993cf24b10ad7046f05bfa2",
		"nonce": "71b1d005c649ffc3b232ddf9f05036b8"
	},
	"metadata": {
		"timestamp": 1597713012,
		"network": "MAINNET",
		"segWit": "NONE",
		"chainType": "ETHEREUM",
		"from": "MNEMONIC"
	},
	"version": 3,
	"crypto": {
		"mac": "f80837c17d153f1bc938dd324745b9c3cec6e92cdfd050214826fe1765368e96",
		"kdf": "scrypt",
		"ciphertext": "1304cc6031f5899b75b2e52117512b12f5824121eada27d076dbb9e3d67b0975",
		"cipher": "aes-128-ctr",
		"kdfparams": {
			"dklen": 32,
			"n": 262144,
			"salt": "06ad987b4eeee4788d83593e5b4cab68b314be6ab39b8760fd8eeab8203cc63d",
			"p": 1,
			"r": 8
		},
		"cipherparams": {
			"iv": "3a7e4851af4cacfd069a245547cb593e"
		}
	},
	"id": "648cd11e-a540-4a23-b1d4-6b27e6d47cee"
}
    """;

        // String privateKey = "4c1b5f7aa4b3c7329caffb6217d79badb53e277e405f85fa87ec7d755694526f";
        // String randomMnemonic = await _channel.invokeMethod("randomMnemonic");
        //
        // String keystore = await _channel.invokeMethod("exportPrivateKey",{
        //   "keystore":keystoreString,
        //   "password":"12345678..."
        //   // "password":"12345678..."
        // }).catchError((error){
        //   print("error:${error.toString()}");
        // });
        // print("keystore: $keystore");

        // String randomMnemonic = await _channel.invokeMethod("randomMnemonic");

        // print("mnemonic: $randomMnemonic");

        // print("date1:${DateTime.now()}");
        // String keystore = await _channel.invokeMethod("verifyPassword",{
        //   "keystore":keystoreString,
        //   "password":"12345678..."
        // });
        // print("date2:${DateTime.now()}");

        Future.delayed(Duration(seconds: 3), () async {
          String nativeResult = await CapoTokenCorePlugin.exportMnemonic(
              keystoreString, "12345678...");

          setState(() {
            result = nativeResult;
          });
          print("result: $result");
        });
      } on PlatformException {
        wallet = 'Failed to get platform version.';
      }
    });

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;
  }

  Future<void> _handleSubmit(BuildContext context) async {
    try {
      Dialogs.showLoadingDialog(context, _keyLoader); //invoking login
//      await serivce.login(user.uid);
//      Navigator.of(_keyLoader.currentContext,rootNavigator: true).pop();//close the dialoge
//      Navigator.pushReplacementNamed(context, "/home");
    } catch (error) {
      print(error);
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: SimpleDialog(
          key: _keyLoader,
          backgroundColor: Colors.black54,
          children: <Widget>[
            Center(
              child: Column(children: [
                CircularProgressIndicator(),
                SizedBox(
                  height: 10,
                ),
                Text(
                  result,
                  style: TextStyle(color: Colors.blueAccent),
                )
              ]),
            )
          ]),
    ));
  }
}
