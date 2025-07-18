package com.dataury.soloJ.domain.user.converter;


import com.dataury.soloJ.domain.user.dto.AuthRequestDTO;
import com.dataury.soloJ.domain.user.dto.AuthResponseDTO;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.domain.user.entity.status.UserType;

public class AuthConverter {

    public static UserRegistrationData toUser(AuthRequestDTO.SignRequestDTO request, Role role){
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(request.getPassword())
                .role(role)
                .build();

        UserProfile.UserProfileBuilder profileBuilder = UserProfile.builder()
                .nickName(request.getNickName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender());

        if (role == Role.USER) {
            UserType userType = UserType.fromDisplayName(request.getUserType());
            profileBuilder.userType(userType);
        }


        UserProfile userProfile = profileBuilder.build();


        return new UserRegistrationData(user, userProfile);
    }

    // 간단한 데이터 클래스
    public record UserRegistrationData(User user, UserProfile userProfile) {}

    public static AuthResponseDTO.SignResponseDTO toSigninResponseDTO(User user){
        return AuthResponseDTO.SignResponseDTO.builder()
                .name(user.getName())
                .role(user.getRole())
                .id(user.getId())
                .build();

    }



}
