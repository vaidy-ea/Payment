package com.mgm.pd.cp.resortpayment.util.common;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateHelper {
    private static final Logger logger = LogManager.getLogger(DateHelper.class);
    public void logWarningForInvalidTransactionDate(String transactionDateTime) {
        LocalDate transactionDate;
        try{
            transactionDate = ZonedDateTime.parse(transactionDateTime).toLocalDate();
        } catch (DateTimeParseException ex) {
            transactionDate = LocalDateTime.parse(transactionDateTime).toLocalDate();
        }
        LocalDate currentDate = LocalDate.now();
        boolean after = transactionDate.isAfter(currentDate);
        boolean before = transactionDate.isBefore(currentDate);
        if (after || before) {
            logger.log(Level.WARN, "transactionDateTime in request is either Future or Past Date: {}", transactionDate);
        }
    }
}
