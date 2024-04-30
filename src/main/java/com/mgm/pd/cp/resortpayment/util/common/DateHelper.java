package com.mgm.pd.cp.resortpayment.util.common;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.ZonedDateTime;
@UtilityClass
public class DateHelper {
    private static final Logger logger = LogManager.getLogger(DateHelper.class);
    public void logWarningForInvalidTransactionDate(String transactionDateTime) {
        LocalDate transactionDate = ZonedDateTime.parse(transactionDateTime).toLocalDate();
        LocalDate currentDate = LocalDate.now();
        if (transactionDate.isAfter(currentDate) || transactionDate.isBefore(currentDate)) {
            logger.log(Level.WARN, "transactionDateTime in request is either Future or Past Date: {}", transactionDate);
        }
    }
}
