package server;

import entity.Board;
import entity.Room;
import entity.User;
import entity.message.MessageType;
import entity.message.ServerMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ClientMessageHandler {
    public static void onLogin(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and password
        // cut the string to get username and password
        // format of content: username,password
        String[] message = content.split(",");
        // check if the message has exactly 2 parts
        if (message.length != 2) {
            System.out.println("LOGIN:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.LOGIN, ServerMessage.StatusCode.C_401);
            return;
        }
        String username = message[0];
        String password = message[1];
        // check if server.getUsers() have username and password
        // if had, send back message: "LOGIN:SUCCESS"
        // else send back message: "LOGIN:FAIL"
        if (GameServer.Server.users.containsKey(username)
                && GameServer.Server.users.get(username).getPassword().equals(password)) {
            if (GameServer.Server.users.get(username).getUserStatus() != User.UserStatus.OFFLINE) {
                System.out.println("LOGIN:FAIL");
                // Response Data to Client
                clientSender.sendMessageToClient(MessageType.LOGIN, ServerMessage.StatusCode.C_402);
                return;
            }
            //#region Update it in server
            System.out.println("LOGIN:SUCCESS");
            GameServer.Server.users.get(username).setUserStatus(User.UserStatus.ONLINE);
            GameServer.Server.onlineUsers.put(username, clientSender);
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.LOGIN, ServerMessage.StatusCode.C_200,
                    GameServer.Server.GetInformationOnMainScreen());
            //#endregion
            return;
        }
        System.out.println("LOGIN:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.LOGIN, ServerMessage.StatusCode.C_403);
    }

    public static void onLogout(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username
        // cut the string to get username
        // format of content: username
        // check if server.getUsers() have username
        // if had, send back message: "LOGOUT:SUCCESS"
        // else send back message: "LOGOUT:FAIL"
        if (GameServer.Server.users.containsKey(content)
                && GameServer.Server.users.get(content).getUserStatus() != User.UserStatus.OFFLINE) {
            //#region Update it in server
            System.out.println("LOGOUT:SUCCESS");
            GameServer.Server.users.get(content).setUserStatus(User.UserStatus.OFFLINE);
            GameServer.Server.onlineUsers.remove(content);
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.LOGOUT, ServerMessage.StatusCode.C_200, content);
            //#endregion
            return;
        }
        System.out.println("LOGOUT:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.LOGOUT, ServerMessage.StatusCode.C_404);
    }

    public static void onRegister(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and password
        // cut the string to get username and password
        // format of content: username,password
        String[] message = content.split(",");
        // Check the number of arguments
        if (message.length != 2) {
            System.out.println("REGISTER:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.REGISTER, ServerMessage.StatusCode.C_401);
            return;
        }
        String username = message[0];
        String password = message[1];
        // Check if username and password is empty
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("REGISTER:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.REGISTER, ServerMessage.StatusCode.C_401);
            return;
        }
        // check if server.getUsers() have username
        // if had, send back message: "REGISTER:FAIL"
        // else send back message: "REGISTER:SUCCESS"
        if (GameServer.Server.users.containsKey(username)) {
            System.out.println("REGISTER:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.REGISTER, ServerMessage.StatusCode.C_405);
            return;
        }
        //#region Update it in database
        boolean isAdded = false;
        String query = "INSERT INTO user (username, password, score) VALUES (?, ?, ?)";
        try {
            PreparedStatement st = GameServer.Server.conn.prepareStatement(query);
            st.setString(1, username);
            st.setString(2, password);
            st.setInt(3, 0);
            st.executeUpdate();
            isAdded = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //#region Update it in server
        if (isAdded) {
            GameServer.Server.users.put(username, new User(GameServer.Server.users.size() + 1, username, password, 0, User.UserStatus.OFFLINE));
            System.out.println("REGISTER:SUCCESS");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.REGISTER, ServerMessage.StatusCode.C_200);
        } else {
            System.out.println("REGISTER:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.REGISTER, ServerMessage.StatusCode.C_406);
        }
        //#endregion
    }

    public static void onMatch(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username
        // cut the string to get username
        // format of content: username
        // check if server.getUsers() have status is ONLINE
        // if had, send back message: "MATCH:SUCCESS"
        // else send back message: "MATCH:FAIL"
        if (content == null) {
            System.out.println("MATCH:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.MATCH, ServerMessage.StatusCode.C_401);
            return;
        }
        if (GameServer.Server.users.containsKey(content)
                && GameServer.Server.users.get(content).getUserStatus() == User.UserStatus.ONLINE) {
            GameServer.Server.users.get(content).setUserStatus(User.UserStatus.MATCHING);
            // if had user with status is MATCHING, create room
            for (User user : GameServer.Server.users.values()) {
                if (user.getUserStatus() == User.UserStatus.MATCHING
                        && !Objects.equals(user.getUsername(), content)) {
                    // Create room and add to server room list
                    Room room = new Room(GameServer.Server.rooms.size() + 1,
                            Room.RoomState.PLAY,
                            content,
                            content,
                            user.getUsername()
                            );
                    GameServer.Server.rooms.add(room);
                    // Update user status
                    GameServer.Server.users.get(content).setUserStatus(User.UserStatus.PLAYING);
                    GameServer.Server.users.get(user.getUsername()).setUserStatus(User.UserStatus.PLAYING);
                    System.out.println("CREATE_ROOM:SUCCESS");
                    System.out.println("Room id: " + room.getRoomId()
                     + " Player 1: " + room.getPlayer1Username()
                     + " Player 2: " + room.getPlayer2Username());
                    // Response Data to Client
                    clientSender.sendMessageToClient(MessageType.CREATE_ROOM, ServerMessage.StatusCode.C_200,
                            room.getRoomId() + "," + user.getUsername() + ",true");
                    // Send to other user
                    GameServer.Server.onlineUsers.get(user.getUsername()).sendMessageToClient(MessageType.CREATE_ROOM, ServerMessage.StatusCode.C_200,
                            room.getRoomId() + "," + content + ",false");
                    return;
                }
            }
            System.out.println("MATCH:SUCCESS");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.MATCH, ServerMessage.StatusCode.C_200);
            return;
        }
        System.out.println("MATCH:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.MATCH, ServerMessage.StatusCode.C_404);
    }

    public static void onCancelMatch(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username
        // cut the string to get username
        // format of content: username
        // check if server.getUsers() have status is MATCHING
        // if had, send back message: "CANCEL_MATCH:SUCCESS"
        // else send back message: "CANCEL_MATCH:FAIL"
        if (content == null) {
            System.out.println("CANCEL_MATCH:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.CANCEL_MATCH, ServerMessage.StatusCode.C_401);
            return;
        }

        if (GameServer.Server.users.containsKey(content)
                && GameServer.Server.users.get(content).getUserStatus() == User.UserStatus.MATCHING) {
            //#region Update it in server
            System.out.println("CANCEL_MATCH:SUCCESS");
            GameServer.Server.users.get(content).setUserStatus(User.UserStatus.ONLINE);
            //#endregion
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.CANCEL_MATCH, ServerMessage.StatusCode.C_200);
            return;
        }
        System.out.println("CANCEL_MATCH:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.CANCEL_MATCH, ServerMessage.StatusCode.C_404);
    }

    public static void onCreateRoom(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username
        // cut the string to get username
        // format of content: username
        // check if server.getUsers() have status is ONLINE
        // if had, send back message: "CREATE_ROOM:SUCCESS"
        // else send back message: "CREATE_ROOM:FAIL"
        if (content == null) {
            System.out.println("CREATE_ROOM:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.CREATE_ROOM, ServerMessage.StatusCode.C_401);
            return;
        }

        if (GameServer.Server.users.containsKey(content)
                && GameServer.Server.users.get(content).getUserStatus() == User.UserStatus.ONLINE) {
            //#region Update it in server
            // Create room and add to server room list
            int lastRoomId = 1;
            if (!GameServer.Server.rooms.isEmpty()) {
                lastRoomId = GameServer.Server.rooms.get(GameServer.Server.rooms.size() - 1).getRoomId() + 1;
            }
            Room room = new Room(lastRoomId,
                    Room.RoomState.IDLE,
                    content,
                    content,
                    null
            );
            GameServer.Server.rooms.add(room);
            GameServer.Server.users.get(content).setUserStatus(User.UserStatus.PLAYING);
            //#endregion
            System.out.println("CREATE_ROOM:SUCCESS");
            System.out.println("Room id: " + room.getRoomId()
                    + " Owner: " + room.getOwnerUsername());
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.CREATE_ROOM, ServerMessage.StatusCode.C_200,
                    room.getRoomId().toString());
            return;
        }
        System.out.println("CREATE_ROOM:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.CREATE_ROOM, ServerMessage.StatusCode.C_404);
    }

    public static void onFindRoom(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and room id
        // cut the string to get username and room id
        // format of content: username,room_id
        String[] message = content.split(",");
        if (message.length != 2) {
            System.out.println("FIND_ROOM:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_401);
            return;
        }
        String username = message[0];
        int roomId = Integer.parseInt(message[1]);
        // check if server.getUsers() have status is ONLINE
        // if had, send back message: "FIND_ROOM:SUCCESS"
        // else send back message: "FIND_ROOM:FAIL"
        if (GameServer.Server.users.containsKey(username)
                && GameServer.Server.users.get(username).getUserStatus() == User.UserStatus.ONLINE) {
            // Create room and add to server room list
            for (Room room : GameServer.Server.rooms) {
                if (room.getRoomId() == roomId) {
                    if (room.getRoomState() == Room.RoomState.IDLE) {
                        //#region Update it in server
                        GameServer.Server.users.get(username).setUserStatus(User.UserStatus.PLAYING);
                        room.setPlayer2Username(username);
                        room.setRoomState(Room.RoomState.READY);
                        System.out.println("FIND_ROOM:SUCCESS");
                        System.out.println("Room id: " + room.getRoomId()
                                + " Owner: " + room.getOwnerUsername()
                                + " Player 1: " + room.getPlayer1Username()
                                + " Player 2: " + room.getPlayer2Username());
                        // Response Data to Client
                        clientSender.sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_200,
                                room.getRoomId() + "," + room.getOwnerUsername());
                        // Send to other user
                        GameServer.Server.onlineUsers.get(room.getOwnerUsername())
                                .sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_200,
                                room.getRoomId() + "," + username);
                        // DEBUG: ToString this room
                        System.out.println(room);
                        //#endregion
                        return;
                    }
                    System.out.println("FIND_ROOM:FAIL");
                    // Response Data to Client
                    clientSender.sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_408);
                }
            }
            System.out.println("FIND_ROOM:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_407);
            return;
        }
        System.out.println("FIND_ROOM:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.FIND_ROOM, ServerMessage.StatusCode.C_404);
    }

    public static void onOutRoom(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and room id
        // cut the string to get username and room id
        // format of content: username,room_id
        String[] message = content.split(",");
        String username = message[0];
        int roomId = Integer.parseInt(message[1]);
        // check if server.getUsers() have status is PLAYING
        // if had, send back message: "OUT_ROOM:SUCCESS"
        // else send back message: "OUT_ROOM:FAIL"
        if (GameServer.Server.users.containsKey(username)
                && GameServer.Server.users.get(username).getUserStatus() == User.UserStatus.PLAYING) {
            // find the room
            for (Room room : GameServer.Server.rooms) {
                if (room.getRoomId() == roomId) {
                    if (room.getOwnerUsername().equals(username)) {
                        //#region Update it in server
                        GameServer.Server.users.get(username).setUserStatus(User.UserStatus.ONLINE);
                        // Delete room
                        GameServer.Server.rooms.remove(room);
                        //#endregion
                        System.out.println("OUT_ROOM:SUCCESS");
                        // Response Data to Client
                        clientSender.sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_200,
                                GameServer.Server.GetInformationOnMainScreen()); // To update the main screen
                        // Check if player 2 exist in room
                        if (room.getPlayer2Username() != null) {
                            GameServer.Server.users.get(room.getPlayer2Username()).setUserStatus(User.UserStatus.ONLINE);
                            // Send to other user
                            GameServer.Server.onlineUsers.get(room.getPlayer2Username())
                                    .sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_200,
                                            GameServer.Server.GetInformationOnMainScreen()); // To update the main screen
                        }
                        return;
                    } else if (room.getPlayer2Username().equals(username)) {
                        //#region Update it in server
                        GameServer.Server.users.get(username).setUserStatus(User.UserStatus.ONLINE);
                        room.setPlayer2Username(null);
                        room.setRoomState(Room.RoomState.IDLE);
                        //#endregion
                        System.out.println("OUT_ROOM:SUCCESS");
                        // Response Data to Client
                        clientSender.sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_200,
                                GameServer.Server.GetInformationOnMainScreen()); // To update the main screen
                        // Send to other user
                        // Check if player 1 (owner) exist in room
                        if (room.getPlayer1Username() != null) {
                            GameServer.Server.onlineUsers.get(room.getPlayer1Username())
                                    .sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_200);
                        }
                        return;
                    } else {
                        System.out.println("OUT_ROOM:FAIL");
                        // Response Data to Client
                        clientSender.sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_410);
                        return;
                    }
                }
            }
            System.out.println("OUT_ROOM:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.OUT_ROOM, ServerMessage.StatusCode.C_409);
        }
    }

    public static void onLeaderboard(GameServer.Server.ClientHandler clientSender) {
        // return list of user with score, sort by score
        // Response Data to Client
        System.out.println("LEADERBOARD:SUCCESS");
        clientSender.sendMessageToClient(MessageType.LEADERBOARD, ServerMessage.StatusCode.C_200,
                GameServer.Server.GetLeaderboard());
        // String format
    }

    public static void onPlay(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and room id
        // cut the string to get username and room id
        // format of content: username,room_id
        String[] message = content.split(",");
        String username = message[0];
        int roomId = Integer.parseInt(message[1]);
        if (GameServer.Server.users.containsKey(username)
                && GameServer.Server.users.get(username).getUserStatus() == User.UserStatus.PLAYING) {
            // find the room
            for (Room room : GameServer.Server.rooms) {
                if (room.getRoomId() == roomId) {
                    // check if server.getUsers() have status is PLAYING
                    // if had, send back message: "PLAY:SUCCESS"
                    // else send back message: "PLAY:FAIL"
                    // Check if room have 2 players and the username is owner or player 1
                    if (room.getPlayer2Username() != null
                            && (room.getOwnerUsername().equals(username)
                            || room.getPlayer1Username().equals(username))) {
                        //#region Update it in server
                        room.setRoomState(Room.RoomState.PLAY);
                        //#endregion
                        System.out.println("PLAY:SUCCESS");
                        // Response Data to Client
                        clientSender.sendMessageToClient(MessageType.PLAY, ServerMessage.StatusCode.C_200);
                        // Send to other user
                        GameServer.Server.onlineUsers.get(room.getPlayer2Username())
                                .sendMessageToClient(MessageType.PLAY, ServerMessage.StatusCode.C_200);
                        return;
                    }
                    System.out.println("PLAY:FAIL");
                    clientSender.sendMessageToClient(MessageType.PLAY, ServerMessage.StatusCode.C_412);
                    return;
                }
            }
            System.out.println("PLAY:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.PLAY, ServerMessage.StatusCode.C_407);
            return;
        }
        System.out.println("PLAY:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.PLAY, ServerMessage.StatusCode.C_409);
    }

    public static void onPlayAction(GameServer.Server.ClientHandler clientSender, String content) {
        // content have username and room id and position x and y
        // cut the string to get username and room id and position x and y
        // format of content: username,room_id,x,y
        String[] message = content.split(",");
        String username = message[0];
        int roomId = Integer.parseInt(message[1]);
        int x = Integer.parseInt(message[2]);
        int y = Integer.parseInt(message[3]);
        if (GameServer.Server.users.containsKey(username)
                && GameServer.Server.users.get(username).getUserStatus() == User.UserStatus.PLAYING) {
            // find the room
            for (Room room : GameServer.Server.rooms) {
                if (room.getRoomId() == roomId) {
                    if (room.getRoomState() == Room.RoomState.PLAY) {
                        // Response msg: username,x,y
                        if (x < 0 || x > 23 || y < 0 || y > 23) {
                            // Error -> Only send to client
                            System.out.println("PLAY_ACTION:FAIL");
                            // Response Data to Client
                            clientSender.sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_414);
                        } else {
                            // Set matrix
                            Board board = room.getBoard();
                            if (board.setMatrix(x, y, username.equals(room.getPlayer1Username()) ? Board.CellValue.X : Board.CellValue.O)) {
                                // Success -> Send to both client
                                System.out.println("PLAY_ACTION:SUCCESS");
                                // Response Data to Client
                                GameServer.Server.onlineUsers.get(room.getPlayer1Username())
                                        .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                username + "," + x + "," + y);
                                GameServer.Server.onlineUsers.get(room.getPlayer2Username())
                                        .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                username + "," + x + "," + y);
                                // check end game
                                Board.EndGameType endGameType = board.checkEndGame();
                                switch (endGameType) {
                                    case NONE -> {}
                                    case DRAW -> {
                                        // each player get 1 point
                                        boolean isAdded = false;
                                        String query = "UPDATE user SET score = score + 1 WHERE username = ?";
                                        try {
                                            PreparedStatement st = GameServer.Server.conn.prepareStatement(query);
                                            st.setString(1, room.getPlayer1Username());
                                            st.executeUpdate();
                                            st.setString(1, room.getPlayer2Username());
                                            st.executeUpdate();
                                            isAdded = true;
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        if (isAdded) {
                                            GameServer.Server.users.get(room.getPlayer1Username()).setScore(GameServer.Server.users.get(room.getPlayer1Username()).getScore() + 1);
                                            GameServer.Server.users.get(room.getPlayer2Username()).setScore(GameServer.Server.users.get(room.getPlayer2Username()).getScore() + 1);
                                        }
                                        // Send to both client with x = 100, y = 100
                                        GameServer.Server.onlineUsers.get(room.getPlayer1Username())
                                                .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                        username + "," + 100 + "," + 100);
                                        GameServer.Server.onlineUsers.get(room.getPlayer2Username())
                                                .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                        username + "," + 100 + "," + 100);
                                        // Remove the room
                                        GameServer.Server.rooms.remove(room);
                                        // Change status of both player to ONLINE
                                        GameServer.Server.users.get(room.getPlayer1Username()).setUserStatus(User.UserStatus.ONLINE);
                                        GameServer.Server.users.get(room.getPlayer2Username()).setUserStatus(User.UserStatus.ONLINE);
                                    } case WIN -> {
                                        // The winner get 2 points, the loser get 0 point
                                        boolean isAdded = false;
                                        String query = "UPDATE user SET score = score + 2 WHERE username = ?";
                                        try {
                                            PreparedStatement st = GameServer.Server.conn.prepareStatement(query);
                                            st.setString(1, username);
                                            st.executeUpdate();
                                            isAdded = true;
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                        if (isAdded) {
                                            GameServer.Server.users.get(username).setScore(GameServer.Server.users.get(username).getScore() + 2);
                                        }
                                        // Send to both client with x = 101, y = 101
                                        GameServer.Server.onlineUsers.get(room.getPlayer1Username())
                                                .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                        username + "," + 101 + "," + 101);
                                        GameServer.Server.onlineUsers.get(room.getPlayer2Username())
                                                .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                                        username + "," + 101 + "," + 101);
                                        // Remove the room
                                        GameServer.Server.rooms.remove(room);
                                        // Change status of both player to ONLINE
                                        GameServer.Server.users.get(room.getPlayer1Username()).setUserStatus(User.UserStatus.ONLINE);
                                        GameServer.Server.users.get(room.getPlayer2Username()).setUserStatus(User.UserStatus.ONLINE);
                                    }
                                }
                            } else {
                                // Error -> Only send to client
                                System.out.println("PLAY_ACTION:FAIL");
                                // Response Data to Client
                                clientSender.sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_414);
                            }
                        }
                        return;
                    }
                    System.out.println("PLAY_ACTION:FAIL");
                    // Response Data to Client
                    clientSender.sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_413);
                    return;
                }
            }
            System.out.println("PLAY_ACTION:FAIL");
            // Response Data to Client
            clientSender.sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_407);
            return;
        }
        System.out.println("PLAY_ACTION:FAIL");
        // Response Data to Client
        clientSender.sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_409);
    }

    public static void onRefresh(GameServer.Server.ClientHandler clientSender) {
        // content have nothing
        // Response Data to Client
        System.out.println("REFRESH:SUCCESS");
        clientSender.sendMessageToClient(MessageType.REFRESH, ServerMessage.StatusCode.C_200,
                GameServer.Server.GetInformationOnMainScreen());
    }

    public static void onSuddenLossConnection(GameServer.Server.ClientHandler lossClient){
        String username = GameServer.Server.onlineUsers.inverse().get(lossClient);
        if (username != null) {
            System.out.println("Sudden loss connection: " + username);
            GameServer.Server.users.get(username).setUserStatus(User.UserStatus.OFFLINE);
            GameServer.Server.onlineUsers.remove(username);
            String otherPlayerInRoom;
            Room roomIn = null;
            for (Room room : GameServer.Server.rooms) {
                if (room.getPlayer1Username().equals(username)) {
                    otherPlayerInRoom = room.getPlayer2Username();
                } else if (room.getPlayer2Username().equals(username)) {
                    otherPlayerInRoom = room.getPlayer1Username();
                } else continue;
                // otherPlayerInRoom player get 2 point
                boolean isAdded = false;
                String query = "UPDATE user SET score = score + 2 WHERE username = ?";
                try {
                    PreparedStatement st = GameServer.Server.conn.prepareStatement(query);
                    st.setString(1, otherPlayerInRoom);
                    st.executeUpdate();
                    isAdded = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (isAdded) {
                    GameServer.Server.users.get(otherPlayerInRoom).setScore(GameServer.Server.users.get(otherPlayerInRoom).getScore() + 2);
                }
                // Send win to otherPlayerInRoom player
                System.out.println("Send win to: " + otherPlayerInRoom);
                GameServer.Server.onlineUsers.get(otherPlayerInRoom)
                        .sendMessageToClient(MessageType.PLAY_ACTION, ServerMessage.StatusCode.C_200,
                                otherPlayerInRoom + "," + 101 + "," + 101);
                // Remove the room
                roomIn = room;
                // Change status of player to ONLINE
                GameServer.Server.users.get(otherPlayerInRoom).setUserStatus(User.UserStatus.ONLINE);
                // out the loop
                break;
            }
            if (roomIn != null) {
                GameServer.Server.rooms.remove(roomIn);
            }
        }


    }
}
