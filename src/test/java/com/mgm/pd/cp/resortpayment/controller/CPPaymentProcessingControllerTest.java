package com.mgm.pd.cp.resortpayment.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mgm.pd.cp.payment.common.constant.AuthType;
import com.mgm.pd.cp.payment.common.constant.TransactionType;
import com.mgm.pd.cp.payment.common.exception.InvalidTransactionAttemptException;
import com.mgm.pd.cp.payment.common.exception.MissingHeaderException;
import com.mgm.pd.cp.payment.common.model.Payment;
import com.mgm.pd.cp.resortpayment.dto.authorize.CPPaymentAuthorizationRequest;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.refund.CPPaymentRefundRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.exception.InvalidTransactionTypeException;
import com.mgm.pd.cp.resortpayment.exception.MissingRequiredFieldException;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import com.mgm.pd.cp.resortpayment.service.payment.CPPaymentProcessingService;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import com.mgm.pd.cp.resortpayment.service.router.RouterClient;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INVALID_REQUEST_PARAMETERS;

@AutoConfigureMockMvc
@SpringBootTest
public class CPPaymentProcessingControllerTest {
    public static final String INCREMENTAL_AUTH_PATH = "/services/paymentprocess/v1/authorize/incremental";
    public static final String AUTHORIZE_PATH = "/services/paymentprocess/v1/authorize";
    public static final String CAPTURE_PATH = "/services/paymentprocess/v1/capture";
    public static final String VOID_PATH = "/services/paymentprocess/v1/void";
    public static final String REFUND_PATH = "/services/paymentprocess/v1/refund";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RouterClient mockRouterClient;
    @Autowired
    private CPPaymentProcessingService cpPaymentProcessingService;
    @MockBean
    private FindPaymentService findPaymentService;
    @Autowired
    PaymentRepository paymentRepository;
    @MockBean
    WebRequest webRequest;
    @BeforeEach
    public void deletePaymentRecord() {
        paymentRepository.deleteAll();
    }
    @AfterEach
    public void deletePaymentRecords() {
        paymentRepository.deleteAll();
    }

    @Test
    void when_throw_MissingHeader_exception() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        //when
        HttpHeaders headers = TestHelperUtil.getHeaders();
        headers.remove("authorization");
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(headers);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(MissingHeaderException.class, mvcResult.getResolvedException());
    }

    @Test
    void when_throw_InvalidTransactionTypeException() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        mockRequest.setTransactionType(AuthType.INIT);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(InvalidTransactionTypeException.class, mvcResult.getResolvedException());
    }

    @Test
    void when_throw_MissingRequiredFieldException() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        mockRequest.setTransactionAuthChainId(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(MissingRequiredFieldException.class, mvcResult.getResolvedException());
    }

    @Test
    void when_throw_MethodArgumentNotValidException() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        mockRequest.setTransactionAuthChainId("12345678910");
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(MethodArgumentNotValidException.class, mvcResult.getResolvedException());
    }

    @Test
    void when_provided_valid_incremental_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_incremental_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponseForIncrementalAuthOperation(), false);
    }

    @Test
    void valid_incremental_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_incremental_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        mockRequest.setTransactionIdentifier(null);
        mockRequest.setTransactionDateTime(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(INVALID_REQUEST_PARAMETERS, JsonPath.read(responseJson, "$.title"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_incremental_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        FeignException mockEx = Mockito.mock(FeignException.class);
        Mockito.when(mockEx.status()).thenReturn(HttpStatus.BAD_REQUEST.value());
        Mockito.when(mockEx.contentUTF8()).thenReturn(TestHelperUtil.getContentUTF8FromFeignException());
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
    }

    @Test
    void when_provided_valid_capture_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK684Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void valid_capture_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_valid_capture_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponseForCaptureOperation(), false);
    }

    @Test
    void when_provided_invalid_capture_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        mockRequest.setTransactionDateTime(null);
        mockRequest.setTransactionIdentifier(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(INVALID_REQUEST_PARAMETERS, JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_capture_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        FeignException mockEx = Mockito.mock(FeignException.class);
        Mockito.when(mockEx.status()).thenReturn(HttpStatus.BAD_REQUEST.value());
        Mockito.when(mockEx.contentUTF8()).thenReturn(TestHelperUtil.getContentUTF8FromFeignException());
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
    }

    @Test
    void when_provided_valid_card_void_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_card_void_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponseForCardVoidOperation(), false);
    }

    @Test
    void valid_card_void_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_card_void_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        mockRequest.setTransactionIdentifier(null);
        mockRequest.setTransactionDateTime(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(INVALID_REQUEST_PARAMETERS, JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_card_void_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        FeignException mockEx = Mockito.mock(FeignException.class);
        Mockito.when(mockEx.status()).thenReturn(HttpStatus.BAD_REQUEST.value());
        Mockito.when(mockEx.contentUTF8()).thenReturn(TestHelperUtil.getContentUTF8FromFeignException());
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("NO INV", JsonPath.read(responseJson, "$.title"));
    }

    @Test
    void when_provided_valid_refund_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentRefundRequest mockRequest = TestHelperUtil.getRefundPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(REFUND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_refund_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentRefundRequest mockRequest = TestHelperUtil.getRefundPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getRefundRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(REFUND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponseForRefundOperation(), false);
    }

    @Test
    void valid_refund_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentRefundRequest mockRequest = TestHelperUtil.getRefundPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getRefundRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(REFUND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_refund_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.any(AuthType.class))).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentRefundRequest mockRequest = TestHelperUtil.getRefundPaymentRequest();
        mockRequest.setTransactionIdentifier(null);
        mockRequest.setTransactionDateTime(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(REFUND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(INVALID_REQUEST_PARAMETERS, JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_refund_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentRefundRequest mockRequest = TestHelperUtil.getRefundPaymentRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        FeignException mockEx = Mockito.mock(FeignException.class);
        Mockito.when(mockEx.status()).thenReturn(HttpStatus.BAD_REQUEST.value());
        Mockito.when(mockEx.contentUTF8()).thenReturn(TestHelperUtil.getContentUTF8FromFeignException());
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(REFUND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("NO INV", JsonPath.read(responseJson, "$.title"));
    }

    @Test
    void when_provided_valid_authorization_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        CPPaymentAuthorizationRequest mockRequest = TestHelperUtil.getAuthorizationRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getAuthorizationRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(AUTHORIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_authorization_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        CPPaymentAuthorizationRequest mockRequest = TestHelperUtil.getAuthorizationRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getAuthorizationRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(AUTHORIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponse(), false);
    }

    @Test
    void valid_authorization_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        CPPaymentAuthorizationRequest mockRequest = TestHelperUtil.getAuthorizationRequest();
        HttpHeaders mockHeaders = TestHelperUtil.getHeaders();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getAuthorizationRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(AUTHORIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(mockHeaders);
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_authorization_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        CPPaymentAuthorizationRequest mockRequest = TestHelperUtil.getAuthorizationRequest();
        mockRequest.setTransactionIdentifier(null);
        mockRequest.setTransactionDateTime(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(AUTHORIZE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals(INVALID_REQUEST_PARAMETERS, JsonPath.read(responseJson, "$.title"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_card_void_payment_payload_but_voi_already_done_then_should_throw_exception() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        Optional<List<Payment>> initialPayment = TestHelperUtil.getInitialPayment();
        initialPayment.get().stream().findFirst().get().setTransactionType(TransactionType.VOID);
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString())).thenReturn(initialPayment);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(InvalidTransactionAttemptException.class, mvcResult.getResolvedException());
    }

    @Test
    void should_throwHttpMessageNotReadableException() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        HttpMessageNotReadableException mockEx = Mockito.mock(HttpMessageNotReadableException.class);
        Mockito.when(mockEx.getCause()).thenReturn(new JsonMappingException("NotValid"));
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(HttpMessageNotReadableException.class, mvcResult.getResolvedException());
    }

    @Test
    void should_throwIllegalArgumentException() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        IllegalArgumentException mockEx = Mockito.mock(IllegalArgumentException.class);
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(IllegalArgumentException.class, mvcResult.getResolvedException());
    }

    @Test
    void should_throwDateTimeParseException() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        DateTimeParseException mockEx = Mockito.mock(DateTimeParseException.class);
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(HttpHeaders.class), ArgumentMatchers.any(RouterRequest.class))).thenThrow(mockEx);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        //then
        Assertions.assertInstanceOf(DateTimeParseException.class, mvcResult.getResolvedException());
    }

    @Test
    void should_throwNoHandlerFoundException() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/vvoid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(TestHelperUtil.getHeaders());
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //then
        Assertions.assertInstanceOf(NoHandlerFoundException.class, mvcResult.getResolvedException());
    }
}
