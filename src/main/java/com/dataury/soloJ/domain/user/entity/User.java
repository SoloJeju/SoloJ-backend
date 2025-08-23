package com.dataury.soloJ.domain.user.entity;

import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private Long id;

    @Column(unique = true)
    private String kakaoId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;


    // ========= 비즈니스 로직 ========= //

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

}
