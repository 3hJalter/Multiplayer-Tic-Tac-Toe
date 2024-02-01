package client.GUI.Screen.Base;

import client.GUI.ScreenManager;
import client.GUI.Utilities;
import client.Manager.GameClient;
import entity.message.MessageType;

import javax.swing.*;
import java.awt.*;

public class RoomScreen extends BaseScreen {
    // Panel
    JPanel topPanel;
    JPanel bottomPanel;
    JPanel bottomLeftPanel;
    JPanel bottomCenterPanel;
    JPanel bottomRightPanel;
    // Label
    JLabel roomNameLabel;
    JLabel player1UsernameLabel;
    JLabel player2UsernameLabel;
    // Button
    JButton playButton;
    JButton quitButton;

    public RoomScreen(){
        SetDefaultValue(ScreenManager.Screen.ROOM);
        onCreatePanel();
        onCreateComponent();
        onAddComponent();
        onAddButtonListener();
    }

    @Override
    public void onUpdateDataWhenOpen(){
        onResetData();
        if (GameClient.Client.currentRoom == null) return;
        if (GameClient.Client.currentRoom.getRoomId() != null) {
            roomNameLabel.setText("Room ID: " + GameClient.Client.currentRoom.getRoomId());
        }
        if (GameClient.Client.currentRoom.getPlayer1Username() != null) {
            player1UsernameLabel.setText("Player 1: " + GameClient.Client.currentRoom.getPlayer1Username());
        }
        if (GameClient.Client.currentRoom.getPlayer2Username() != null) {
            player2UsernameLabel.setText("Player 2: " + GameClient.Client.currentRoom.getPlayer2Username());
        }
        // hide the play button if the current user is not the owner of the room
        playButton.setVisible(GameClient.Client.currentRoom.getOwnerUsername().equals(GameClient.Client.thisUser.username()));
    }

    private void onResetData(){
        roomNameLabel.setText("NO ROOM");
        player1UsernameLabel.setText("NO PLAYER");
        player2UsernameLabel.setText("NO PLAYER");
    }

    private void onCreatePanel(){
        topPanel = Utilities.createRoundedPanel(false, 0, 0, 2);
        topPanel.setLayout(new GridLayout(1, 2));
        bottomPanel = Utilities.createRoundedPanel(false, 0, 0, 3);
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);
        bottomLeftPanel = new JPanel();
        bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel, BoxLayout.Y_AXIS));
        bottomCenterPanel = new JPanel();
        bottomCenterPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomRightPanel = new JPanel();
        bottomRightPanel.setLayout(new BoxLayout(bottomRightPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(bottomLeftPanel);
        bottomPanel.add(bottomCenterPanel);
        bottomPanel.add(bottomRightPanel);

    }

    private void onCreateComponent(){
        roomNameLabel = new JLabel();
        player1UsernameLabel = new JLabel();
        player2UsernameLabel = new JLabel();
        playButton = new JButton("Play");
        quitButton = new JButton("Quit Room");
    }

    private void onAddComponent(){
        topPanel.add(roomNameLabel);
        topPanel.add(quitButton);
        bottomLeftPanel.add(player1UsernameLabel);
        bottomCenterPanel.add(playButton);
        bottomRightPanel.add(player2UsernameLabel);
    }

    private void onAddButtonListener(){
        playButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(this, "You are not connected to the server!");
                return;
            }
            if (GameClient.Client.currentRoom == null) {
                JOptionPane.showMessageDialog(this, "You are not in any room!");
                return;
            }
            if (!GameClient.Client.currentRoom.getOwnerUsername().equals(GameClient.Client.thisUser.username())) {
                JOptionPane.showMessageDialog(this, "You are not the owner of this room!");
                return;
            }
            if (GameClient.Client.currentRoom.getPlayer2Username() == null) {
                JOptionPane.showMessageDialog(this, "There is no player 2 in this room!");
                return;
            }
            String content = GameClient.Client.thisUser.username() + "," + GameClient.Client.currentRoom.getRoomId().toString();
            GameClient.Client.sendMsg(MessageType.PLAY, content);
        });

        quitButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(this, "You are not connected to the server!");
                return;
            }
            // Open the MainScreen when Login button is clicked
            String content = GameClient.Client.thisUser.username() + "," + GameClient.Client.currentRoom.getRoomId().toString();
            GameClient.Client.sendMsg(MessageType.OUT_ROOM, content);
        });
    }
}
