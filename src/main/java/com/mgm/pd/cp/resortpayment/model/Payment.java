package com.mgm.pd.cp.resortpayment.model;

import com.mgm.pd.cp.resortpayment.constant.AuthType;
import com.mgm.pd.cp.resortpayment.constant.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private Double authAmountRequested;
	private Double authTotalAmount;
	private String binCurrencyCode;
	private String binRate;
	private Double dccAmount;
	private String cardExpirationDate;
	private String cardNumber;
	private Integer cardNumberLast4Digits;
	private String cardType;
	private Integer issueNumber;
	private String message;
	private String printInfo1;
	private String printInfo2;
	private String printInfo3;
	private String printInfo4;
	private String printInfo5;
	private String printInfo6;
	private String printInfo7;
	private String printInfo8;
	private String printInfo9;
	private String resvNameID;
	private String returnCode;
	private String sequenceNumber;
	private String startDate;
	private String transDate;
	private String uniqueID;
	private String vendorTranID;
	private String approvalCode;

	//TODO: need details in payload like Format, length etc
	private String clientID;
	private String corelationId;

	//TODO: Not in Payload
	private Long incrementalAuthInvoiceId;
	private String propertyCode;
	@Enumerated(EnumType.STRING)
	private AuthType authType;
	//TODO: not in UC1 but present in UC2
	private String usageType;
	private String guestName;
	private String currencyIndicator;
	private Double balance;
	private String trackIndicator;
	private String originDate;
	private String merchantId;
	private Double settleAmount;
	private String transReference;
	@Enumerated(EnumType.STRING)
	private TransactionType cpTransactionType;
}
