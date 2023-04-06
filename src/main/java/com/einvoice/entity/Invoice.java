package com.einvoice.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;

/****
 * @description: 发票实体类
 * @author: mjl
 */
@Data
public class Invoice {

    /**
     * 发票标题
     */
    @ExcelProperty("发票标题")
    private String title;

    /**
     * 机器编号
     */
    @ExcelProperty("机器编号")
    private String machineNumber;

    /**
     * 票据代码
     */
    @ExcelProperty("发票代码")
    private String code;

    /**
     * 票据号码
     */
    @ExcelProperty("发票号码")
    private String number;

    /**
     * 开票日期
     */
    @ExcelProperty("开票日期")
    private String date;

    /**
     * 校验码
     */
    @ExcelProperty("校验码")
    private String checkCode;

    /**
     * 购方名称
     */
    @ExcelProperty("购买方名称")
    private String buyerName;

    /**
     * 购方纳税人识别号
     */
    @ExcelProperty("购买方纳税识别号")
    private String buyerCode;

    /**
     * 购方地址、电话
     */
    @ExcelProperty("购买方地址")
    private String buyerAddress;

    /**
     * 购方开户行及账号
     */
    @ExcelProperty("购买方开户行及账号")
    private String buyerAccount;

    /**
     * 密码区
     */
    @ExcelProperty("密码区")
    private String password;

    /**
     * 合计金额
     */
    @ExcelProperty("金额")
    private BigDecimal amount;

    /**
     * 合计税额
     */
    @ExcelProperty("税额")
    private BigDecimal taxAmount;

    /**
     * 价税合计(大写)
     */
    @ExcelProperty("价税合计(大写) ")
    private String totalAmountZH;

    /**
     * 价税合计(小写)
     */
    @ExcelProperty("价税合计(小写) ")
    private BigDecimal totalAmount;

    /**
     * 销方名称
     */
    @ExcelProperty("销售方名称")
    private String sellerName;

    /**
     * 销方纳税人识别号
     */
    @ExcelProperty("销售方纳税识别号")
    private String sellerCode;

    /**
     * 销方地址、电话
     */
    @ExcelProperty("销售方地址")
    private String sellerAddress;

    /**
     * 销方开户行及账号
     */
    @ExcelProperty("销售方开户行及账号")
    private String sellerAccount;

    /**
     * 收款人
     */
    @ExcelProperty("收款人")
    private String payee;

    /**
     * 复核
     */
    @ExcelProperty("复核人")
    private String reviewer;

    /**
     * 开票人
     */
    @ExcelProperty("开票人")
    private String drawer;

    /**
     * 票据类型：普通发票、专用发票
     */
    @ExcelProperty("发票类型")
    private String type;

    /**
     * 票据明细
     */
    @ExcelIgnore
    private List<Detail> detailList;

    /**
     * 明细
     */
    @Data
    public static class Detail {

        /**
         * 货物或应税劳务、服务名称
         */
        private String name;

        /**
         * 规格型号
         */
        private String model;

        /**
         * 单位
         */
        private String unit;

        /**
         * 数量
         */
        private BigDecimal count;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税额
         */
        private BigDecimal taxAmount;

    }
}


