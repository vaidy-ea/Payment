package com.mgm.pd.cp.resortpayment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalAuthorizationRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.RefundRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Optional;

public class TestHelperUtil {
	ObjectMapper mapper;

	@Value("classpath:initialAuthRequest.json")
	Resource testFile;

	public static CPPaymentIncrementalAuthRequest getIncrementalAuthRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC2/incrementalAuthRequest.json").getFile(),
				CPPaymentIncrementalAuthRequest.class);
	}

	public static RouterResponseJson getIncrementalRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021041509:18:23\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
        return RouterResponseJson.builder().responseJson(mockResponse).build();
	}

	public static CPPaymentCaptureRequest getCapturePaymentRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC4/capturePaymentRequest.json").getFile(),
				CPPaymentCaptureRequest.class);
	}

	public static RouterResponseJson getCaptureRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021-04-15T00:00:00.000-07:00\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK684Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
    }

	public static CPPaymentCardVoidRequest getVoidPaymentRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC22/voidPaymentRequest.json").getFile(),
				CPPaymentCardVoidRequest.class);
	}

	public static RouterResponseJson getVoidRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}

	public static IncrementalAuthorizationRouterResponse getIncrementalRouterResponse() throws JsonProcessingException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(getIncrementalRouterResponseJson().getResponseJson(), IncrementalAuthorizationRouterResponse.class);
	}

	public static String getOperaResponse() {
		return "{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"gatewayInfo\":{},\"transactionDateTime\":\"2021041509:18:23\",\"transactionAmount\":{\"balanceAmount\":500.0,\"requestedAmount\":300.0,\"cumulativeAmount\":898.07,\"currencyIndicator\":\"USD\",\"detailedAmount\":{}},\"card\":{\"cardType\":\"VS\",\"sequenceNumber\":\"1234\",\"isTokenized\":false},\"printDetails\":[{}]}";
	}

	public static Optional<Payment> getInitialPayment() throws IOException {
		Payment value = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("Payments/initialPayment.json").getFile(), Payment.class);
		return Optional.of(value);
    }

	public static String getOperaResponseForCaptureOperation() {
		return "{\"approvalCode\":\"OK684Z\",\"responseCode\":\"A\",\"gatewayInfo\":{},\"transactionDateTime\":\"2019-08-24T14:15:22Z\",\"transactionAmount\":{\"authorizedAmount\":1500.0,\"cumulativeAmount\":898.07,\"detailedAmount\":{}},\"card\":{\"cardType\":\"VS\",\"sequenceNumber\":\"1234\",\"isTokenized\":false},\"printDetails\":[{}]}";
	}

	public static CPPaymentRefundRequest getRefundPaymentRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC10/refundPaymentRequest.json").getFile(),
				CPPaymentRefundRequest.class);
	}
	public static RouterResponseJson getRefundRouterResponseJson() {
		String mockResponse= "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":100,\"cardType\":\"VS\",\"returnCode\":\"Approved\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021-04-15T00:00:00.000-07:00\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK846Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}
	public static RefundRouterResponse getRefundRouterResponse() throws JsonProcessingException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(getIncrementalRouterResponseJson().getResponseJson(), RefundRouterResponse.class);
	}

	public static String getOperaResponseForRefundOperation() {
		return "{\"approvalCode\":\"OK846Z\",\"responseCode\":\"Approved\",\"gatewayInfo\":{},\"transactionDateTime\":\"2021-04-15T00:00:00.000-07:00\",\"transactionAmount\":{\"balanceAmount\":0.0,\"requestedAmount\":300.0,\"cumulativeAmount\":100.0,\"currencyIndicator\":\"string\",\"detailedAmount\":{}},\"card\":{\"cardType\":\"VS\",\"sequenceNumber\":\"1234\",\"isTokenized\":false},\"printDetails\":[{}]}";
	}
}
