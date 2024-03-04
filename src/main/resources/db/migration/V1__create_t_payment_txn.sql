CREATE TABLE t_payment_txn (
  payment_id VARCHAR(255) NOT NULL,
   reference_id VARCHAR(50) NULL,
   group_id VARCHAR(50) NULL,
   gateway_rrn VARCHAR(50) NULL,
   gateway_chain_id VARCHAR(50) NULL,
   client_reference_number VARCHAR(50) NULL,
   amount DOUBLE NULL,
   authorized_amount DOUBLE NULL,
   auth_chain_id BIGINT NULL,
   gateway_id VARCHAR(4) NULL,
   client_id VARCHAR(50) NULL,
   order_type VARCHAR(50) NULL,
   mgm_id VARCHAR(50) NULL,
   mgm_token VARCHAR(50) NULL,
   card_holder_name VARCHAR(50) NULL,
   tender_type VARCHAR(50) NULL,
   tender_category VARCHAR(50) NULL,
   issuer_type VARCHAR(50) NULL,
   currency_code VARCHAR(50) NULL,
   last_4_digits_of_the_card VARCHAR(50) NULL,
   billing_address_1 VARCHAR(50) NULL,
   billing_address_2 VARCHAR(50) NULL,
   billing_city VARCHAR(50) NULL,
   billing_zipcode VARCHAR(10) NULL,
   billing_state VARCHAR(20) NULL,
   billing_country VARCHAR(20) NULL,
   gateway_auth_code VARCHAR(50) NULL,
   clerk_id VARCHAR(50) NULL,
   transaction_type VARCHAR(20) NULL,
   transaction_status VARCHAR(50) NULL,
   gateway_reason_code VARCHAR(5) NULL,
   gateway_reason_description VARCHAR(500) NULL,
   gateway_response_code VARCHAR(40) NULL,
   gateway_auth_source VARCHAR(40) NULL,
   deferred_auth VARCHAR(40) NULL,
   created_timestamp datetime NULL,
   updated_timestamp datetime NULL,
   created_by VARCHAR(50) NULL,
   updated_by VARCHAR(50) NULL,
   correlation_id VARCHAR(50) NULL,
   journey_id VARCHAR(50) NULL,
   txn_session_id VARCHAR(50) NULL,
   card_entry_mode VARCHAR(20) NULL,
   avs_response_code VARCHAR(20) NULL,
   cvv_response_code VARCHAR(20) NULL,
   dcc_flag VARCHAR(1) NULL,
   dcc_control_number VARCHAR(20) NULL,
   dcc_amount VARCHAR(20) NULL,
   dcc_bin_rate VARCHAR(20) NULL,
   dcc_bin_currency VARCHAR(3) NULL,
   processor_status_code VARCHAR(20) NULL,
   processor_status_message VARCHAR(50) NULL,
   processor_auth_code VARCHAR(20) NULL,
   auth_subtype VARCHAR(20) NULL,
   CONSTRAINT pk_t_payment_txn PRIMARY KEY (payment_id)
);