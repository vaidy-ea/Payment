package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.mgm.pd.cp.resortpayment.dto.cardvoid.VoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.OperaResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class VoidRouterToOperaConverter {
    public OperaResponse convert(Payment payment, VoidRouterResponse responseJson) {
        return OperaResponse.builder()
                .cardExpirationDate(payment.getCardExpirationDate())
                .cardNumber(Long.valueOf(payment.getCardNumber()))
                .cardType(String.valueOf(responseJson.getCardType()))
                .merchantID(payment.getMerchantId())
                .settleAmount(responseJson.getSettleAmount())
                .message(responseJson.getMessage())
                .printInfo1(payment.getPrintInfo1())
                .printInfo2(payment.getPrintInfo2())
                .printInfo3(payment.getPrintInfo3())
                .printInfo4(payment.getPrintInfo4())
                .printInfo5(payment.getPrintInfo5())
                .printInfo6(payment.getPrintInfo6())
                .printInfo7(payment.getPrintInfo7())
                .printInfo8(payment.getPrintInfo8())
                .printInfo9(payment.getPrintInfo9())
                .resvNameID(responseJson.getResvNameID())
                .returnCode(String.valueOf(responseJson.getReturnCode()))
                .sequenceNumber(responseJson.getSequenceNumber())
                .transDate(responseJson.getTransDate())
                .transReference(responseJson.getTransReference())
                .uniqueID(responseJson.getUniqueID())
                .vendorTranID(responseJson.getVendorTranID())
                .clientID(responseJson.getClientID())
                .corelationId(responseJson.getCorelationId())
                .build();
    }
}
