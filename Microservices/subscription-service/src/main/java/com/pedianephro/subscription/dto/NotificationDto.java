package com.pedianephro.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type;
    private boolean readStatus;
    private LocalDateTime createdAt;
}

