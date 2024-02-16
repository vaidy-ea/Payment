package com.mgm.pd.cp.resortpayment.dto.error;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
    private String type;
    private int status;
    private String title;
    private String detail;
    private String instance;
    private String errorCode;
    private List<String> messages;
}
