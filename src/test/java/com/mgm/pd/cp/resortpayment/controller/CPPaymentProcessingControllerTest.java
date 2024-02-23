package com.mgm.pd.cp.resortpayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mgm.pd.cp.resortpayment.dto.capture.CPPaymentCaptureRequest;
import com.mgm.pd.cp.resortpayment.dto.cardvoid.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.dto.incrementalauth.CPPaymentIncrementalAuthRequest;
import com.mgm.pd.cp.resortpayment.dto.router.RouterRequest;
import com.mgm.pd.cp.resortpayment.repository.PaymentRepository;
import com.mgm.pd.cp.resortpayment.service.payment.CpPaymentProcessingService;
import com.mgm.pd.cp.resortpayment.service.payment.FindPaymentService;
import com.mgm.pd.cp.resortpayment.service.router.RouterClient;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class CPPaymentProcessingControllerTest {
    public static final String INCREMENTAL_AUTH_PATH = "/services/v1/payments/incrementalauth";
    public static final String CAPTURE_PATH = "/services/v1/payments/capture";
    public static final String VOID_PATH = "/services/v1/payments/void";
    @Autowired
    private MockMvc mockMvc;
/*    @Autowired
    Consumer consumer;*/
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private RouterClient mockRouterClient;
    @Autowired
    private CpPaymentProcessingService cpPaymentProcessingService;
    @MockBean
    private FindPaymentService findPaymentService;
    @Autowired
    PaymentRepository paymentRepository;
    @BeforeEach
    public void deletePaymentRecord() {
        paymentRepository.deleteAll();
    }
    @AfterEach
    public void deletePaymentRecords() {
        paymentRepository.deleteAll();
    }

    @Test
    void when_provided_valid_incremental_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_incremental_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponse(), false);
    }

    @Test
    void valid_incremental_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_incremental_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
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
        Assertions.assertEquals("Invalid Request Parameters", JsonPath.read(responseJson, "$.title"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_incremental_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentIncrementalAuthRequest mockRequest = TestHelperUtil.getIncrementalAuthRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getIncrementalRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(INCREMENTAL_AUTH_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("Initial Payment is missing", JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()), JsonPath.read(responseJson, "$.status"));
    }

    @Test
    void when_provided_valid_capture_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK684Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void valid_capture_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_valid_capture_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponseForCaptureOperation(), false);
    }

    @Test
    void when_provided_invalid_capture_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
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
        Assertions.assertEquals("Invalid Request Parameters", JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_capture_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentCaptureRequest mockRequest = TestHelperUtil.getCapturePaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getCaptureRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(CAPTURE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("Initial Payment is missing", JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()), JsonPath.read(responseJson, "$.status"));
    }

    @Test
    void when_provided_valid_card_void_payment_payload_should_process_and_return_approval_code() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("OK196Z", JsonPath.read(responseJson, "$.approvalCode"));
    }

    @Test
    void when_provided_valid_card_void_payment_payload_should_process_and_return_response_to_opera() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        JSONAssert.assertEquals(responseJson, TestHelperUtil.getOperaResponse(), false);
    }

    @Test
    void valid_card_void_intelligent_router_response_should_persist_in_payment_db() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        mockMvc.perform(requestBuilder);
        //then
        Assertions.assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void when_provided_invalid_card_void_payment_payload_should_throw_validation_error_and_return_bad_request() throws Exception {
        //given
        Mockito.when(findPaymentService.getPaymentDetails(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(TestHelperUtil.getInitialPayment());
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        mockRequest.setAmount(null);
        mockRequest.setGuestName(null);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("Invalid Request Parameters", JsonPath.read(responseJson, "$.title"));
        Assertions.assertEquals(Integer.valueOf(HttpStatus.BAD_REQUEST.value()), JsonPath.read(responseJson, "$.status"));
        List<String> errorList = JsonPath.read(responseJson, "$.messages");
        Assertions.assertEquals(2, errorList.size());
    }

    @Test
    void when_provided_valid_card_void_payment_payload_but_initial_payment_is_missing_then_should_not_process_and_return_unsuccessful() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        Mockito.when(mockRouterClient.sendRequest(ArgumentMatchers.any(RouterRequest.class))).thenReturn(TestHelperUtil.getVoidRouterResponseJson());
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is4xxClientError()).andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        //then
        Assertions.assertEquals("Initial Payment is missing", JsonPath.read(responseJson, "$.title"));
    }
}
