package com.capo.capo_token_core_plugin;

import android.app.Activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhalcyon.tokencore.foundation.crypto.Crypto;
import com.lhalcyon.tokencore.foundation.utils.MnemonicUtil;
import com.lhalcyon.tokencore.wallet.bip.Words;
import com.lhalcyon.tokencore.wallet.ex.ChainType;
import com.lhalcyon.tokencore.wallet.ex.ExIdentity;
import com.lhalcyon.tokencore.wallet.ex.ExMetadata;
import com.lhalcyon.tokencore.wallet.ex.ExWallet;
import com.lhalcyon.tokencore.wallet.ex.Network;
import com.lhalcyon.tokencore.wallet.ex.SegWit;
import com.lhalcyon.tokencore.wallet.ex.WalletFrom;
import com.lhalcyon.tokencore.wallet.ex.WalletType;
import com.lhalcyon.tokencore.wallet.keystore.ExHDMnemonicKeystore;
import com.lhalcyon.tokencore.wallet.keystore.ExIdentityKeystore;
import com.lhalcyon.tokencore.wallet.keystore.V3Keystore;
import com.capo.capo_token_core_plugin.args.ArgsValid;
import com.capo.capo_token_core_plugin.args.CreateIdentityArgs;
import com.capo.capo_token_core_plugin.args.ExportArgs;
import com.capo.capo_token_core_plugin.args.ImportPrivateKeyArgs;
import com.capo.capo_token_core_plugin.args.RecoverIdentityArgs;
import com.capo.capo_token_core_plugin.args.VerifyArgs;
import com.capo.capo_token_core_plugin.model.FlutterExIdentity;
import com.capo.capo_token_core_plugin.model.FlutterExMetadata;
import com.capo.capo_token_core_plugin.model.FlutterExWallet;
import com.capo.capo_token_core_plugin.util.KeystoreUtil;

//import org.consenlabs.tokencore.wallet.Identity;
//import org.consenlabs.tokencore.wallet.KeystoreStorage;
//import org.consenlabs.tokencore.wallet.WalletManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * TokenCorePlugin
 */
public class CapoTokenCorePlugin implements MethodCallHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
//    private final Activity activity;
//
    public CapoTokenCorePlugin() {
    }
    /**
     * Plugin registration.
     */
//    public static void registerWith(PluginRegistry.Registrar registrar) {
//        final MethodChannel channel = new MethodChannel(registrar.messenger(), "capo_token_core_plugin");
//        channel.setMethodCallHandler(new CapoTokenCorePlugin());
//    }


    public static void registerWith(PluginRegistry.Registrar registrar) {

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "capo_token_core_plugin");
        channel.setMethodCallHandler(new CapoTokenCorePlugin());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call == null || call.method == null) {
            result.error(ErrorCode.CALL_ERROR, "call or call.method is null", call);
            return;
        }
        switch (call.method) {

            case CallMethod.randomMnemonic:
                onRandomMnemonic(call, result);
                break;
            case CallMethod.exportMnemonic:
                onExportMnemonic(call, result);
                break;
            case CallMethod.exportPrivateKey:
                onExportPrivateKey(call, result);
                break;
            case CallMethod.importPrivateKey:
                onImportPrivateKey(call, result);
                break;
            case CallMethod.importMnenonic:
                onimportMnenonic(call, result);
                break;

            case CallMethod.verifyPassword:
                onVerifyPassword(call, result);
                break;


            default:
                result.notImplemented();
                break;
        }
    }



    private void onVerifyPassword(MethodCall call, Result result) {
        try {
            if (isArgumentIllegal(call, result)) {
                return;
            }
            Object arguments = call.arguments;
            String json = objectMapper.writeValueAsString(arguments);
            VerifyArgs args = objectMapper.readValue(json, VerifyArgs.class);
            boolean isCorrect = false;
            if (KeystoreUtil.isIdentityKeystore(objectMapper,args.keystore)) {
                ExIdentityKeystore identityKeystore = objectMapper.readValue(args.keystore, ExIdentityKeystore.class);
                isCorrect = identityKeystore.verifyPassword(args.password);
            } else {
                ExWallet wallet = mapKeystore2Wallet(args.keystore, args.password);
                isCorrect = wallet.getKeystore().verifyPassword(args.password);
            }
            result.success(isCorrect ? "true" : "false");
        } catch (Exception e) {
            e.printStackTrace();
            result.error(ErrorCode.ERROR, e.getMessage(), null);
        }
    }



    private void onImportPrivateKey(MethodCall call, Result result) {
        try {
            if (isArgumentIllegal(call, result)) {
                return;
            }
            Object arguments = call.arguments;
            String json = objectMapper.writeValueAsString(arguments);
            ImportPrivateKeyArgs args = objectMapper.readValue(json, ImportPrivateKeyArgs.class);

            if (isArgumentValid(args, call, result)) {
                return;
            }
            ExMetadata exMetadata = new ExMetadata();

            exMetadata.setFrom(WalletFrom.PRIAVTE_KEY);
            exMetadata.setChainType(ChainType.ETHEREUM);
            exMetadata.setNetwork(Network.MAINNET);
//            exMetadata.setSegWit(SegWit.NONE);
            exMetadata.setWalletType(WalletType.V3);

            V3Keystore keystore = new V3Keystore(exMetadata, args.password, args.privateKey, "");
            String keystoreJson = keystore.toJsonString();
            result.success(keystoreJson);
//
//            ExWallet exWallet = new ExWallet(keystore);
//
//            FlutterExMetadata meta = new FlutterExMetadata(exMetadata);
//            FlutterExWallet wallet = new FlutterExWallet(meta, keystore.toJsonString(), exWallet.getAddress());
//            String s = objectMapper.writeValueAsString(wallet);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(ErrorCode.IMPORT_ERROR, e.getMessage(), null);
        }

    }


    private void onimportMnenonic(MethodCall call, Result result) {
        try {
            if (isArgumentIllegal(call, result)) {
                return;
            }

            Object arguments = call.arguments;
            String json = objectMapper.writeValueAsString(arguments);
            RecoverIdentityArgs args = objectMapper.readValue(json, RecoverIdentityArgs.class);
            if (isArgumentValid(args, call, result)) {
                return;
            }
            ExIdentity rawIdentity = ExIdentity.recoverIdentity(args.mnemonic, args.password, Network.MAINNET,SegWit.NONE);
           String keystoreJson = rawIdentity.getEthereumWallet().getKeystore().toJsonString();
           result.success(keystoreJson);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(ErrorCode.IMPORT_ERROR, e.getMessage(), null);
        }

    }

    private void onExportPrivateKey(MethodCall call, Result result) {
        try {
            if (isArgumentIllegal(call, result)) {
                return;
            }

            Object arguments = call.arguments;
            String json = objectMapper.writeValueAsString(arguments);
            ExportArgs args = objectMapper.readValue(json, ExportArgs.class);

            ExWallet wallet = mapKeystore2Wallet(args.keystore, args.password);
            String privateKey = wallet.exportPrivateKey(args.password);
            result.success(privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(ErrorCode.EXPORT_ERROR, "export error , " + e.getMessage(), null);
        }
    }

    private void onExportMnemonic(MethodCall call, Result result) {
        try {
            if (isArgumentIllegal(call, result)) {
                return;
            }
            Object arguments = call.arguments;
            String json = objectMapper.writeValueAsString(arguments);
            VerifyArgs args = objectMapper.readValue(json, VerifyArgs.class);
            String mnemonic = "";
            if (KeystoreUtil.isIdentityKeystore(objectMapper,args.keystore)) {
                ExIdentityKeystore identityKeystore = objectMapper.readValue(args.keystore, ExIdentityKeystore.class);
                mnemonic = identityKeystore.decryptMnemonic(args.password);
            } else {
                ExWallet wallet = mapKeystore2Wallet(args.keystore, args.password);
                mnemonic = wallet.exportMnemonic(args.password).getMnemonic();
            }
            result.success(mnemonic);
        } catch (Exception e) {
            e.printStackTrace();
            result.error(ErrorCode.EXPORT_ERROR, e.getMessage(), null);
        }
    }


    @SuppressWarnings({"unused", "unchecked"})
    private void onRandomMnemonic(MethodCall call, Result result) {

        Map<String, Object> map = (Map<String, Object>) call.arguments;
        List<String> strings = MnemonicUtil.randomMnemonicCodes(Words.TWELVE);
        String mnemonic = MnemonicUtil.mnemonicToString(strings);
        result.success(mnemonic);
    }


    private void handleRawIdentity(Result result, ExIdentity rawIdentity) {
        try {
            String keystore = rawIdentity.getKeystore().toString();
            List<FlutterExWallet> wallets = new ArrayList<>();
            for (int i = 0; i < rawIdentity.getWallets().size(); i++) {
                ExWallet exWallet = rawIdentity.getWallets().get(i);
                ExMetadata exMetadata = exWallet.getMetadata();
                FlutterExMetadata metadata = new FlutterExMetadata(exMetadata);
                FlutterExWallet wallet = new FlutterExWallet(metadata, exWallet.getKeystore().toJsonString(), exWallet.getAddress());
                wallets.add(wallet);
            }

            FlutterExMetadata metadata = new FlutterExMetadata(rawIdentity.getMetadata());
            FlutterExIdentity flutterExIdentity = new FlutterExIdentity(keystore, wallets, metadata);
            String s = objectMapper.writeValueAsString(flutterExIdentity);
            result.success((s));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            result.error(ErrorCode.ERROR, e.getMessage(), null);
        }
    }

    private ExWallet mapKeystore2Wallet(String keystoreJson, String password) throws Exception {
        if (KeystoreUtil.isIdentityKeystore(objectMapper,keystoreJson)) {
            throw new IllegalArgumentException("do not allow export identity keystore 2 private key");
        }
        ExWallet wallet = null;
        if (KeystoreUtil.isHDMnemonicKeystore(objectMapper,keystoreJson)) {
            ExHDMnemonicKeystore keystore = objectMapper.readValue(keystoreJson, ExHDMnemonicKeystore.class);
            wallet = new ExWallet(keystore);
        }
        if (KeystoreUtil.isV3Keystore(objectMapper,keystoreJson)) {
            V3Keystore keystore = objectMapper.readValue(keystoreJson, V3Keystore.class);
            wallet = new ExWallet(keystore);
        }
        if (wallet == null) {
            throw new IllegalArgumentException("unrecognized keystore content");
        }
        return wallet;
    }


    private boolean isArgumentIllegal(MethodCall call, Result result) {
        if (!(call.arguments instanceof Map)) {
            result.error(ErrorCode.ARGS_ERROR, String.format("arguments in %s method type error.need map", call.method), null);
            return true;
        }
        return false;
    }

    private boolean isArgumentValid(ArgsValid args, MethodCall call, Result result) {
        if (!args.isValid()) {
            result.error(ErrorCode.ARGS_ERROR, String.format("arguments in %s method param error", call.method), null);
            return true;
        }
        return false;
    }

}
