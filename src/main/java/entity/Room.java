package entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private Integer roomId;
    private RoomState roomState;
    private String ownerUsername;
    private String player1Username;
    private String player2Username;
    private Board board;

    public Room(Integer roomId, RoomState roomState, String ownerUsername, String player1Username, String player2Username) {
        this.roomId = roomId;
        this.roomState = roomState;
        this.ownerUsername = ownerUsername;
        this.player1Username = player1Username;
        this.player2Username = player2Username;
        board = new Board();
    }

    public enum RoomState {
        IDLE,
        READY,
        PLAY,
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", roomState=" + roomState +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", player1Username='" + player1Username + '\'' +
                ", player2Username='" + player2Username + '\'' +
                '}';
    }
}
