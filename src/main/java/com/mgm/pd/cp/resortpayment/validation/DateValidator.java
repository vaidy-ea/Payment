package com.mgm.pd.cp.resortpayment.validation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateValidator implements ConstraintValidator<ValidDate, String> {
    private static final Logger logger = LogManager.getLogger(DateValidator.class);
    private String format;

    @Override
    public void initialize(ValidDate validDate) {
        this.format = validDate.format();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return isValidFormat(this.format, value);
    }

    private static boolean isValidFormat(String format, String value) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            if (value != null) {
                date = sdf.parse(value);
                if (!value.equals(sdf.format(date))) {
                    date = null;
                }
            }
        } catch (ParseException ex) {
            logger.log(Level.ERROR, "Unable to parse the date, while validating the field", ex);
        }
        return date != null;
    }
}
