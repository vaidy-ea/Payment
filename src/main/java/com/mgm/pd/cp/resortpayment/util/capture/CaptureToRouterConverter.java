package com.mgm.pd.cp.resortpayment.util.capture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.constant.CardType;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.payment.common.dto.opera.DetailedAmount;
import com.mgm.pd.cp.payment.common.dto.opera.TransactionAmount;
import com.mgm.pd.cp.resortpayment.constants.BooleanValue;
import com.mgm.pd.cp.resortpayment.dto.CurrencyConversion;
import com.mgm.pd.cp.resortpayment.dto.Merchant;
import com.mgm.pd.cp.resortpayment.dto.TransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.CAPTURE_OPERATION;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SHIFT4_GATEWAY_ID;

@Component
@AllArgsConstructor
public class CaptureToRouterConverter implements Converter<CPPaymentCaptureRequest, RouterRequest> {
    ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentCaptureRequest source) {
        TransactionDetails transactionDetails = source.getTransactionDetails();
        TransactionAmount transactionAmount = transactionDetails.getTransactionAmount();
        DetailedAmount detailedAmount = transactionAmount.getDetailedAmount();
        CurrencyConversion currencyConversion = transactionDetails.getCurrencyConversion();
        String conversionIdentifier = currencyConversion.getConversionIdentifier();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        CaptureRouterRequestJson requestJson = CaptureRouterRequestJson.builder()
                .amount(detailedAmount.getAmount())
                .taxAmount(detailedAmount.getVat())
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .guestName(transactionDetails.getCustomer().getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .dccControlNumber(Double.valueOf(conversionIdentifier))
                .dCCFlag(conversionIdentifier)
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .binRate(currencyConversion.getBinCurrencyRate())
                .uniqueID(card.getTokenValue())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                .cardPresent(BooleanValue.getEnumByString(transactionDetails.getIsCardPresent().toString()))
                .cardType(CardType.valueOf(card.getCardType()))
                .trackData(card.getTrack1())
                //.trackLength(source.getTrackLength())
                //.trackIndicator(source.getTrackIndicator())
                .startDate(card.getStartDate())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                //.usageType(source.getUsageType())
                .propertyCode(helper.getValueByName(source, "propertyIdentifier"))
                .chainCode(helper.getValueByName(source, "propertyChainIdentifier"))
                //.originDate(helper.getValueByName(source, "originDate"))
                .merchantID(merchant.getMerchantIdentifier())
                .version(merchant.getVersion())
                .workstation(merchant.getTerminalIdentifier())
                //.messageResend(source.getMessageResend())
                .departureDate(helper.getValueByName(source, "checkOutDate"))
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .roomNum(helper.getValueByName(source, "roomNumber"))
                .roomRate(Objects.nonNull(helper.getValueByName(source, "roomRate")) ? Double.valueOf(helper.getValueByName(source, "roomRate")) : null)
                .arrivalDate(helper.getValueByName(source, "checkInDate"))
                .vendorTranID(source.getGatewayInfo().getGatewayTransactionIdentifier())
                .sequenceNumber(source.getTransactionIdentifier())
                .originalAuthSequence(Long.valueOf(source.getOriginalTransactionIdentifier()))
                .transDate(source.getTransactionDateTime())
                //.approvalCode(source.getApprovalCode())
                .messageType(source.getTransactionType())
                //.installments(source.getInstallments())
                .clientID(source.getClientID())
                .corelationId(source.getCorelationId())
                .incrementalAuthInvoiceId(source.getIncrementalAuthInvoiceId())
                .dateTime(source.getDateTime())
                .clerkId(source.getClerkId())
                .build();

        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(CAPTURE_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}
