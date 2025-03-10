package com.einvoice.parse.reg;

import com.einvoice.parse.AbstractRegularParse;

/****
 * @description: 销方信息
 * @author: xj
 * @date: 2021/8/30 17:15
 */
public class SellerRegularParse extends AbstractRegularParse {

    @Override
    protected String getRegular() {
        String reg = "名称:(?<sellerName>\\S*)|纳税人识别号:(?<sellerCode>\\S*)|地址、电话:(?<sellerAddress>\\S*)|开户行及账号:(?<sellerAccount>\\S*)";
        //Pattern pattern = Pattern.compile(reg);
        return reg;
    }

    @Override
    protected String getKeyWord() {
        return "seller";
    }

}
