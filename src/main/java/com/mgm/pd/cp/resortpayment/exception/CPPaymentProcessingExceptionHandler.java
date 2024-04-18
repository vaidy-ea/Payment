package com.mgm.pd.cp.resortpayment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import com.mgm.pd.cp.payment.common.exception.CommonException;
import com.mgm.pd.cp.payment.common.exception.MissingHeaderException;
import com.mgm.pd.cp.payment.common.util.MGMErrorCode;
import feign.FeignException;
import feign.RetryableException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.*;

@RestControllerAdvice
public class CPPaymentProcessingExceptionHandler extends CommonException {
    private static final Logger logger = LogManager.getLogger(CPPaymentProcessingExceptionHandler.class);

    //Used to handle exception from SpringBoot in case of missing attributes
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder()
                .type(HttpStatus.BAD_REQUEST.toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_REQUEST_PARAMETERS)
                .detail(INVALID_REQUEST_PARAMETERS)
                .instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(errors).build(), HttpStatus.BAD_REQUEST);
    }

    //Used to handle Date Parsing Exception for invalid dates
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateValidationErrors(DateTimeParseException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        List<String> errors = Collections.singletonList("Invalid date - " + ex.getParsedString());
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder()
                .type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value()).title(INVALID_DATE_PARAMETERS)
                .detail(INVALID_DATE_PARAMETERS).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(errors).build(), HttpStatus.BAD_REQUEST);
    }

    //Used to validate the Json
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(JsonProcessingException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_JSON).detail(INVALID_JSON).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(ex.getMessage())).build(), HttpStatus.BAD_REQUEST);
    }

    //Used when PPS is unable to connect to Intelligent Router
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> handleConnectionException(RetryableException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.INTERNAL_SERVER_ERROR.toString()).status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .title(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).detail(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), true))
                .messages(Collections.singletonList(ex.getMessage())).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Used for Validation of Enum Values
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_REQUEST_PARAMETERS).detail(INVALID_REQUEST_PARAMETERS).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(getErrorDetails(ex))).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        String errorDetails = "Unacceptable JSON -" + ex.getMessage();
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_REQUEST_PARAMETERS).detail(INVALID_REQUEST_PARAMETERS).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(errorDetails)).build(), HttpStatus.BAD_REQUEST);
    }

    //Used to catch exception/errors from Intelligent Router
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleIntelligentRouterExceptions(FeignException ex) throws JsonProcessingException {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        ErrorResponse irEx = new ObjectMapper().readValue(ex.contentUTF8(), ErrorResponse.class);
        return new ResponseEntity<>(irEx, Objects.requireNonNull(HttpStatus.resolve(ex.status())));
    }

    //Used to catch exception in case headers are not present in request
    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeaderException(MissingHeaderException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_REQUEST_PARAMETERS).detail(INVALID_REQUEST_PARAMETERS).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(MISSING_HEADERS_PREFIX + ex.getMessage())).build(), HttpStatus.BAD_REQUEST);
    }

    //used to catch exception when Request URL is not found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundError(NoHandlerFoundException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.NOT_FOUND.toString()).status(HttpStatus.NOT_FOUND.value())
                .title(HttpStatus.NOT_FOUND.getReasonPhrase()).detail(HttpStatus.NOT_FOUND.getReasonPhrase()).instance(ex.getRequestURL())
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(RESOURCE_NOT_FOUND)).build(), HttpStatus.NOT_FOUND);
    }

    //Used to handle NullPointer Exceptions
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundError(NullPointerException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.INTERNAL_SERVER_ERROR.toString()).status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).detail(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.INTERNAL_SERVER_ERROR.value(), false))
                .messages(Collections.singletonList(ex.getStackTrace()[0] + ": " + ex.getMessage())).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequiredField(MissingRequiredFieldException ex, WebRequest request) {
        logger.log(Level.ERROR, EXCEPTION_PREFIX, ex);
        String uri = request.getDescription(false);
        return new ResponseEntity<>(ErrorResponse.builder().type(HttpStatus.BAD_REQUEST.toString()).status(HttpStatus.BAD_REQUEST.value())
                .title(INVALID_REQUEST_PARAMETERS).detail(INVALID_REQUEST_PARAMETERS).instance(uri)
                .errorCode(MGMErrorCode.getMgmErrorCode(MGMErrorCode.getServiceCodeByMethodURI(uri), HttpStatus.BAD_REQUEST.value(), false))
                .messages(Collections.singletonList(ex.getMessage())).build(), HttpStatus.BAD_REQUEST);
    }

    private static String getErrorDetails(HttpMessageNotReadableException ex) {
        String errorDetails = "Unacceptable JSON " + ex.getMessage();
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
            if (invalidFormatException.getTargetType() != null && invalidFormatException.getTargetType().isEnum()) {
                errorDetails = String.format("Invalid enum value: '%s' for the field: '%s'. The value must be one of: %s.",
                        invalidFormatException.getValue(), invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getFieldName(),
                        Arrays.toString(invalidFormatException.getTargetType().getEnumConstants()));
            }
        }
        if (cause instanceof JsonMappingException && !(cause instanceof InvalidFormatException)) {
            JsonMappingException invalidBooleanInJson = (JsonMappingException) cause;
            String errorFields = invalidBooleanInJson.getPath().stream().map(JsonMappingException.Reference::getFieldName).collect(Collectors.joining("."));
            errorDetails = invalidBooleanInJson.getOriginalMessage() + " for attribute -> " + errorFields;
        }
        return errorDetails;
    }
}
