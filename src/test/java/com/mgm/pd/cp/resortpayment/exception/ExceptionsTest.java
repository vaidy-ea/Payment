package com.mgm.pd.cp.resortpayment.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgm.pd.cp.payment.common.dto.CPPaymentCardVoidRequest;
import com.mgm.pd.cp.resortpayment.service.payment.CPPaymentProcessingService;
import com.mgm.pd.cp.resortpayment.util.TestHelperUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.mgm.pd.cp.resortpayment.controller.CPPaymentProcessingControllerTest.VOID_PATH;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionsTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    CPPaymentProcessingService cpPaymentProcessingService;

    @Test
     void shouldThrowNullPointerException() throws Exception {
        //given
        CPPaymentCardVoidRequest mockRequest = TestHelperUtil.getVoidPaymentRequest();
        HttpHeaders headers = TestHelperUtil.getHeaders();
        Mockito.when(cpPaymentProcessingService.processCardVoidRequest(mockRequest, headers)).thenThrow(NullPointerException.class);
        //when
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(VOID_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(mockRequest))
                .headers(headers);
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().is5xxServerError()).andReturn();
        //then
        Assertions.assertInstanceOf(NullPointerException.class, mvcResult.getResolvedException());
    }
}
