package com.px.service.wechat;

import com.alibaba.fastjson2.JSONObject;
import com.px.entity.miniapp.Invoice;
import com.px.entity.wechat.*;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.refund.model.Refund;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

/**
 * @author 品讯科技
 * @description
 * @date 2024-08
 */
public interface WxPayService {
    void applyWxRefund(String reason);

    void wxRefund(long refundLogId);

    Refund queryByOutRefundNo(String outRefundNo);

    @SneakyThrows
    ResponseEntity<Object> wxRefundNotify(HttpServletRequest request);

    PrepayWithRequestPaymentResponse wxPay(JSONObject rechargeAmount);

    ResponseEntity<Object> wxNotify(HttpServletRequest request) throws IOException;


    Object devConfig();

    void cardTemplate();

    Object getDevConfig();

    //================================================================发票=====================================================================
    TitleUrl titleUrl(Invoice invoice);

    void titleWriteNotice(Object[] notifyRes);

    FaPiao.BuyerInformation userTitle(String applyId);

    InvoiceBaseInfo baseInformation();

    void fapiaoApplication(String invoiceId);

    void wxInvoiceNotify(Object[] notifyRes);

    void invoiceNotify(HttpServletRequest request);

    Map<String, String> downloadInvoice(String invoiceId);

    FapiaoDownload getFapiaoDownloadInfo(String applyId);

    FapiaoApplications queryFapiao(String applyId);
}
