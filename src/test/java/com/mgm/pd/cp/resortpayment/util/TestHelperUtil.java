package com.mgm.pd.cp.resortpayment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
		return "{\"approvalCode\":\"OK196Z\",\"responseCode\":\"A\",\"gatewayInfo\":{\"gatewayTransactionIdentifier\":\"12345\"},\"transactionDateTime\":\"2019-08-24T14:15:22\",\"transactionAmount\":{\"balanceAmount\":0.0,\"requestedAmount\":898.07,\"authorizedAmount\":898.07,\"cumulativeAmount\":0.0,\"detailedAmount\":{\"amount\":0.0}},\"card\":{\"cardType\":\"null\",\"cardHolderName\":\"ser\",\"isTokenized\":false},\"printDetails\":[{}]}";
	}

	public static Optional<Payment> getInitialPayment() throws IOException {
		Payment value = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("Payments/initialPayment.json").getFile(), Payment.class);
		return Optional.of(value);
    }

	public static String getOperaResponseForCaptureOperation() {
		return "{\n" +
				"    \"responseCode\": \"A\",\n" +
				"    \"approvalCode\": \"OK684Z\",\n" +
				"    \"gatewayInfo\": {\n" +
				"        \"gatewayTransactionIdentifier\": \"12345\"\n" +
				"    },\n" +
				"    \"transactionDateTime\": \"2019-08-24T14:15:22\",\n" +
				"    \"transactionAmount\": {\n" +
				"        \"balanceAmount\": 0.0,\n" +
				"        \"requestedAmount\": 898.07,\n" +
				"        \"authorizedAmount\": 898.07,\n" +
				"        \"cumulativeAmount\": 0.0,\n" +
				"        \"detailedAmount\": {\n" +
				"            \"amount\": 0.0\n" +
				"        }\n" +
				"    },\n" +
				"    \"card\": {\n" +
				"        \"cardType\": \"null\",\n" +
				"        \"cardHolderName\": \"ser\",\n" +
				"        \"isTokenized\": false\n" +
				"    },\n" +
				"    \"printDetails\": [\n" +
				"        {}\n" +
				"    ]\n" +
				"}";
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
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(getIncrementalRouterResponseJson().getResponseJson(), RefundRouterResponse.class);
	}

	public static String getOperaResponseForRefundOperation() {
		return "{\n" +
				"    \"approvalCode\": \"OK846Z\",\n" +
				"    \"responseCode\": \"Approved\",\n" +
				"    \"gatewayInfo\": {\n" +
				"        \"gatewayTransactionIdentifier\": \"12345\"\n" +
				"    },\n" +
				"    \"transactionDateTime\": \"2019-08-24T14:15:22\",\n" +
				"    \"transactionAmount\": {\n" +
				"        \"balanceAmount\": 0.0,\n" +
				"        \"requestedAmount\": 100.0,\n" +
				"        \"authorizedAmount\": 100.0,\n" +
				"        \"cumulativeAmount\": 0.0,\n" +
				"        \"detailedAmount\": {\n" +
				"            \"amount\": 0.0\n" +
				"        }\n" +
				"    },\n" +
				"    \"card\": {\n" +
				"        \"cardType\": \"null\",\n" +
				"        \"cardHolderName\": \"ser\",\n" +
				"        \"isTokenized\": false\n" +
				"    },\n" +
				"    \"printDetails\": [\n" +
				"        {}\n" +
				"    ]\n" +
				"}";
	}

	public static CPPaymentAuthorizationRequest getAuthorizationRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC12/authorizationPaymentRequest.json").getFile(),
				CPPaymentAuthorizationRequest.class);
	}

	public static RouterResponseJson getAuthorizationRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2019-08-24T14:15:22Z\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}
}
