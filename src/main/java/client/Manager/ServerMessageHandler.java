package client.Manager;

import client.GUI.Screen.Base.GameScreen;
import client.GUI.ScreenManager;
import entity.Room;
import entity.User;
import entity.message.ServerMessage;

import java.util.ArrayList;

public class ServerMessageHandler {
    public static void onLogin(ServerMessage.StatusCode code, String content) {
        // Check the status code
        if (code == ServerMessage.StatusCode.C_200) {
            // Take the thisUser info from the lastMsgContent
            String[] lastMsgArr = GameClient.Client.lastMsgContent.split(",", 2);
            GameClient.Client.thisUser = new GameClient.UserContext(lastMsgArr[0], User.UserStatus.ONLINE);
            // Take the allUsers and roomID info from the content
            UpdateListData(content);
            // Open the MainScreen when Login button is clicked
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.MAIN);
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onRegister(ServerMessage.StatusCode code) {
        ScreenManager.Ins().ShowMessagePopup(code);
    }

    public static void onLogout(ServerMessage.StatusCode code) {
        // Check the status code
        if (code == ServerMessage.StatusCode.C_200) {
            // Open the LoginScreen when Logout button is clicked
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.LOGIN);
            // CLear all data info
            GameClient.Client.onClearData();
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onMatch(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            ScreenManager.Ins().ShowPopup(ScreenManager.Popup.MATCHING, content);
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onCancelMatch(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // close the popup
            ScreenManager.Ins().getCurrentPopup().Close();
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onCreateRoom(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // content can be roomID,username,bool or just roomID
            // Take param
            String[] contentArr = content.split(",", 3);
            int roomId = Integer.parseInt(contentArr[0]);
            Room room = new Room();
            room.setRoomId(roomId);
            // Check if contentArr has 3 elements -> Create room by matching
            if (contentArr.length == 3) {
                // close the popup
                if (ScreenManager.Ins().getCurrentPopup() != null) {
                    ScreenManager.Ins().getCurrentPopup().Close();
                }
                // if the contentArr[2] is true -> This user is the owner of the room, else -> This user is the player2
                if (Boolean.parseBoolean(contentArr[2])) {
                    room.setOwnerUsername(GameClient.Client.thisUser.username());
                    room.setPlayer1Username(GameClient.Client.thisUser.username());
                    room.setPlayer2Username(contentArr[1]);
                } else {
                    room.setOwnerUsername(contentArr[1]);
                    room.setPlayer1Username(contentArr[1]);
                    room.setPlayer2Username(GameClient.Client.thisUser.username());
                }
            } else {
                // Create new room
                room.setOwnerUsername(GameClient.Client.thisUser.username());
                room.setPlayer1Username(GameClient.Client.thisUser.username());
            }
            GameClient.Client.rooms.add(room);
            GameClient.Client.currentRoom = room;
            // Open the RoomScreen when Create Room button is clicked
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.ROOM);
        }
    }

    public static void onFindRoom(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Take the roomID info from the content
            // Format: roomID, username in room
            String[] contentArr = content.split(",", 2);
            // Check if currentRoom has same roomID -> The owner of the room
            if (GameClient.Client.currentRoom != null && GameClient.Client.currentRoom.getRoomId() == Integer.parseInt(contentArr[0])) {
                // Update the player2Username
                System.out.println("Owner update player 2: " + contentArr[1]);
                GameClient.Client.currentRoom.setPlayer2Username(contentArr[1]);
            } else {
                // -> The player2 who join the room
                // Try to find the room in the rooms list
                for (Room room : GameClient.Client.rooms) {
                    if (room.getRoomId() == Integer.parseInt(contentArr[0])) {
                        // Update the player1Username (Owner)
                        room.setOwnerUsername(contentArr[1]);
                        room.setPlayer1Username(contentArr[1]);
                        room.setPlayer2Username(GameClient.Client.thisUser.username());
                        // Set currentRoom
                        GameClient.Client.currentRoom = room;
                        break;
                    }
                }
                // If not found -> Create new room
                if (GameClient.Client.currentRoom == null) {
                    Room room = new Room();
                    room.setRoomId(Integer.parseInt(contentArr[0]));
                    room.setOwnerUsername(contentArr[1]);
                    room.setPlayer1Username(contentArr[1]);
                    room.setPlayer2Username(GameClient.Client.thisUser.username());
                    GameClient.Client.rooms.add(room);
                    GameClient.Client.currentRoom = room;
                }
                System.out.println("Player 2 join room: " + contentArr[1]);
            }
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.ROOM);
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onOutRoom(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Do thing based on has content or content null-empty
            if (content == null || content.isEmpty()) {
                // Meaning that the user still in the room, only clear player 2
                GameClient.Client.currentRoom.setPlayer2Username(null);
                ScreenManager.Ins().UpdateNewCurrentScreen();
            } else {
                // Meaning that the user is not in the room, clear currentRoom
                GameClient.Client.currentRoom = null;
                // Take the allUsers and roomID info from the content
                UpdateListData(content);
                // Open the MainScreen when Login button is clicked
                ScreenManager.Ins().OpenScreen(ScreenManager.Screen.MAIN);
            }
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onLeaderboard(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Open a popup with content
            ScreenManager.Ins().ShowPopup(ScreenManager.Popup.LEADERBOARD, content);
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onPlay(ServerMessage.StatusCode code) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Open the GameScreen when Play button is clicked
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.GAME);
            //
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onPlayAction(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Cut the content msg: <username>,<x>,<y>
            String[] contentArr = content.split(",", 3);
            boolean isFromThisUser = contentArr[0].equals(GameClient.Client.thisUser.username());
            // Check if the action is surrender
            int x = Integer.parseInt(contentArr[1]);
            int y = Integer.parseInt(contentArr[2]);
            if (x == 100 && y == 100) {
                ScreenManager.Ins().ShowPopup(ScreenManager.Popup.END_GAME, "Draw!");
            } else if (x == 101 && y == 101) {
                ScreenManager.Ins().ShowPopup(ScreenManager.Popup.END_GAME, isFromThisUser ? "You win!" : "You lose!");
            }
            else {
                // Show action dialog
                // cast current screen in ScreenManager to GameScreen, then call onDraw
                GameScreen gameScreen = (GameScreen) ScreenManager.Ins().getCurrentScreen();
                gameScreen.onDraw(Integer.parseInt(contentArr[1]), Integer.parseInt(contentArr[2]), contentArr[0]);
            }
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    public static void onRefresh(ServerMessage.StatusCode code, String content) {
        if (code == ServerMessage.StatusCode.C_200) {
            // Take the allUsers and roomID info from the content
            UpdateListData(content);
            if (ScreenManager.Ins().getCurrentScreen().type == ScreenManager.Screen.MAIN) {
                ScreenManager.Ins().UpdateNewCurrentScreen();
            } else {
                ScreenManager.Ins().OpenScreen(ScreenManager.Screen.MAIN);
            }
        } else {
            // Show error message dialog
            ScreenManager.Ins().ShowMessagePopup(code);
        }
    }

    private static void UpdateListData(String content) {
        // Format: number of users, username1, userStatus1, username2, userStatus2, ... , usernameN, userStatusN, roomID1, roomID2, ... , roomIDN
        String[] contentArr = content.split(",");
        // User list
        if (GameClient.Client.allUsers == null) {
            GameClient.Client.allUsers = new ArrayList<>();
        } else {
            GameClient.Client.allUsers.clear();
        }
        for (int i = 0; i < Integer.parseInt(contentArr[0]); i++) {
            GameClient.Client.allUsers.add(new GameClient.UserContext(contentArr[i * 2 + 1], User.statusFromString(contentArr[i * 2 + 2])));
        }
        // Room list
        if (GameClient.Client.rooms == null) {
            GameClient.Client.rooms = new ArrayList<>();
        } else {
            GameClient.Client.rooms.clear();
        }
        for (int i = Integer.parseInt(contentArr[0]) * 2 + 1; i < contentArr.length; i++) {
            Room room = new Room();
            room.setRoomId(Integer.parseInt(contentArr[i]));
            GameClient.Client.rooms.add(room);
        }
    }
}
