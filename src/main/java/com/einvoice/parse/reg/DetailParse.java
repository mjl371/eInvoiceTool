package com.einvoice.parse.reg;

import com.einvoice.entity.Invoice;
import com.einvoice.domain.ParseRequest;
import com.einvoice.parse.AbstractRegularParse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/****
 * @description: 明细区域
 * @author: xj
 * @date: 2021/8/30 17:13
 */
public class DetailParse extends AbstractRegularParse {

    /**
     * 解析发票明细信息
     *
     * @param parseRequest 解析请求对象，包含发票相关信息
     */
    @Override
    public void doParse(ParseRequest parseRequest) {
        // 获取除服务名称外的明细区域文本
        PDFTextStripperByArea mostArea = parseRequest.getMost();
        String[] detailExcludeNameArray = mostArea.getTextForRegion("detailPrice").split("\n");

        List<Invoice.Detail> detailList = new ArrayList<>();
        for (String detailExcludeName : detailExcludeNameArray) {
            // 过滤包含金额、税率、税额等关键字的非法行
            if (detailExcludeName.matches("\\S*(金额|税率|税额|¥|￥)\\S*")) {
                continue;
            }
            Invoice.Detail detail = new Invoice.Detail();
            // 按空格分割每行信息
            String[] itemArray = detailExcludeName.split("\\s+");
            int itemArrayLength = itemArray.length;
            if (itemArrayLength == 2) {
                // 若分割后只有两项，尝试设置金额和税额
                setAmountAndTaxAmount(detail, itemArray[0], itemArray[1]);
            } else if (itemArrayLength > 2) {
                // 若分割后超过两项，按顺序设置各项信息
                setInvoiceDetail(detail, itemArray);
            }
            detailList.add(detail);
        }

        // 设置明细名称
        setDetailName(parseRequest, detailList);
        Invoice invoice = parseRequest.getInvoice();
        // 排除没有识别完整的明细
        excludeDetails(detailList);
        invoice.setDetailList(detailList);
    }

    /**
     * 设置金额和税额
     *
     * @param detail       发票明细对象
     * @param amountStr    金额字符串
     * @param taxAmountStr 税额字符串
     */
    private void setAmountAndTaxAmount(Invoice.Detail detail, String amountStr, String taxAmountStr) {
        if (NumberUtils.isCreatable(amountStr)) {
            detail.setAmount(new BigDecimal(amountStr));
        }
        if (NumberUtils.isCreatable(taxAmountStr)) {
            detail.setTaxAmount(new BigDecimal(taxAmountStr));
        }
    }

    /**
     * 设置发票明细信息
     *
     * @param detail    发票明细对象
     * @param itemArray 分割后的信息数组
     */
    private void setInvoiceDetail(Invoice.Detail detail, String[] itemArray) {
        int itemArrayLength = itemArray.length;
        // 税额
        String taxAmount = itemArray[itemArrayLength - 1];
        if (NumberUtils.isCreatable(taxAmount)) {
            detail.setTaxAmount(new BigDecimal(taxAmount));
        }
        // 税率
        String taxRate = itemArray[itemArrayLength - 2].replaceAll("%", "");
        if (NumberUtils.isCreatable(taxRate)) {
            detail.setTaxRate(new BigDecimal(taxRate));
        }
        // 金额
        String amount = itemArray[itemArrayLength - 3];
        if (NumberUtils.isCreatable(amount)) {
            detail.setAmount(new BigDecimal(amount));
        }
        // 单价
        if (itemArrayLength >= 4 && NumberUtils.isCreatable(itemArray[itemArrayLength - 4])) {
            detail.setPrice(new BigDecimal(itemArray[itemArrayLength - 4]));
        }
        // 数量
        if (itemArrayLength >= 5 && NumberUtils.isDigits(itemArray[itemArrayLength - 5])) {
            detail.setCount(new BigDecimal(itemArray[itemArrayLength - 5]));
        }
        // 单位
        if (itemArrayLength >= 6 && !itemArray[itemArrayLength - 6].matches("^(-?\\d+)(\\.\\d+)?$")) {
            detail.setUnit(itemArray[itemArrayLength - 6]);
        }
        // 规格型号
        if (itemArrayLength >= 7 && !itemArray[itemArrayLength - 7].matches("^(-?\\d+)(\\.\\d+)?$")) {
            detail.setModel(itemArray[itemArrayLength - 7]);
        }
    }

    /**
     * 排除没有识别完整的明细
     *
     * @param detailList 明细列表
     */
    private void excludeDetails(List<Invoice.Detail> detailList) {
        detailList.removeIf(next -> StringUtils.isEmpty(next.getName()));
    }

    /**
     * 设置明细名称
     *
     * @param parseRequest 解析请求对象
     * @param detailList   明细列表
     */
    private void setDetailName(ParseRequest parseRequest, List<Invoice.Detail> detailList) {
        PDFTextStripperByArea detailArea = parseRequest.getDetail();
        PDFTextStripperByArea mostArea = parseRequest.getMost();

        String[] detailNameArray = mostArea.getTextForRegion("detailName").replaceAll("\r", "")
                .split("\n");

        String[] detailArray = detailArea.getTextForRegion("detail").replaceAll("\r", "")
                .split("\n");

        for (int i = 0, j = detailNameArray.length, k = detailArray.length, m = detailList.size(); i < j; i++) {
            String separateName = detailNameArray[i];
            if (i >= k) {
                break;
            }
            if (!detailArray[i].contains(separateName)) {
                continue;
            }
            if (i < m) {
                Invoice.Detail temp = detailList.get(i);
                temp.setName(separateName);
            } else {
                Invoice.Detail temp = new Invoice.Detail();
                temp.setName(separateName);
                detailList.add(temp);
            }
        }
    }

}
