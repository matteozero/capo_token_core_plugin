package com.capo.capo_token_core_plugin.args;


import com.lhalcyon.tokencore.foundation.utils.MnemonicUtil;
import com.lhalcyon.tokencore.wallet.ex.Network;
import com.lhalcyon.tokencore.wallet.ex.SegWit;

import java.util.Arrays;
import java.util.List;

public class RecoverIdentityArgs implements ArgsValid{

    public String password;

//    public String network;
//
//    public String segWit;

    public String mnemonic;



    @Override
    public boolean isValid() {
        try {
            String[] split = mnemonic.split(" ");
            List<String> mnemonic = Arrays.asList(split);
            MnemonicUtil.validateMnemonics(mnemonic);
            return password != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
