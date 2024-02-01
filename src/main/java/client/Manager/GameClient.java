package client.Manager;

import client.GUI.ScreenManager;
import entity.Room;
import entity.User;
import entity.message.MessageType;
import entity.message.ServerMessage;
import utilities.Constant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GameClient {
    // Singleton
    public static void main(String[] args) {
        Client client = new Client();
        client.OnInit();
    }

    public static class Client{
        private static volatile boolean isRunning = true;
        public static String lastMsgContent = "";
        private static final Map<MessageType, BiConsumer<ServerMessage.StatusCode, String>> messageHandlers = new HashMap<>();
        public static UserContext thisUser;
        public static Room currentRoom;
        public static List<UserContext> allUsers;
        public static List<Room> rooms;
        private static Socket socket;
        private static PrintWriter writer;

        public void OnInit(){
            onConnect(Constant.DEFAULT_HOST, Constant.DEFAULT_PORT);
            addMessageHandler();
            ScreenManager.Ins().OpenScreen(ScreenManager.Screen.LOGIN);
        }

        public static void sendMsg(MessageType type, String content){
            lastMsgContent = content;
            writer.println(type + ":" + content);
        }

        private static void handleMsgFromServer(String msg) {
            System.out.println("------------------------");
            System.out.println("Message from server: " + msg);
            String[] msgArr = msg.split(":", 3);
            MessageType type = MessageType.valueOf(msgArr[0]);
            ServerMessage.StatusCode code = ServerMessage.StatusCode.valueOf(msgArr[1]);
            String content = msgArr[2];
            messageHandlers.get(type).accept(code, content);
        }

        public static boolean isNotConnected(){
            return socket == null || !socket.isConnected();
        }

        public static boolean tryConnected(String host, Integer port){
            try {
                if ((host == null) || host.isEmpty()) {
                    host = Constant.DEFAULT_HOST;
                }
                if (port == null) {
                    port = Constant.DEFAULT_PORT;
                }
                socket = new Socket(host, port);
                if (socket.isConnected()) {
                    System.out.println("Server connected");
                    return true;
                } else {
                    socket = null;
                    System.out.println("Server not connected");
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        private static void onConnect(String host, Integer port) {
            try {
                if (socket == null) {
                    if (!tryConnected(host, port)) {
                        // Show message dialogue
                        return;
                    }
                }
                BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
                // Create a separate thread to read messages from the server
                Thread readerThread = new Thread(() -> {
                    try {
                        String serverMessage;
                        while (isRunning && (serverMessage = serverReader.readLine()) != null) {
                            System.out.println(serverMessage);
                            handleMsgFromServer(serverMessage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        onExit();
                    }
                });
                readerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void addMessageHandler(){
            System.out.println("------------------------");
            System.out.println("Adding message handlers...");
            messageHandlers.put(MessageType.LOGIN, ServerMessageHandler::onLogin);
            messageHandlers.put(MessageType.LOGOUT, (code1, content1) -> ServerMessageHandler.onLogout(code1));
            messageHandlers.put(MessageType.REGISTER, (code, content) -> ServerMessageHandler.onRegister(code));
            messageHandlers.put(MessageType.MATCH, ServerMessageHandler::onMatch);
            messageHandlers.put(MessageType.CANCEL_MATCH, ServerMessageHandler::onCancelMatch);
            messageHandlers.put(MessageType.CREATE_ROOM, ServerMessageHandler::onCreateRoom);
            messageHandlers.put(MessageType.FIND_ROOM, ServerMessageHandler::onFindRoom);
            messageHandlers.put(MessageType.OUT_ROOM, ServerMessageHandler::onOutRoom);
            messageHandlers.put(MessageType.LEADERBOARD, ServerMessageHandler::onLeaderboard);
            messageHandlers.put(MessageType.PLAY, (code, content) -> ServerMessageHandler.onPlay(code));
            messageHandlers.put(MessageType.PLAY_ACTION, ServerMessageHandler::onPlayAction);
            messageHandlers.put(MessageType.REFRESH, ServerMessageHandler::onRefresh);
        }

        public static void onClearData(){
            thisUser = null;
            currentRoom = null;
            allUsers = null;
            rooms = null;
        }

        public static void onExit(){
            isRunning = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public record UserContext(String username, User.UserStatus status) {
    }
}
