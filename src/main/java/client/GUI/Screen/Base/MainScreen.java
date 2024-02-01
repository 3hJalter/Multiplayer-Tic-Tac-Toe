package client.GUI.Screen.Base;

import client.GUI.Components.RoomPanel;
import client.GUI.Components.UserPanel;
import client.GUI.ScreenManager;
import client.Manager.GameClient;
import entity.message.MessageType;

import javax.swing.*;
import java.awt.*;

import static client.GUI.Utilities.*;

public class MainScreen extends BaseScreen {
    // Label
    JLabel welcomeLabel;
    private JPanel topPanel;
    private JPanel centerLeftTopPanel;
    private RoomPanel centerLeftCenterPanel;
    private JPanel centerLeftBottomPanel;
    private JPanel centerRightPanel;
    private UserPanel centerRightCenterPanel;
    private JPanel centerRightBottomPanel;
    // Text Field
    private JTextField findRoomIdField;
    // Button
    private JButton prevButton;
    private JButton nextButton;
    private JButton prevUserButton;
    private JButton nextUserButton;
    private JButton createRoomButton;
    private JButton matchRandomButton;
    private JButton findRoomIdButton;
    private JButton refreshMainScreenButton;
    private JButton leaderboardButton;
    private JButton logoutButton;
    // Data
    private int currentPage = 0;
    private int currentUserPage = 0;

    public MainScreen() {
        SetDefaultValue(ScreenManager.Screen.MAIN);
        onCreatePanel();
        onCreateComponent();
        onAddComponent();
        onAddButtonListener();
    }

    @Override
    public void onUpdateDataWhenOpen() {
        onResetData();
        centerLeftCenterPanel.showRoomsOnPage(currentPage, GameClient.Client.rooms);
        centerRightCenterPanel.showUsersOnPage(currentUserPage, GameClient.Client.allUsers);
    }

    private void onResetData() {
        currentPage = 0;
        currentUserPage = 0;
    }

    private void onCreatePanel() {
        // container
        // Panel
        JPanel containerPanel = new JPanel(new GridLayout());
        containerPanel.setLayout(new BorderLayout());
        add(containerPanel);
        // top
        topPanel = createRoundedPanel(false, 20, 0, 2);
        containerPanel.add(topPanel, BorderLayout.NORTH);
        // center left
        JPanel centerLeftPanel = createRoundedPanel(false, 20, 0, 2);
        centerLeftPanel.setLayout(new BorderLayout());
        // center left top
        centerLeftTopPanel = createRoundedPanel(false, 20, 0, 2);
        centerLeftPanel.add(centerLeftTopPanel, BorderLayout.NORTH);
        // center left center
        centerLeftCenterPanel = new RoomPanel();
        centerLeftPanel.add(centerLeftCenterPanel, BorderLayout.CENTER);
        // center left bottom
        centerLeftBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerLeftPanel.add(centerLeftBottomPanel, BorderLayout.SOUTH);
        // center right
        centerRightPanel = createRoundedPanel(false, 20, 0, 2);
        centerRightPanel.setLayout(new BorderLayout());
        containerPanel.add(centerLeftPanel, BorderLayout.WEST);
        containerPanel.add(centerRightPanel, BorderLayout.CENTER);
        // center right center
        centerRightCenterPanel = new UserPanel();
        centerRightPanel.add(centerRightCenterPanel, BorderLayout.CENTER);
        // center right bottom
        centerRightBottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerRightPanel.add(centerRightBottomPanel, BorderLayout.SOUTH);
    }

    private void onCreateComponent() {
        // Top
        logoutButton = createRoundedButton("Logout");
        welcomeLabel = new JLabel("Welcome, user " + GameClient.Client.thisUser.username() + "!");
        // Center Left Top
        createRoomButton = createRoundedButton("Create Room");
        matchRandomButton = createRoundedButton("Match Random");
        findRoomIdField = createRoundedTextField();
        findRoomIdButton = createRoundedButton("Find Room ID");
        refreshMainScreenButton = createRoundedButton("Refresh");
        // Center Left Bot
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        // Center Right
        leaderboardButton = createRoundedButton("Leaderboard");
        // Center Right Bot
        prevUserButton = new JButton("Previous");
        nextUserButton = new JButton("Next");
    }

    private void onAddComponent() {
        // Top Component
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutButton, BorderLayout.EAST);
        // Center Left Top Component
        centerLeftTopPanel.add(createRoomButton);
        centerLeftTopPanel.add(matchRandomButton);
        centerLeftTopPanel.add(new JLabel("Find Room ID:"));
        centerLeftTopPanel.add(findRoomIdField);
        centerLeftTopPanel.add(findRoomIdButton);
        centerLeftTopPanel.add(refreshMainScreenButton);
        // Center Left Bottom Component
        centerLeftBottomPanel.add(prevButton);
        centerLeftBottomPanel.add(nextButton);
        // Center Right Component
        centerRightPanel.add(leaderboardButton, BorderLayout.NORTH);
        // Center Right Bottom Component
        centerRightBottomPanel.add(prevUserButton);
        centerRightBottomPanel.add(nextUserButton);
    }

    private void onAddButtonListener() {
        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                centerLeftCenterPanel.showRoomsOnPage(currentPage, GameClient.Client.rooms);
            }
        });

        nextButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) GameClient.Client.rooms.size() / (RoomPanel.ROWS * RoomPanel.COLS));
            if (currentPage < totalPages - 1) {
                currentPage++;
                centerLeftCenterPanel.showRoomsOnPage(currentPage, GameClient.Client.rooms);
            }
        });

        prevUserButton.addActionListener(e -> {
            if (currentUserPage > 0) {
                currentUserPage--;
                centerRightCenterPanel.showUsersOnPage(currentUserPage, GameClient.Client.allUsers);
            }
        });

        nextUserButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) GameClient.Client.allUsers.size() / (UserPanel.ROWS * UserPanel.COLS));
            if (currentUserPage < totalPages - 1) {
                currentUserPage++;
                centerRightCenterPanel.showUsersOnPage(currentUserPage, GameClient.Client.allUsers);
            }
        });

        createRoomButton.addActionListener(e -> {
            // Open Room Screen
            GameClient.Client.sendMsg(MessageType.CREATE_ROOM, GameClient.Client.thisUser.username());
        });

        matchRandomButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(MainScreen.this, "You are not connected to the server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.MATCH, GameClient.Client.thisUser.username());
        });

        refreshMainScreenButton.addActionListener(e -> {
            // Add logic for Refresh button click
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(MainScreen.this, "You are not connected to the server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.REFRESH, GameClient.Client.thisUser.username());
        });

        findRoomIdButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(MainScreen.this, "You are not connected to the server!");
                return;
            }
            // Add logic for Find Room ID button click
            String roomId = findRoomIdField.getText();
            // Send content = username,room_id
            String content = GameClient.Client.thisUser.username() + "," + roomId;
            GameClient.Client.sendMsg(MessageType.FIND_ROOM, content);
        });

        leaderboardButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(MainScreen.this, "You are not connected to the server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.LEADERBOARD, GameClient.Client.thisUser.username());
        });

        // Add action listener for the logout button
        logoutButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(MainScreen.this, "You are not connected to the server!");
                return;
            }
            // Add logic for logout button click
            int choice = JOptionPane.showConfirmDialog(MainScreen.this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Perform logout actions
                GameClient.Client.sendMsg(MessageType.LOGOUT, GameClient.Client.thisUser.username());
            }
        });
    }
}
