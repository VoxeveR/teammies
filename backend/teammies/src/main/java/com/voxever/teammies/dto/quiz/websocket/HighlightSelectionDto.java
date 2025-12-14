package com.voxever.teammies.dto.quiz.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighlightSelectionDto {
    private Long playerId;
    private Long questionId;
    private String selectedOption;
    private Integer selectedIndex;
    private Long timestamp;
}
