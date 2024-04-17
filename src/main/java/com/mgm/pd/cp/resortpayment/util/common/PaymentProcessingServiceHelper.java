package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.CardType;
import com.mgm.pd.cp.payment.common.constant.OrderType;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.dto.GenericResponse;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.config.HeaderConfigProperties;
import com.mgm.pd.cp.resortpayment.dto.CPPaymentProcessingRequest;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.resortpayment.dto.common.SaleDetails;
import com.mgm.pd.cp.resortpayment.dto.common.SaleItem;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.FAILURE_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.SUCCESS_MESSAGE;
import static com.mgm.pd.cp.payment.common.constant.ReturnCode.Approved;
import static com.mgm.pd.cp.payment.common.util.CommonService.throwExceptionIfRequiredHeadersAreMissing;

/**
 * Helper class for utility methods
 */
@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    public static final String LEADING_ZEROES = "^0+(?!$)";
    private static final Logger logger = LogManager.getLogger(PaymentProcessingServiceHelper.class);
    private FindPaymentService findPaymentService;
    private ObjectMapper mapper;
    private Converter converter;
    private HeaderConfigProperties headerConfigProperties;

    /**
     * This method is helping to find the complete sale details object at once from the request
     *
     * @param transactionDetails: to fetch SaleDetails Object
     */
    public LinkedHashMap<String, String> getSaleDetailsObject(BaseTransactionDetails transactionDetails) {
        if (Objects.nonNull(transactionDetails)) {
            SaleItem saleItem = transactionDetails.getSaleItem();
            if (Objects.nonNull(saleItem)) {
                String saleType = saleItem.getSaleType();
                SaleDetails saleDetails = saleItem.getSaleDetails();
                if (Objects.nonNull(saleType) && Objects.nonNull(saleDetails)) {
                    OrderType orderType = OrderType.valueOf(saleType);
                    LinkedHashMap<String, String> map = null;
                    switch (orderType) {
                        case Hotel : map = mapper.convertValue(saleDetails.getHotel(), new TypeReference<>() {}); break;
                        case Ticket : map = mapper.convertValue(saleDetails.getTicket(), new TypeReference<>() {}); break;
                        default: map = new LinkedHashMap<>();
                    }
                    if (!map.isEmpty()) return map;
                }
            }
        }
        return null;
    }

    /**
     * Method to convert a positive response from Payment DB for Opera
     * @param payment: data from Payment DB
     */
    public <T> ResponseEntity<GenericResponse> response(Payment payment, T request) {
        logger.log(Level.INFO, "Client Id is: " + payment.getClientId() + " Response Code from Router is: " + payment.getGatewayResponseCode());
        OperaResponse operaResponse;
        //converting the response from Payment DB for Opera
        operaResponse = converter.convert(payment, request);
        return response(operaResponse);
    }

    //internal method to add complete data in the response payload
    private <D> ResponseEntity<GenericResponse> response(D data) {
        return new ResponseEntity<>(GenericResponse.builder().data(data).build(), HttpStatus.OK);
    }

    /**
     * This method is responsible to find and return initial Authorization Payment for all types of requests.
     * @param request: any type of Card Payment Request
     * @return Payment details from Payment DB
     */
    public <T> Optional<Payment> getInitialAuthPayment(T request) {
        String authChainId;
        Optional<List<Payment>> paymentDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            authChainId = ((CPPaymentCardVoidRequest) request).getTransactionAuthChainId();
            paymentDetails = findPaymentService.getPaymentDetails(authChainId);
        } else {
            authChainId = ((CPPaymentProcessingRequest) request).getTransactionAuthChainId();
            @Valid AuthType transactionType = ((CPPaymentProcessingRequest) request).getTransactionType();
            if (transactionType == AuthType.DEPOSIT) {
                paymentDetails = findPaymentService.getPaymentDetails(authChainId, transactionType);
            } else {
                paymentDetails = findPaymentService.getPaymentDetails(authChainId);
            }
        }
        if (paymentDetails.isPresent()) {
            List<Payment> payments = paymentDetails.get();
            if (!payments.isEmpty()) {
                return Optional.ofNullable(payments.get(payments.size() - 1));
            }
        }
        logger.log(Level.WARN, "Parent Payment transaction is missing in Payment DB for authChainId: " + authChainId);
        return Optional.empty();
    }

    /**
     * This method is helping to find the complete TransactionDetails object at once from the request
     *
     * @param request: generic for all types of request
     */
    public <T> BaseTransactionDetails getBaseTransactionDetails(T request) {
        BaseTransactionDetails transactionDetails;
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            transactionDetails = ((CPPaymentCardVoidRequest) request).getTransactionDetails();
        } else {
            transactionDetails = ((CPPaymentProcessingRequest) request).getTransactionDetails();
        }
        return transactionDetails;
    }

    /**
     * This method checks for all required Headers in the
     * request and maps it to the CPRequestHeaders class
     * @param request: all types of requests are accepted
     * @param headers: Request Headers
     */
    public <T> T mapHeadersInRequest(T request, HttpHeaders headers) {
        HttpHeaders httpHeaders = removeAutoGeneratedHeaders(headers);
        throwExceptionIfRequiredHeadersAreMissing(httpHeaders);
        return addHeadersInRequest(request, httpHeaders);
    }

    private HttpHeaders removeAutoGeneratedHeaders(HttpHeaders headers) {
        List<String> requiredHeaders = headerConfigProperties.getRequiredHeaders();
        for (String key:  headers.toSingleValueMap().keySet()){
            if (!requiredHeaders.contains(key)) {
                headers.remove(key);
            }
        }
        return headers;
    }

    /**
     * This method is adding headers in the request
     * @param request: request
     * @param headers: headers in the payload
     */
    private <T> T addHeadersInRequest(T request, HttpHeaders headers) {
        CPRequestHeaders cpRequestHeaders = mapper.convertValue(headers.toSingleValueMap(), CPRequestHeaders.class);
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            ((CPPaymentCardVoidRequest) request).setHeaders(cpRequestHeaders);
        } else {
            ((CPPaymentProcessingRequest) request).setHeaders(cpRequestHeaders);
        }
        return request;
    }

    //used for converting to compatible date format
    public LocalDateTime convertToTimestamp(String transactionDateTime) {
        transactionDateTime = transactionDateTime.substring(0, 19);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T']HH:mm:ss['Z']");
        return LocalDateTime.parse(transactionDateTime, formatter);
    }

    //Used to convert cardType received from Request to valid enum value for Intelligent Router and Payment DB
    public String getEnumValueOfCardType(String cardType) throws InvalidFormatException {
        String enumByString = null;
        if (Objects.nonNull(cardType)){
            enumByString = CardType.getEnumByString(replaceSpecialCharacters(cardType));
            if (Objects.isNull(enumByString)) {
                throw new InvalidFormatException("Invalid Value for CardType ", cardType, CardType.class);
            }
        }
        return enumByString;
    }

    private static String replaceSpecialCharacters(String value) {
        return value.replaceAll("\\s+", "_")
                .replace("’", "\\$").replace("'", "\\$");
    }

    public void getIncrementalAuthorizationDetailsFromRouterResponse(CPPaymentIncrementalAuthRequest request, IncrementalAuthorizationRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayTransactionStatusReason(response.getMessage())
                    .gatewayResponseCode(returnCode)
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
    }

    public void getAuthorizationDetailsFromRouterResponse(CPPaymentAuthorizationRequest request, AuthorizationRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authChainId(vendorTranID)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayTransactionStatusReason(response.getMessage())
                    .gatewayResponseCode(returnCode)
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
    }

    public void getCaptureDetailsFromRouterResponse(CPPaymentCaptureRequest request, CaptureRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .gatewayTransactionStatusReason(response.getMessage())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
    }

    public void getVoidDetailsFromRouterResponse(CPPaymentCardVoidRequest request, CardVoidRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .gatewayTransactionStatusReason(response.getMessage())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
    }

    public void getRefundDetailsFromRouterResponse(CPPaymentRefundRequest request, RefundRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .authChainId(vendorTranID)
                    .gatewayTransactionStatusReason(response.getMessage())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE);
        }
    }
}
