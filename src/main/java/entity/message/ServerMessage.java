package entity.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ServerMessage {
    MessageType messageType;
    StatusCode status;
    String content;

    public enum StatusCode {
        C_200, // OK
        C_401, // Number of arguments is not correct
        C_402, // Username is currently in use
        C_403, // Username or password is incorrect
        C_404, // User is not online
        C_405, // User is existed
        C_406, // User can not added to database
        C_407, // Room not found
        C_408, // Room is full
        C_409, // User not found or not playing
        C_410, // User is not in room
        C_411, // User is not owner of room
        C_412, // Room is not ready
        C_413, // Room is not playing
        C_414 // Invalid Cell Value
    }
}


