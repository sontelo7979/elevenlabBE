package com.example.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SepayConfig {

    @Value("${sepay.api.url:https://my.sepay.vn/userapi}")
    private String apiUrl;

    @Value("${sepay.api.token:}")
    private String apiToken;

    @Value("${sepay.bank.account.number:03957047401}")
    private String bankAccountNumber;

    @Value("${sepay.bank.code:TPBank}")
    private String bankCode;

    @Value("${sepay.qr.template:compact2}")
    private String qrTemplate;
}