package com.mgm.pd.cp.resortpayment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.IncrementalRouterResponse;
import com.mgm.pd.cp.resortpayment.dto.router.RouterResponseJson;
import com.mgm.pd.cp.resortpayment.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Optional;

public class TestHelperUtil {
	ObjectMapper mapper;

	@Value("classpath:initialAuthRequest.json")
	Resource testFile;

	public static CPPaymentIncrementalRequest getIncrementalAuthRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC2/incrementalAuthRequest.json").getFile(),
				CPPaymentIncrementalRequest.class);
	}

	public static RouterResponseJson getIncrementalRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":162.34,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021-04-15T00:00:00.000-07:00\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK196Z\"}";
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

	public static CPPaymentVoidRequest getVoidPaymentRequest() throws IOException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("UC22/voidPaymentRequest.json").getFile(),
				CPPaymentVoidRequest.class);
	}

	public static RouterResponseJson getVoidRouterResponseJson() {
		String mockResponse = "{\"dateTime\":\"2021-04-15T09:18:23.000-07:00\",\"totalAuthAmount\":898.07,\"cardType\":\"VS\",\"returnCode\":\"A\",\"vendorTranId\":\"0000192029\",\"approvalCode\":\"OK432Z\"}";
		return RouterResponseJson.builder().responseJson(mockResponse).build();
	}

	public static IncrementalRouterResponse getIncrementalRouterResponse() throws JsonProcessingException {
		return new ObjectMapper().registerModule(new JavaTimeModule()).readValue(getIncrementalRouterResponseJson().getResponseJson(), IncrementalRouterResponse.class);
	}

	public static String getOperaResponse() {
		return "{\"message\":\"Success\",\"code\":\"200\",\"data\":{\"authAmountRequested\":162.34,\"binRate\":\"USD\",\"binCurrencyCode\":\"USD\",\"cardExpirationDate\":\"0325\",\"cardNumber\":8048994003381119,\"cardNumberLast4Digits\":325,\"cardType\":\"VS\",\"returnCode\":\"A\",\"sequenceNumber\":\"1234\",\"transDate\":\"2021-04-15T00:00:00.000-07:00\",\"vendorTranID\":\"0000192029\",\"merchantID\":\"merchantID\",\"approvalCode\":\"OK196Z\"}}";
	}

	public static Optional<Payment> getInitialPayment() throws IOException {
		Payment value = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(new ClassPathResource("Payments/initialPayment.json").getFile(), Payment.class);
		return Optional.of(value);
    }
}
