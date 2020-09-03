package com.capo.capo_token_core_plugin.model;

public class FlutterExWallet {

    public FlutterExMetadata metadata;

    public String keystore;

    public String address;

    public FlutterExWallet(FlutterExMetadata metadata, String keystore, String address) {
        this.metadata = metadata;
        this.keystore = keystore;
        this.address = address;
    }
}
