package com.dataury.soloJ.domain.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class RefreshToken {

    @Id
    private Long userId;

    private String refreshToken;

    private LocalDateTime expiryDate;

    public RefreshToken(Long userId, String refreshToken, LocalDateTime expiryDate) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiryDate = expiryDate;
    }


}

