package edu.zsc.ai.domain.model.dto.response.sys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Boolean verified;
    private String authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
