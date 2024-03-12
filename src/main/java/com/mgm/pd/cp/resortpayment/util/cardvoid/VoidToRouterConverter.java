package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.opera.Card;
import com.mgm.pd.cp.resortpayment.dto.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.Merchant;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.util.common.PaymentProcessingServiceHelper;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

@Component
@AllArgsConstructor
public class VoidToRouterConverter implements Converter<CPPaymentCardVoidRequest, RouterRequest> {
    private ObjectMapper mapper;
    private PaymentProcessingServiceHelper helper;

    @Override
    public RouterRequest convert(CPPaymentCardVoidRequest source) {
        BaseTransactionDetails baseTransactionDetails = helper.getBaseTransactionDetails(source);
        BaseTransactionDetails transactionDetails = source.getTransactionDetails();
        Card card = transactionDetails.getCard();
        Merchant merchant = transactionDetails.getMerchant();
        String roomRate = helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_RATE);
        String clerkIdentifier = merchant.getClerkIdentifier();
        CardVoidRouterRequestJson requestJson = CardVoidRouterRequestJson.builder()
                /*.amount(detailedAmount.getAmount())
                .taxAmount(detailedAmount.getVat())
                .totalAuthAmount(transactionAmount.getCumulativeAmount())
                .guestName(transactionDetails.getCustomer().getFullName())
                .dccAmount(Double.valueOf(currencyConversion.getAmount()))
                .dccControlNumber(Double.valueOf(conversionIdentifier))
                .dCCFlag(conversionIdentifier)
                .binCurrencyCode(currencyConversion.getBinCurrencyCode())
                .binRate(currencyConversion.getBinCurrencyRate())*/
                .uniqueID(card.getTokenValue())
                .cardNumber(card.getMaskedCardNumber())
                .cardExpirationDate(card.getExpiryDate())
                //.cardPresent(BooleanValue.getEnumByString(transactionDetails.getIsCardPresent().toString()))
                .cardType(card.getCardType())
                .trackData(card.getTrack1())
                //.trackLength(source.getTrackLength())
                //.trackIndicator(source.getTrackIndicator())
                .startDate(card.getStartDate())
                .issueNumber(Integer.valueOf(card.getSequenceNumber()))
                //.usageType(source.getUsageType())
                .propertyCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_IDENTIFIER))
                .chainCode(helper.getValueFromSaleDetails(baseTransactionDetails, PROPERTY_CHAIN_IDENTIFIER))
                .merchantID(merchant.getMerchantIdentifier())
                .version(merchant.getVersion())
                .workstation(merchant.getTerminalIdentifier())
                //.messageResend(source.getMessageResend())
                .departureDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_OUT_DATE))
                .resvNameID(transactionDetails.getSaleItem().getSaleReferenceIdentifier())
                .roomNum(helper.getValueFromSaleDetails(baseTransactionDetails, ROOM_NUMBER))
                .roomRate(!roomRate.equals(NULL) ? Double.valueOf(roomRate) : null)
                .arrivalDate(helper.getValueFromSaleDetails(baseTransactionDetails, CHECK_IN_DATE))
                .vendorTranID(source.getGatewayInfo().getGatewayTransactionIdentifier())
                .sequenceNumber(source.getTransactionIdentifier())
                .originalAuthSequence(Long.valueOf(source.getOriginalTransactionIdentifier()))
                .transDate(source.getTransactionDateTime())
                //.approvalCode(source.getApprovalCode())
                //.messageType(source.getTransactionType())
                //.installments(source.getInstallments())
                .clientID(source.getClientID())
                .corelationId(source.getCorelationId())
                //.incrementalAuthInvoiceId(source.getAuthChainId())
                .dateTime(String.valueOf(LocalDateTime.now()))
                .clerkId(Objects.nonNull(clerkIdentifier) ? Long.valueOf(clerkIdentifier) : null)
                .build();

        String requestJsonAsString;
        try {
            requestJsonAsString = mapper.writeValueAsString(requestJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return RouterRequest.builder()
                .operation(VOID_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}
