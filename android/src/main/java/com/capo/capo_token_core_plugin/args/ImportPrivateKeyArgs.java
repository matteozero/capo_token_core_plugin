package com.capo.capo_token_core_plugin.args;
import com.lhalcyon.tokencore.wallet.validators.PrivateKeyValidator;

import com.lhalcyon.tokencore.wallet.ex.ChainType;
import com.lhalcyon.tokencore.wallet.ex.Network;
import com.lhalcyon.tokencore.wallet.ex.SegWit;
import com.lhalcyon.tokencore.wallet.validators.WIFValidator;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

public class ImportPrivateKeyArgs implements ArgsValid {

    public String privateKey;

    public String password;

//    public String network;
//
//    public String segWit;
//
//    public String chainType;

    @Override
    public boolean isValid() {
        // check enums
        boolean enumValid = true;

        // check privateKey
        boolean pkValid = true;
        PrivateKeyValidator privateKeyValidator = new PrivateKeyValidator(privateKey);
        try {
            privateKeyValidator.validate();
        } catch (Exception e) {
            e.printStackTrace();
            pkValid = false;
        }
        return password != null && enumValid && pkValid;
    }
}
