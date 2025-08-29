package com.dataury.soloJ.domain.user.entity;

import com.dataury.soloJ.domain.user.entity.status.Country;
import com.dataury.soloJ.domain.user.entity.status.Gender;
import com.dataury.soloJ.domain.user.entity.status.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_nick_name", columnNames = "nickName")
        }
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

//    @Column(unique = true, nullable = false)
//    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String nickName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    @Builder.Default
    private int point = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private String imageName;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Country country = Country.KOREA;

    @Enumerated(EnumType.STRING)
    @Column
    private UserType userType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateProfile(String nickName, LocalDate birthDate, Gender gender, UserType userType, String imageName, String imageUrl) {
        this.nickName = nickName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.userType = userType;
        this.imageName = imageName;
        this.imageUrl = imageUrl;

    }

}
