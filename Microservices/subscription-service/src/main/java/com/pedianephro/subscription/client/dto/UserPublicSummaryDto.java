package com.pedianephro.subscription.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Miroir du JSON renvoyé par le MS User ({@code UserPublicSummaryDto}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPublicSummaryDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean active;
}
