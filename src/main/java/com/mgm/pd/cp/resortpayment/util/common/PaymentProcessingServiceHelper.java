package com.mgm.pd.cp.resortpayment.util.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mgm.pd.cp.payment.common.constant.*;
import com.mgm.pd.cp.payment.common.dto.*;
import com.mgm.pd.cp.payment.common.dto.common.BaseTransactionDetails;
import com.mgm.pd.cp.payment.common.dto.common.SaleDetails;
import com.mgm.pd.cp.payment.common.dto.common.SaleItem;
import com.mgm.pd.cp.payment.common.dto.opera.OperaResponse;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.config.HeaderConfigProperties;
import com.mgm.pd.cp.resortpayment.dto.authorize.AuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.capture.CaptureRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CardVoidRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import com.mgm.pd.cp.resortpayment.util.authorize.AuthorizeValidationHelper;
import com.mgm.pd.cp.resortpayment.util.capture.CaptureValidationHelper;
import com.mgm.pd.cp.resortpayment.util.cardvoid.CardVoidValidationHelper;
import com.mgm.pd.cp.resortpayment.util.incremental.IncrementalAuthorizationValidationHelper;
import com.mgm.pd.cp.resortpayment.util.refund.RefundValidationHelper;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.internal.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;
import static com.mgm.pd.cp.payment.common.constant.ReturnCode.Approved;
import static com.mgm.pd.cp.payment.common.util.CommonService.throwExceptionIfRequiredHeadersAreMissing;

/**
 * Helper class for utility methods
 */
@Component
@AllArgsConstructor
public class PaymentProcessingServiceHelper {
    private static final Logger logger = LogManager.getLogger(PaymentProcessingServiceHelper.class);
    public static final String LEADING_ZEROES = "^0+(?!$)";
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
        return new LinkedHashMap<>();
    }

    /**
     * Method to convert a positive response from Payment DB for Opera
     * @param payment: data from Payment DB
     */
    public <T> ResponseEntity<GenericResponse> response(Payment payment, T request) {
        logger.log(Level.INFO, "Client Id is: {} and Response Code from Router is: {}", payment.getClientId(), payment.getGatewayResponseCode());
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
     * This method is responsible to find and return last initial Authorization Payment for all types of requests.
     * @param paymentDetails: Payment Details
     * @return Payment details from Payment DB
     */
    private Optional<Payment> getLastRecordFromPaymentsList(Pair<Optional<List<Payment>>, String> paymentDetails) {
        Optional<List<Payment>> optionalPaymentList = paymentDetails.getLeft();
        if (optionalPaymentList.isPresent()) {
            List<Payment> payments = optionalPaymentList.get();
            if (!payments.isEmpty()) {
                return Optional.ofNullable(payments.get(payments.size() - 1));
            }
        }
        logger.log(Level.WARN, "Parent Payment transaction is missing in Payment DB for authChainId: {}", paymentDetails.getRight());
        return Optional.empty();
    }

    //Used to get all payments chain on the basis of transactionAuthChainId
    private <T> Pair<Optional<List<Payment>>, String> getAllPayments(T request) {
        String authChainId;
        Optional<List<Payment>> paymentDetails = Optional.empty();
        if (request.getClass().equals(CPPaymentCardVoidRequest.class)) {
            authChainId = ((CPPaymentCardVoidRequest) request).getTransactionAuthChainId();
            if (Objects.nonNull(authChainId)) {
                paymentDetails = findPaymentService.getPaymentDetails(authChainId);
            }
        } else {
            authChainId = ((CPPaymentProcessingRequest) request).getTransactionAuthChainId();
            @Valid AuthType transactionType = ((CPPaymentProcessingRequest) request).getTransactionType();
            if (Objects.nonNull(authChainId)) {
                if (transactionType == AuthType.DEPOSIT) {
                    paymentDetails = findPaymentService.getPaymentDetails(authChainId, transactionType);
                } else {
                    paymentDetails = findPaymentService.getPaymentDetails(authChainId);
                }
            }
        }
        return Pair.of(paymentDetails, authChainId);
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
        throwExceptionIfRequiredHeadersAreMissing(httpHeaders, headerConfigProperties.getOptionalHeaders());
        return addHeadersInRequest(request, httpHeaders);
    }

    private HttpHeaders removeAutoGeneratedHeaders(HttpHeaders headers) {
        List<String> collectedHeaders = Stream.concat(headerConfigProperties.getRequiredHeaders().stream(), headerConfigProperties.getOptionalHeaders().stream()).collect(Collectors.toList());
        for (String key:  headers.toSingleValueMap().keySet()){
            if (!collectedHeaders.contains(key)) {
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
    public static String getEnumValueOfCardType(String cardType) throws InvalidFormatException {
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

    public void getAuthorizationDetailsFromRouterResponse(CPPaymentAuthorizationRequest request, AuthorizationRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            //adding this in request to send back to Upstream System(e.g. Opera/UPOS) as there in no column is there in Payment DB to store this value
            request.setResponseReason(response.getResponseReason());
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    //.gatewayId(Gateway.valueOf(response.getGatewayID()))
                    .authChainId(vendorTranID)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayTransactionStatusCode(response.getMessage())
                    .gatewayTransactionStatusReason(response.getReasonDescription())
                    .gatewayResponseCode(returnCode)
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .avsResponseCode(response.getAvsResult());
        }
    }

    public void getIncrementalAuthorizationDetailsFromRouterResponse(CPPaymentIncrementalAuthRequest request, IncrementalAuthorizationRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            //adding this in request to send back to Upstream System(e.g. Opera/UPOS) as there in no column is there in Payment DB to store this value
            request.setResponseReason(response.getResponseReason());
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authChainId(vendorTranID)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayTransactionStatusCode(response.getMessage())
                    .gatewayTransactionStatusReason(response.getReasonDescription())
                    .gatewayResponseCode(returnCode)
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .avsResponseCode(response.getAvsResult());
        }
    }

    public void getCaptureDetailsFromRouterResponse(CPPaymentCaptureRequest request, CaptureRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            //adding this in request to send back to Upstream System(e.g. Opera/UPOS) as there in no column is there in Payment DB to store this value
            request.setResponseReason(response.getResponseReason());
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authChainId(vendorTranID)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .gatewayTransactionStatusCode(response.getMessage())
                    .gatewayTransactionStatusReason(response.getReasonDescription())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .avsResponseCode(response.getAvsResult());
        }
    }

    public void getVoidDetailsFromRouterResponse(CPPaymentCardVoidRequest request, CardVoidRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            //adding this in request to send back to Upstream System(e.g. Opera/UPOS) as there in no column is there in Payment DB to store this value
            request.setResponseReason(response.getResponseReason());
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    .authChainId(vendorTranID)
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .gatewayTransactionStatusCode(response.getMessage())
                    .gatewayTransactionStatusReason(response.getReasonDescription())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .avsResponseCode(response.getAvsResult());
        }
    }

    public void getRefundDetailsFromRouterResponse(CPPaymentRefundRequest request, RefundRouterResponse response, Payment.PaymentBuilder newPayment) {
        if (Objects.nonNull(response)) {
            //adding this in request to send back to Upstream System(e.g. Opera/UPOS) as there in no column is there in Payment DB to store this value
            request.setResponseReason(response.getResponseReason());
            request.setTransactionDateTime(response.getDateTime());
            String returnCode = Objects.nonNull(response.getReturnCode()) ? response.getReturnCode() : "";
            String vendorTranID = response.getVendorTranID();
            newPayment
                    //.gatewayId(Gateway.valueOf(response.getGatewayID()))
                    .authorizedAmount(response.getTotalAuthAmount())
                    .paymentAuthId(response.getApprovalCode())
                    .gatewayResponseCode(returnCode)
                    .authChainId(vendorTranID)
                    .gatewayTransactionStatusCode(response.getMessage())
                    .gatewayTransactionStatusReason(response.getReasonDescription())
                    .createdTimeStamp(convertToTimestamp(response.getDateTime()))
                    .gatewayChainId(Objects.nonNull(vendorTranID) ? vendorTranID.replaceFirst(LEADING_ZEROES, "") : null)
                    .transactionStatus((returnCode.equals(Approved.name())) ? SUCCESS_MESSAGE : FAILURE_MESSAGE)
                    .avsResponseCode(response.getAvsResult());
        }
    }

    public void validateAuthorizeRequest(CPPaymentAuthorizationRequest request, HttpHeaders headers) throws ParseException {
        AuthorizeValidationHelper.logWarningForInvalidRequestData(request);
        AuthorizeValidationHelper.throwExceptionForInvalidRequest(request);
        AuthorizeValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(getPaymentDetailsByTransactionId(headers));
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = getAllPayments(request);
        AuthorizeValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment);
    }

    public Optional<Payment> validateIncrementalAuthorizationRequestAndReturnInitialPayment(CPPaymentIncrementalAuthRequest request, HttpHeaders headers) throws ParseException {
        IncrementalAuthorizationValidationHelper.throwExceptionForInvalidRequest(request);
        IncrementalAuthorizationValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(getPaymentDetailsByTransactionId(headers));
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = getAllPayments(request);
        IncrementalAuthorizationValidationHelper.logWarningForInvalidRequest(headers, optionalInitialAuthPayment, request);
        IncrementalAuthorizationValidationHelper.throwExceptionForInvalidAttempts(request, optionalInitialAuthPayment);
        return getLastRecordFromPaymentsList(optionalInitialAuthPayment);
    }

    public Optional<Payment> validateCaptureRequestAndReturnInitialPayment(CPPaymentCaptureRequest request, HttpHeaders headers) throws ParseException {
        CaptureValidationHelper.logWarningForInvalidRequestData(request);
        CaptureValidationHelper.throwExceptionForInvalidRequest(request);
        CaptureValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(getPaymentDetailsByTransactionId(headers));
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = getAllPayments(request);
        CaptureValidationHelper.logWarningForInvalidRequest(headers, optionalInitialAuthPayment, request);
        CaptureValidationHelper.throwExceptionForInvalidAttempts(request, optionalInitialAuthPayment);
        return getLastRecordFromPaymentsList(optionalInitialAuthPayment);
    }

    public Optional<Payment> validateCardVoidRequestAndReturnInitialPayment(CPPaymentCardVoidRequest request, HttpHeaders headers) {
        CardVoidValidationHelper.logWarningForInvalidRequestData(request);
        CardVoidValidationHelper.throwExceptionForInvalidRequest(request);
        CardVoidValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(getPaymentDetailsByTransactionId(headers));
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = getAllPayments(request);
        CardVoidValidationHelper.logWarningForInvalidRequest(optionalInitialAuthPayment, request);
        CardVoidValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment);
        return getLastRecordFromPaymentsList(optionalInitialAuthPayment);
    }

    public Optional<Payment> validateRefundRequest(CPPaymentRefundRequest request, HttpHeaders headers) {
        RefundValidationHelper.logWarningForInvalidRequestData(request);
        RefundValidationHelper.throwExceptionForInvalidRequest(request);
        RefundValidationHelper.throwExceptionIfDuplicateTransactionIdUsed(getPaymentDetailsByTransactionId(headers));
        Pair<Optional<List<Payment>>, String> optionalInitialAuthPayment = getAllPayments(request);
        Pair<Optional<Payment>, String> initialPaymentAndApprovalCode = getInitialPaymentByApprovalCode(getPaymentDetailsByApprovalCode(request.getTransactionDetails().getApprovalCode()));
        RefundValidationHelper.throwExceptionForInvalidAttempts(optionalInitialAuthPayment, initialPaymentAndApprovalCode);
        return initialPaymentAndApprovalCode.getLeft();
    }

    private static Pair<Optional<Payment>, String> getInitialPaymentByApprovalCode(Pair<Optional<List<Payment>>, String> initialAuthPaymentByApprovalCode) {
        String approvalCode = initialAuthPaymentByApprovalCode.getRight();
        if (Objects.nonNull(approvalCode)) {
            Optional<List<Payment>> optionalPaymentList = initialAuthPaymentByApprovalCode.getLeft();
            if (optionalPaymentList.isPresent()) {
                List<Payment> payments = optionalPaymentList.get();
                if (!payments.isEmpty()) {
                    Optional<Payment> firstInitialPayment = payments.stream().filter(p -> TransactionType.AUTHORIZE.equals(p.getTransactionType()) && !AuthType.SUPP.equals(p.getAuthSubType())).findFirst();
                    logger.log(Level.INFO, "PaymentId of initialAuth associated with Refund is: {}", firstInitialPayment.get().getPaymentId());
                    return Pair.of(firstInitialPayment, approvalCode);
                }
            }
        }
        return Pair.of(Optional.empty(), approvalCode);
    }

    private Pair<Optional<List<Payment>>, String> getPaymentDetailsByApprovalCode(String approvalCode) {
        if (Objects.nonNull(approvalCode)) {
            return Pair.of(findPaymentService.getPaymentDetailsByApprovalCode(approvalCode), approvalCode);
        }
        return Pair.of(Optional.empty(), null);
    }

    private Optional<List<Payment>> getPaymentDetailsByTransactionId(HttpHeaders headers) {
        Map<String, String> singleValueMap = headers.toSingleValueMap();
        return findPaymentService.getPaymentDetailsByTransactionId(singleValueMap.get(MGM_TRANSACTION_ID));
    }
}
