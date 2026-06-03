package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SepayTransactionResponse {
    private Integer status;
    private String error;
    private Messages messages;
    private List<TransactionData> transactions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Messages {
        private Boolean success;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionData {
        private String id;

        @JsonProperty("bank_brand_name")
        private String bankBrandName;

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("transaction_date")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionDate;

        @JsonProperty("amount_out")
        private BigDecimal amountOut;

        @JsonProperty("amount_in")
        private BigDecimal amountIn;

        private BigDecimal accumulated;

        @JsonProperty("transaction_content")
        private String transactionContent;  // Đây là nội dung chuyển khoản

        @JsonProperty("reference_number")
        private String referenceNumber;

        private String code;

        @JsonProperty("sub_account")
        private String subAccount;

        @JsonProperty("bank_account_id")
        private String bankAccountId;
    }
}