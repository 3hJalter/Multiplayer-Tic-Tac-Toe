package server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import entity.Room;
import entity.User;
import entity.message.ClientMessage;
import entity.message.MessageType;
import entity.message.ServerMessage;
import utilities.Constant;
import utilities.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GameServer {
    public static void main(String[] args) throws SQLException {
        Server server = new Server();
        server.onInit();
        server.start();
    }

    public static class Server {
        //#region Server socket Data
        private static final Map<Socket, ClientHandler> clients = new HashMap<>();
        private static final Map<MessageType, BiConsumer<ClientHandler, String>> messageHandlers = new HashMap<>();
        //#region Game Database Data
        public static BiMap<String, ClientHandler> onlineUsers = HashBiMap.create();
        public static Map<String, User> users = new HashMap<>();
        //#endregion
        public static Connection conn;
        private static ServerSocket serverSocket;
        public static List<Room> rooms = new ArrayList<>();

        public void onInit() throws SQLException {
            try {
                serverSocket = new ServerSocket(5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectSQLite();
            setUserDataFromSQLite();
            addMessageHandler();
        }

        private void connectSQLite() {
            System.out.println("------------------------");
            System.out.println("Connecting to SQLite...");
            try {
                Class.forName("org.sqlite.JDBC");
                String dbURL = "jdbc:sqlite:" + Constant.DB_URL;
                conn = DriverManager.getConnection(dbURL);
                if (conn != null) {
                    System.out.println("Connected to the database");
                }
            } catch (ClassNotFoundException | SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void setUserDataFromSQLite() throws SQLException {
            System.out.println("------------------------");
            System.out.println("Get user data from SQLite");
            // Query
            String query = "SELECT * FROM User";
            // Create the java statement
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("user_id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                int score = rs.getInt("score");
                // Add user to list
                users.put(username, new User(id, username, password, score, User.UserStatus.OFFLINE));
            }
            // TEST: Print number of users
            System.out.println("Number of users: " + users.size());
        }

        private void addMessageHandler() {
            System.out.println("------------------------");
            System.out.println("Adding message handlers...");
            messageHandlers.put(MessageType.LOGIN, ClientMessageHandler::onLogin);
            messageHandlers.put(MessageType.LOGOUT, ClientMessageHandler::onLogout);
            messageHandlers.put(MessageType.REGISTER, ClientMessageHandler::onRegister);
            messageHandlers.put(MessageType.MATCH, ClientMessageHandler::onMatch);
            messageHandlers.put(MessageType.CANCEL_MATCH, ClientMessageHandler::onCancelMatch);
            messageHandlers.put(MessageType.CREATE_ROOM, ClientMessageHandler::onCreateRoom);
            messageHandlers.put(MessageType.FIND_ROOM, ClientMessageHandler::onFindRoom);
            messageHandlers.put(MessageType.OUT_ROOM, ClientMessageHandler::onOutRoom);
            messageHandlers.put(MessageType.LEADERBOARD, (clientSender, content) -> ClientMessageHandler.onLeaderboard(clientSender));
            messageHandlers.put(MessageType.PLAY, ClientMessageHandler::onPlay);
            messageHandlers.put(MessageType.PLAY_ACTION, ClientMessageHandler::onPlayAction);
            messageHandlers.put(MessageType.REFRESH, (clientSender, content) -> ClientMessageHandler.onRefresh(clientSender));
        }

        public void start() {
            System.out.println("------------------------");
            System.out.println("Server is running...");
            //#endregion
            int socketErrorCount = 0;
            final int MAX_SOCKET_ERROR_COUNT = 10;
            while (socketErrorCount < MAX_SOCKET_ERROR_COUNT) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.put(clientSocket, clientHandler);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                } catch (IOException e) {
                    System.out.println("Error accepting client connection: " + e.getMessage());
                    socketErrorCount++;
                }
            }
            //#region Handle when server stopped
            System.out.println("Server stopped because of too many socket errors");
            // disconnect all clients
            for (Socket clientSocket : clients.keySet()) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
            clients.clear();
            //#endregion
        }

        public static String GetInformationOnMainScreen() {
            StringBuilder result = new StringBuilder();
            // Format: Number of user, user_name_1, user_status_1, user_name_2, user_status_2, ... , user_name_n, user_status_n,
            // room_id_1, room_id_2, ... , room_id_n
            result.append(users.size()).append(",");
            for (Map.Entry<String, User> entry : users.entrySet()) {
                result.append(entry.getValue().getUsername()).append(",");
                result.append(entry.getValue().getUserStatus()).append(",");
            }
            for (Room room : rooms) {
                result.append(room.getRoomId()).append(",");
            }
            return result.toString();
        }

        public static String GetLeaderboard(){
            StringBuilder result = new StringBuilder();
            // Get a list (max 10) users with the highest score
            List<User> highestUser = new ArrayList<>(users.values());
            highestUser.sort((o1, o2) -> o2.getScore() - o1.getScore());
            int count = Math.min(highestUser.size(), 10);
            result.append(count).append(",");
            // Format: username_1, score_1, username_2, score_2, ... , username_n, score_n
            for (int i = 0; i < count; i++) {
                result.append(highestUser.get(i).getUsername()).append(",");
                result.append(highestUser.get(i).getScore()).append(",");
            }
            return result.toString();
        }

        public static class ClientHandler implements Runnable {
            private Socket thisSocket;
            public PrintWriter writer;
            public BufferedReader reader;

            ClientHandler(Socket clientSocket) {
                try {
                    System.out.println("------------------------");
                    System.out.println("New client connected");
                    System.out.println("Client socket: " + clientSocket);
                    System.out.println("Client address: " + clientSocket.getInetAddress());
                    System.out.println("Client port: " + clientSocket.getPort());
                    System.out.println("------------------------");
                    thisSocket = clientSocket;
                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new PrintWriter(clientSocket.getOutputStream(), true);
                } catch (Exception e) {
                    System.out.println("Error handling client# " + e.getMessage());
                }
            }

            @Override
            public void run() {
                try {
                    while (true) {
                        String receivedMessage = reader.readLine();
                        handleMessageFromClient(this, receivedMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Error when running clientHandler# " + e.getMessage());
                    ClientMessageHandler.onSuddenLossConnection(this);
                    clients.remove(thisSocket);
                    try {
                        thisSocket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Connection with client closed");
                    System.out.println("------------------------");
                }
            }

            private void handleMessageFromClient(ClientHandler sender, String receivedString) {
                System.out.println("------------------------");
                System.out.println("Received message: " + receivedString);
                // validate message
                if (!Utils.ValidateClientMessage(receivedString)) {
                    System.out.println("Message is not valid");
                    return;
                }
                ClientMessage receivedClientMessage = Utils.stringToMessage(receivedString);
                messageHandlers.get(receivedClientMessage.getMessageType()).accept(sender, receivedClientMessage.getContent());
            }

            public void sendMessageToClient(MessageType messagetype, ServerMessage.StatusCode statusCode) {
                sendMessageToClient(messagetype, statusCode, null);
            }

            public void sendMessageToClient(MessageType messagetype, ServerMessage.StatusCode statusCode, String content) {
                System.out.println("------------------------");
                System.out.println("Sending message to client...");
                if (content == null) {
                    content = "";
                }
                String message = messagetype + ":" + statusCode + ":" + content;
                System.out.println("Message: " + message);
                writer.println(message);
            }
        }
    }
}

