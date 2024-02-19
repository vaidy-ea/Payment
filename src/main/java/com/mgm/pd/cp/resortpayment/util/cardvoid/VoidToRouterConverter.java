package com.mgm.pd.cp.resortpayment.util.cardvoid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterRequestJson;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static com.mgm.pd.cp.resortpayment.constant.ApplicationConstants.SHIFT4_GATEWAY_ID;
import static com.mgm.pd.cp.resortpayment.constant.ApplicationConstants.VOID_OPERATION;

@Component
@AllArgsConstructor
public class VoidToRouterConverter implements Converter<CPPaymentCardVoidRequest, RouterRequest> {
    ObjectMapper mapper;
    @Override
    public RouterRequest convert(CPPaymentCardVoidRequest source) {
        CardVoidRouterRequestJson requestJson = CardVoidRouterRequestJson.builder()
                .amount(source.getAmount())
                .taxAmount(source.getTaxAmount())
                .totalAuthAmount(source.getTotalAuthAmount())
                .guestName(source.getGuestName())
                .dccAmount(source.getDccAmount())
                .dccControlNumber(source.getDccControlNumber())
                .dCCFlag(source.getDccFlag())
                .binCurrencyCode(source.getBinCurrencyCode())
                .binRate(source.getBinRate())
                .uniqueID(source.getUniqueID())
                .cardNumber(source.getCardNumber())
                .cardExpirationDate(source.getCardExpirationDate())
                .cardPresent(source.getCardPresent())
                .cardType(source.getCardType())
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
                .messageResend(source.getMessageResend())
                .departureDate(source.getDepartureDate())
                .resvNameID(source.getResvNameID())
                .roomNum(source.getRoomNum())
                .roomRate(source.getRoomRate())
                .arrivalDate(source.getArrivalDate())
                .vendorTranID(source.getVendorTranID())
                .sequenceNumber(source.getSequenceNumber())
                .originalAuthSequence(source.getOriginalAuthSequence())
                .transDate(source.getTransDate())
                .approvalCode(source.getApprovalCode())
                .messageType(source.getMessageType())
                .installments(source.getInstallments())
                .clientID(source.getClientID())
                .corelationId(source.getCorelationId())
                .incrementalAuthInvoiceId(source.getIncrementalAuthInvoiceId())
                .dateTime(source.getDateTime())
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
