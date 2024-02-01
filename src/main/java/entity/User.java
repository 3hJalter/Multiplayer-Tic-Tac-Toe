package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String username;
    private String password;
    private Integer score;
    private UserStatus userStatus;

    public static UserStatus statusFromString(String status) {
        return switch (status) {
            case "OFFLINE" -> UserStatus.OFFLINE;
            case "ONLINE" -> UserStatus.ONLINE;
            case "MATCHING" -> UserStatus.MATCHING;
            case "PLAYING" -> UserStatus.PLAYING;
            default -> null;
        };
    }

    public enum UserStatus {
        OFFLINE,
        ONLINE,
        MATCHING,
        PLAYING
    }
}


