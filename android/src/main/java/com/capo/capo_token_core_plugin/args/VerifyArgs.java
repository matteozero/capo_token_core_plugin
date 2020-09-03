package com.capo.capo_token_core_plugin.args;

public class VerifyArgs implements ArgsValid{

    public String keystore;

    public String password;

    @Override
    public boolean isValid() {
        return null != password;
    }
}
