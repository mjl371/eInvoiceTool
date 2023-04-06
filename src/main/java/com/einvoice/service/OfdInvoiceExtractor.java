package com.einvoice.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
// import org.springframework.util.StreamUtils;

import com.einvoice.entity.Invoice;
import com.einvoice.entity.Invoice.Detail;

import cn.hutool.core.io.IoUtil;

/**
 * 专用于处理电子发票识别的类
 * 
 *
 */

public class OfdInvoiceExtractor {

    public static Invoice extract(File file) throws IOException, DocumentException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry entry = zipFile.getEntry("Doc_0/Attachs/original_invoice.xml");
        ZipEntry entry1 = zipFile.getEntry("Doc_0/Pages/Page_0/Content.xml");
        InputStream input = zipFile.getInputStream(entry);
        InputStream input1 = zipFile.getInputStream(entry1);

        // String body = StreamUtils.copyToString(input, Charset.forName("utf-8"));
        // String content = StreamUtils.copyToString(input1, Charset.forName("utf-8"));

        String body = IoUtil.readUtf8(input);
        String content =IoUtil.readUtf8(input1);

        zipFile.close();
        Document document = DocumentHelper.parseText(body);
        Element root = document.getRootElement();
        Invoice invoice = new Invoice();
        invoice.setMachineNumber(root.elementTextTrim("MachineNo"));
        invoice.setCode(root.elementTextTrim("InvoiceCode"));
        invoice.setNumber(root.elementTextTrim("InvoiceNo"));
        invoice.setDate(root.elementTextTrim("IssueDate"));
        invoice.setCheckCode(root.elementTextTrim("InvoiceCheckCode"));
        invoice.setAmount(stringToBigDecimal(root.elementTextTrim("TaxExclusiveTotalAmount")));
        invoice.setTaxAmount(stringToBigDecimal(root.elementTextTrim("TaxTotalAmount")));
        int ind = content.indexOf("圆整</ofd:TextCode>");
        invoice.setTotalAmountZH(content.substring(content.lastIndexOf(">", ind) + 1, ind + 2));
        invoice.setTotalAmount(stringToBigDecimal(root.elementTextTrim("TaxInclusiveTotalAmount")));
        invoice.setPayee(root.elementTextTrim("Payee"));
        invoice.setReviewer(root.elementTextTrim("Checker"));
        invoice.setDrawer(root.elementTextTrim("InvoiceClerk"));
        int index = content.indexOf("</ofd:TextCode>");
        invoice.setTitle(content.substring(content.lastIndexOf(">", index) + 1, index));
        invoice.setType("普通发票");
        if (invoice.getTitle().contains("专用发票")) {
            invoice.setType("专用发票");
        } else if (invoice.getTitle().contains("通行费")) {
            invoice.setType("通行费");
        }
        invoice.setPassword(root.elementText("TaxControlCode"));
        Element buyer = root.element("Buyer");
        {
            invoice.setBuyerName(buyer.elementTextTrim("BuyerName"));
            invoice.setBuyerCode(buyer.elementTextTrim("BuyerTaxID"));
            invoice.setBuyerAddress(buyer.elementTextTrim("BuyerAddrTel"));
            invoice.setBuyerAccount(buyer.elementTextTrim("BuyerFinancialAccount"));
        }
        Element seller = root.element("Seller");
        {
            invoice.setSellerName(seller.elementTextTrim("SellerName"));
            invoice.setSellerCode(seller.elementTextTrim("SellerTaxID"));
            invoice.setSellerAddress(seller.elementTextTrim("SellerAddrTel"));
            invoice.setSellerAccount(seller.elementTextTrim("SellerFinancialAccount"));
        }
        Element details = root.element("GoodsInfos");
        {
            List<Detail> detailList = new ArrayList<>();
            List<Element> elements = details.elements();
            for (Element element : elements) {
                Detail detail = new Detail();
                detail.setName(element.elementTextTrim("Item"));
                detail.setAmount(stringToBigDecimal(element.elementTextTrim("Amount")));
                detail.setTaxAmount(stringToBigDecimal(element.elementTextTrim("TaxAmount")));
                detail.setCount(stringToBigDecimal(element.elementTextTrim("Quantity")));
                detail.setPrice(stringToBigDecimal(element.elementTextTrim("Price")));
                detail.setUnit(element.elementTextTrim("MeasurementDimension"));
                detail.setModel(element.elementTextTrim("Specification"));

                detail.setTaxRate(
                        getTaxRate(element.elementTextTrim("TaxScheme")));

                detailList.add(detail);
            }
            invoice.setDetailList(detailList);
        }
        return invoice;

    }

    public static BigDecimal stringToBigDecimal(String string) {
        if (StringUtils.isBlank(string))
            return null;
        else {
            try {
                return new BigDecimal(string);
            } catch (Exception e) {
                return null;
            }

        }

    }

    public static BigDecimal getTaxRate(String string) {
        if (StringUtils.isBlank(string))
            return null;
        else {
            if (string.contains("免税")) {
                return new BigDecimal(0);
            } else {

                BigDecimal bigDecimal = stringToBigDecimal(string.replace("%", ""));
                if (bigDecimal == null) {
                    return bigDecimal;
                } else {
                    return bigDecimal.divide(new BigDecimal(100));
                }

            }

        }

    }

}