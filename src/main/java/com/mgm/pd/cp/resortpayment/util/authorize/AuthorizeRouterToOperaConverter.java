package com.mgm.pd.cp.resortpayment.util.authorize;


import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.OperaResponse;
import com.mgm.pd.cp.resortpayment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class AuthorizeRouterToOperaConverter {
    public OperaResponse convert(Payment payment, AuthorizationRouterResponse responseJson) {
        return OperaResponse.builder()
                .cardType(responseJson.getCardType())
                .resvNameID(responseJson.getResvNameID())
                .returnCode(responseJson.getReturnCode())
                .sequenceNumber(responseJson.getSequenceNumber())
                .transDate(responseJson.getTransDate())
                .vendorTranID(responseJson.getVendorTranID())
                .approvalCode(responseJson.getApprovalCode())
                .authAmountRequested(responseJson.getTotalAuthAmount())
                .binRate(payment.getBinCurrencyCode())
                .binCurrencyCode(payment.getBinCurrencyCode())
                .dccAmount(payment.getDccAmount())
                .cardExpirationDate(payment.getCardExpirationDate())
           //     .cardNumber(Long.valueOf(payment.getCardNumber()))
             //   .cardNumberLast4Digits(Integer.valueOf(payment.getCardExpirationDate()))
                .build();
    }
}
