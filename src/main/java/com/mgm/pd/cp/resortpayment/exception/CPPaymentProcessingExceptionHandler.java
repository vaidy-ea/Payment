package com.mgm.pd.cp.resortpayment.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.mgm.pd.cp.payment.common.dto.ErrorResponse;
import feign.FeignException;
import feign.RetryableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mgm.pd.cp.payment.common.constant.ApplicationConstants.INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE;

@RestControllerAdvice
public class CPPaymentProcessingExceptionHandler {

    public static final String INVALID_REQUEST_PARAMETERS = "Invalid Request Parameters";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).title(INVALID_REQUEST_PARAMETERS)
                .detail(INVALID_REQUEST_PARAMETERS).messages(errors).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateValidationErrors(DateTimeParseException ex) {
        List<String> errors = Collections.singletonList("Invalid date - " + ex.getParsedString());
        return new ResponseEntity<>(ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).title("Invalid Date Parameter/s")
                .detail("Invalid Date Parameter/s").messages(errors).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(JsonProcessingException ex) {
        return new ResponseEntity<>(ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).title("Invalid Json")
                .detail("Invalid Json").messages(Collections.singletonList(ex.getMessage())).build(), HttpStatus.BAD_REQUEST);
    }

    //Used when PPS is unable to connect to Intelligent Router
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorResponse> handleConnectionException(RetryableException ex) {
        return new ResponseEntity<>(ErrorResponse.builder().status(HttpStatus.SERVICE_UNAVAILABLE.value()).title(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE)
                .detail(INTELLIGENT_ROUTER_CONNECTION_EXCEPTION_MESSAGE).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //Used for Validation of Enum Values
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorDetails = "Unacceptable JSON " + ex.getMessage();
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException inavlidFormatEx = (InvalidFormatException) ex.getCause();
            if (inavlidFormatEx.getTargetType() != null && inavlidFormatEx.getTargetType().isEnum()) {
                errorDetails = String.format("Invalid enum value: '%s' for the field: '%s'. The value must be one of: %s.",
                        inavlidFormatEx.getValue(), inavlidFormatEx.getPath().get(inavlidFormatEx.getPath().size() - 1).getFieldName(),
                        Arrays.toString(inavlidFormatEx.getTargetType().getEnumConstants()));
            }
        }
        return new ResponseEntity<>(ErrorResponse.builder().status(HttpStatus.BAD_REQUEST.value()).title(INVALID_REQUEST_PARAMETERS)
                .detail(INVALID_REQUEST_PARAMETERS).messages(Collections.singletonList(errorDetails)).build(), HttpStatus.BAD_REQUEST);
    }

    //Used to catch exception/errors from Intelligent Router
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleIntelligentRouterExceptions(FeignException ex) throws JsonProcessingException {
        ErrorResponse irEx = new ObjectMapper().readValue(ex.contentUTF8(), ErrorResponse.class);
        return new ResponseEntity<>(irEx, Objects.requireNonNull(HttpStatus.resolve(ex.status())));
    }
}
