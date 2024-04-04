package com.mgm.pd.cp.resortpayment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgm.pd.cp.payment.common.constant.MGMChannel;
import com.mgm.pd.cp.payment.common.dto.CPRequestHeaders;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
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
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2019-08-24T14:15:22Z\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
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
		return "{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"transactionAuthChainId\":\"0000192029\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"authorizedAmount\":898.07},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"transactionAuthChainId\":\"0000192029\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"authorizedAmount\":898.07},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}";
	}

	public static Optional<List<Payment>> getInitialPayment() throws IOException {
		Payment value = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("Payments/initialPayment.json").getFile(), Payment.class);
		return Optional.of(Collections.singletonList(value));
    }

	public static String getOperaResponseForCaptureOperation() {
		return "{\"approvalCode\":\"OK684Z\",\"responseCode\":\"A\",\"transactionAuthChainId\":\"0000192029\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"authorizedAmount\":898.07},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}";
	}

	public static CPPaymentRefundRequest getRefundPaymentRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC10_UC11_UC27/refundPaymentRequest.json").getFile(),
				CPPaymentRefundRequest.class);
	}
	public static RouterResponseJson getRefundRouterResponseJson() {
		String mockResponse= "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":100,\"cardType\":\"VS\",\"returnCode\":\"Approved\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021-04-15T00:00:00.000-07:00\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK846Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}
	public static RefundRouterResponse getRefundRouterResponse() throws JsonProcessingException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(getRefundRouterResponseJson().getResponseJson(), RefundRouterResponse.class);
	}

	public static String getOperaResponseForRefundOperation() {
		return "{\"approvalCode\":\"OK846Z\",\"responseCode\":\"Approved\",\"transactionAuthChainId\":\"0000192029\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"authorizedAmount\":100.0},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}";
	}

	public static CPPaymentAuthorizationRequest getAuthorizationRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC12/authorizationPaymentRequest.json").getFile(),
				CPPaymentAuthorizationRequest.class);
	}

	public static RouterResponseJson getAuthorizationRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2019-08-24T14:15:22Z\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}

	public static HttpHeaders getHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("x-mgm-source", "testSource");
		httpHeaders.add("x-mgm-journey-id", "testJourney");
		httpHeaders.add("x-mgm-correlation-id", "testCorrelation");
		httpHeaders.add("x-mgm-transaction-id", "testTransaction");
		httpHeaders.add("x-mgm-channel", MGMChannel.POS.name());
		httpHeaders.add("x-mgm-client-id", "testClient");
		httpHeaders.add("authorization", "testAuthorization");
		return httpHeaders;
	}

	public static CPPaymentIncrementalAuthRequest getIncrementalAuthRequestWithHeaders() throws IOException {
		CPPaymentIncrementalAuthRequest incrementalAuthRequest = getIncrementalAuthRequest();
		incrementalAuthRequest.setHeaders(buildCustomHeaders());
		return incrementalAuthRequest;
	}

	private static CPRequestHeaders buildCustomHeaders() {
		return CPRequestHeaders.builder().channel(MGMChannel.POS).source("testSource").transactionId("testTransaction")
				.journeyId("testJourney").correlationId("testCorrelation").clientId("testClient").authorization("testAuth").build();
	}

	public static CPPaymentRefundRequest getRefundPaymentRequestWithHeaders() throws IOException {
		CPPaymentRefundRequest refundPaymentRequest = getRefundPaymentRequest();
		refundPaymentRequest.setHeaders(buildCustomHeaders());
		return refundPaymentRequest;
	}

	public static String getOperaResponseForCardVoidOperation() {
		return "{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"transactionAuthChainId\":\"12345\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"authorizedAmount\":898.07},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}";
	}

	public static String getOperaResponseForIncrementalAuthOperation() {
		return "{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"transactionAuthChainId\":\"0000192029\",\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"cumulativeAmount\":898.07},\"card\":{\"tokenType\":\"MGM\",\"tokenValue\":\"string\"},\"printDetails\":[{}]}";
	}

	public static String getContentUTF8FromFeignException() {
		return "{\"type\":\"BAD_REQUEST\",\"origin\":\"shift4\",\"status\":400,\"title\":\"NO INV\",\"detail\":\"Invoice Not Found 9100003284 0009263898  ENGINE17CE\",\"instance\":\"/route\",\"errorCode\":\"9815\"}";
	}
}
