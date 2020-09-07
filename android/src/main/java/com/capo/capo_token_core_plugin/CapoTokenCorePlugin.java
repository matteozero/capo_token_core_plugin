package com.capo.capo_token_core_plugin;


import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.capo.capo_token_core_plugin.args.ExportArgs;
import com.capo.capo_token_core_plugin.args.ImportPrivateKeyArgs;
import com.capo.capo_token_core_plugin.args.RecoverIdentityArgs;
import com.capo.capo_token_core_plugin.args.VerifyArgs;
import com.capo.capo_token_core_plugin.util.KeystoreUtil;

import org.consenlabs.tokencore.wallet.Identity;
import org.consenlabs.tokencore.wallet.KeystoreStorage;
import org.consenlabs.tokencore.wallet.WalletManager;

import java.io.File;
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

    private final Activity activity;


    private CapoTokenCorePlugin(Activity activity) {
        this.activity = activity;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(PluginRegistry.Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "capo_token_core_plugin");
        channel.setMethodCallHandler(new CapoTokenCorePlugin(registrar.activity()));
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method == null) {
            result.error(ErrorCode.CALL_ERROR, "call or call.method is null", call);
            return;
        }
        switch (call.method) {

            case CallMethod.randomMnemonic:
                onRandomMnemonic(result);
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
            case CallMethod.importKeystore:
                onimportKeystore(call, result);
                break;
            case CallMethod.verifyPassword:
                onVerifyPassword(call, result);
                break;


            default:
                result.notImplemented();
                break;
        }
    }



    private void onVerifyPassword(final MethodCall call, final Result result) {
        final Handler mHandler = new Handler();
        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isArgumentIllegal(call, result)) {
                        return;
                    }
                    Object arguments = call.arguments;
                    String json = objectMapper.writeValueAsString(arguments);
                    VerifyArgs args = objectMapper.readValue(json, VerifyArgs.class);
                    boolean isCorrect;
                    if (KeystoreUtil.isIdentityKeystore(objectMapper,args.keystore)) {
                        ExIdentityKeystore identityKeystore = objectMapper.readValue(args.keystore, ExIdentityKeystore.class);
                        isCorrect = identityKeystore.verifyPassword(args.password);
                    } else {
                        ExWallet wallet = mapKeystore2Wallet(args.keystore);
                        isCorrect = wallet.getKeystore().verifyPassword(args.password);
                    }
                    result.success(isCorrect ? "true" : "false");
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error(ErrorCode.ERROR, e.getMessage(), null);
                }

            }
        });

    }



    private void onImportPrivateKey(final MethodCall call, final Result result) {

        final Handler mHandler = new Handler();
        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {

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
                    exMetadata.setWalletType(WalletType.V3);

                    V3Keystore keystore = new V3Keystore(exMetadata, args.password, args.privateKey, "");
                    String keystoreJson = keystore.toJsonString();
                    result.success(keystoreJson);

                } catch (Exception e) {
                    e.printStackTrace();
                    result.error(ErrorCode.IMPORT_ERROR, e.getMessage(), null);
                }
            }
        });


    }


    private void onimportMnenonic(final MethodCall call, final Result result) {
        final Handler mHandler = new Handler();
        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {
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
        });



    }

    private void onimportKeystore(final MethodCall call, final Result result) {

        final Handler mHandler = new Handler();

        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isArgumentIllegal(call, result)) {
                        return;
                    }

                    Object arguments = call.arguments;
                    String json = objectMapper.writeValueAsString(arguments);
                    ExportArgs args = objectMapper.readValue(json, ExportArgs.class);

                    ExWallet wallet = mapKeystore2Wallet(args.keystore);

                    WalletManager.storage = new KeystoreStorage() {
                        @Override
                        public File getKeystoreDir() {
                            return activity.getFilesDir();
                        }
                    };
                    WalletManager.scanWallets();
                    String mnemonic = wallet.exportMnemonic(args.password).getMnemonic();
                    Identity identity = Identity.recoverIdentity(mnemonic,null,args.password, args.password,
                            wallet.getMetadata().getNetwork().getValue(), wallet.getMetadata().getSegWit().getValue());

                    String privateKey = WalletManager.exportPrivateKey(identity.getWallets().get(0).getId(), args.password);
                    ExMetadata exMetadata = new ExMetadata();

                    exMetadata.setFrom(WalletFrom.KEYSTORE);
                    exMetadata.setChainType(ChainType.ETHEREUM);
                    exMetadata.setNetwork(Network.MAINNET);
                    exMetadata.setWalletType(WalletType.V3);

                    V3Keystore keystore = new V3Keystore(exMetadata, args.password, privateKey, "");
                    final String keystoreJson = keystore.toJsonString();

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            result.success(keystoreJson);
                        }
                    });


                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                     result.error(ErrorCode.IMPORT_ERROR, e.getMessage(), null);
                        }
                    });

                }
            }
        });




    }

    private void onExportPrivateKey(final MethodCall call, final Result result) {

        final Handler mHandler = new Handler();
        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isArgumentIllegal(call, result)) {
                        return;
                    }

                    Object arguments = call.arguments;
                    String json = objectMapper.writeValueAsString(arguments);
                    ExportArgs args = objectMapper.readValue(json, ExportArgs.class);

                    ExWallet wallet = mapKeystore2Wallet(args.keystore);

                    WalletManager.storage = new KeystoreStorage() {
                        @Override
                        public File getKeystoreDir() {
                            return activity.getFilesDir();
                        }
                    };
                    WalletManager.scanWallets();
                    String mnemonic = wallet.exportMnemonic(args.password).getMnemonic();
                    Identity identity = Identity.recoverIdentity(mnemonic,null,args.password, args.password,
                            wallet.getMetadata().getNetwork().getValue(), wallet.getMetadata().getSegWit().getValue());

                    String privateKey = WalletManager.exportPrivateKey(identity.getWallets().get(0).getId(), args.password);
                    result.success(privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error(ErrorCode.EXPORT_ERROR, "export error , " + e.getMessage(), null);
                }
            }
        });


    }


    private void onExportMnemonic(final MethodCall call, final Result result) {

        final Handler mHandler = new Handler();
        HandlerThread mBackThread = new HandlerThread("capo_back_thread");
        mBackThread.start();
        Handler mBackHandler = new Handler(mBackThread.getLooper());
        mBackHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isArgumentIllegal(call, result)) {
                        return;
                    }
                    Object arguments = call.arguments;
                    String json = objectMapper.writeValueAsString(arguments);
                    VerifyArgs args = objectMapper.readValue(json, VerifyArgs.class);
                    String mnemonic;
                    if (KeystoreUtil.isIdentityKeystore(objectMapper,args.keystore)) {
                        ExIdentityKeystore identityKeystore = objectMapper.readValue(args.keystore, ExIdentityKeystore.class);
                        mnemonic = identityKeystore.decryptMnemonic(args.password);
                    } else {
                        ExWallet wallet = mapKeystore2Wallet(args.keystore);
                        mnemonic = wallet.exportMnemonic(args.password).getMnemonic();
                    }
                    result.success(mnemonic);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.error(ErrorCode.EXPORT_ERROR, e.getMessage(), null);
                }

            }
        });

    }


    private void onRandomMnemonic(Result result) {

        List<String> strings = MnemonicUtil.randomMnemonicCodes(Words.TWELVE);
        String mnemonic = MnemonicUtil.mnemonicToString(strings);
        result.success(mnemonic);
    }

    private ExWallet mapKeystore2Wallet(String keystoreJson) throws Exception {
        if (KeystoreUtil.isIdentityKeystore(objectMapper,keystoreJson)) {
            throw new IllegalArgumentException("do not allow export identity keystore 2 private key");
        }
        ExWallet wallet = null;

        if (KeystoreUtil.isV3Keystore(objectMapper,keystoreJson)) {
            V3Keystore keystore = objectMapper.readValue(keystoreJson, V3Keystore.class);
            wallet = new ExWallet(keystore);
        }
        if (KeystoreUtil.isHDMnemonicKeystore(objectMapper,keystoreJson)) {
            ExHDMnemonicKeystore keystore = objectMapper.readValue(keystoreJson, ExHDMnemonicKeystore.class);
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
