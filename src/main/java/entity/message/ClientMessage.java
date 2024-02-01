package entity.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClientMessage {
    MessageType messageType;
    String content;
}
