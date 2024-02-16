package com.mgm.pd.cp.resortpayment.util.incremental;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static com.mgm.pd.cp.resortpayment.constant.ApplicationConstants.AUTHORIZE_OPERATION;
import static com.mgm.pd.cp.resortpayment.constant.ApplicationConstants.SHIFT4_GATEWAY_ID;

@Component
@AllArgsConstructor
public class IncrementalToRouterConverter implements Converter<CPPaymentIncrementalRequest, RouterRequest> {
    ObjectMapper mapper;

    @Override
    public RouterRequest convert(CPPaymentIncrementalRequest source) {
        IncrementalRouterRequestJson requestJson = IncrementalRouterRequestJson.builder()
                .authorizationAmount(source.getAuthorizationAmount())
                .totalAuthAmount(source.getTotalAuthAmount())
                .currencyIndicator(source.getCurrencyIndicator())
                .guestName(source.getGuestName())
                .billingAddress1(source.getBillingAddress1())
                .billingAddress2(source.getBillingAddress2())
                .billingCity(source.getBillingCity())
                .billingState(source.getBillingState())
                .billingZIP(source.getBillingZIP())
                .dccAmount(source.getDCCAmount())
                .dCCFlag(source.getDCCFlag())
                .binRate(source.getBinRate())
                .uniqueID(source.getUniqueID())
                .binCurrencyCode(source.getBinCurrencyCode())
                .cardNumber(source.getCardNumber())
                .cardExpirationDate(source.getCardExpirationDate())
                .cardPresent(source.getCardPresent())
                .cardType(String.valueOf(source.getCardType()))
                .cID(source.getCid())
                .trackData(source.getTrackData())
                .trackLength(source.getTrackLength())
                .trackIndicator(source.getTrackIndicator())
                .startDate(source.getStartDate())
                .issueNumber(source.getIssueNumber())
                .usageType(source.getUsageType())
                .chainCode(source.getChainCode())
                .propertyCode(source.getPropertyCode())
                .merchantID(source.getMerchantID())
                .version(source.getVersion())
                .workstation(source.getWorkstation())
                .checkOutDate(source.getCheckOutDate())
                .checkInDate(source.getCheckInDate())
                .originDate(source.getOriginDate())
                .resvNameID(source.getResvNameID())
                .roomNum(source.getRoomNum())
                .roomRate(source.getRoomRate())
                .vendorTranID(source.getVendorTranID())
                .balance(source.getBalance())
                .sequenceNumber(source.getSequenceNumber())
                .originalAuthSequence(source.getOriginalAuthSequence())
                .transDate(source.getTransDate())
                .authType(source.getAuthType())
                .aVSStatus(source.getAVSStatus())
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
                .operation(AUTHORIZE_OPERATION)
                .gatewayId(SHIFT4_GATEWAY_ID)
                .requestJson(requestJsonAsString).build();
    }
}
