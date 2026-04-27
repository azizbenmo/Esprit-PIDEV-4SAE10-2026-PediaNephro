package com.nephroforum.dto;

import lombok.*;
import java.time.LocalDate;

public class SearchDTOs {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SearchRequest {
        private String keyword;
        private String tag;
        private LocalDate dateFrom;
        private LocalDate dateTo;
        private Boolean hasImage;
        private Boolean hasResponse;  // a au moins un commentaire
        private Boolean anonymous;
        private String sort;          // RECENT, OLDEST, HOT
        private int page;
        private int size;
    }
}