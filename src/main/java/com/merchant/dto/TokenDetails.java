package com.merchant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenDetails {
    private String token;
    private String lastFour;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
}
