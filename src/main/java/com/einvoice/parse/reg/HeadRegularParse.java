package com.einvoice.parse.reg;


import com.einvoice.entity.Invoice;
import com.einvoice.parse.AbstractRegularParse;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xj
 * @description: 头部信息
 * @date 2021/8/27 16:39
 */
public class HeadRegularParse extends AbstractRegularParse {
    // 使用预编译正则提升性能
    private static final Pattern CODE_FALLBACK_PATTERN = Pattern.compile("通发票(?<code>\\d{12})");
    
    @Override
    protected void check(String fullText, Invoice invoice, Map<String, Field> invoiceField) {
        // 添加方法说明注释
        if (StringUtils.isEmpty(invoice.getCode())) {
            Matcher code = CODE_FALLBACK_PATTERN.matcher(fullText);
            if (code.find()) {
                invoice.setCode(code.group("code"));
            }
        }
    }
}
