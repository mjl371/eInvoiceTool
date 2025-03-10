package com.einvoice.utils;

import java.math.BigDecimal;

import org.apache.commons.lang3.math.NumberUtils;

public class StringUtils {

    public static String replace(String str) {
        return str.replaceAll(" ", "").replaceAll("　", "").replaceAll("：", ":").replaceAll(" ", "");
    }

    public static BigDecimal toBigDecimal(String str) {
    
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        return NumberUtils.isCreatable(str)?NumberUtils.createBigDecimal(str):null;
    }
}
