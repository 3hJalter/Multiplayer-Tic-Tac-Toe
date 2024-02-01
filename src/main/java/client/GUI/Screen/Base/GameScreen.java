package client.GUI.Screen.Base;

import client.GUI.ScreenManager;
import client.GUI.Utilities;
import client.Manager.GameClient;
import entity.message.MessageType;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static client.GUI.GUIConstant.*;

public class GameScreen extends BaseScreen {
    // Panel
    private JPanel belowContainerPanel;
    private JPanel boardPanel;
    // Label
    private JLabel turnLabel;
    private JLabel timeLabel;
    // Button
    private JButton[][] boardButtons;
    private boolean[][] clickedButtons;
    // Other
    private Timer timer;
    private int timeInSeconds = 0;

    public GameScreen() {
        SetDefaultValue(ScreenManager.Screen.GAME);
        onCreateBg();
        onCreatePanel();
        onCreateBoard();
        onCreateComponent();
        onAddButtonListener();
        onUpdateDataWhenOpen();
    }

    @Override
    public void SetDefaultValue(ScreenManager.Screen type) {
        super.SetDefaultValue(type);
        timer = new Timer(1000, e -> {
            timeInSeconds++;
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            Date date = new Date(timeInSeconds * 1000L);
            timeLabel.setText("Time: " + sdf.format(date));
        });
    }

    @Override
    public void onUpdateDataWhenOpen() {
        super.onUpdateDataWhenOpen();
        clickedButtons = new boolean[24][24];
        // clear image icon for all board buttons
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                boardButtons[i][j].setIcon(null);
            }
        }
        timeInSeconds = 0;
        timer.start();
        SetTurn(GameClient.Client.currentRoom.getPlayer1Username());
    }

    public void SetTurn(String username) {
        SetTurnLabel(username);
        // enable all board buttons if the username is the current user, disable otherwise
        onChangeBoardButtonActive(username.equals(GameClient.Client.thisUser.username()));
    }

    public void onDraw(int x, int y, String username) {
        soundClick();
        // draw X or O on the board
        drawXO(x, y, GameClient.Client.currentRoom.getPlayer1Username().equals(username) ? "X" : "O");
        // set active false for this button
        boardButtons[x][y].setEnabled(false);
        clickedButtons[x][y] = true;
        // set turn for the other player
        SetTurn(username.equals(GameClient.Client.currentRoom.getPlayer1Username())
                ? GameClient.Client.currentRoom.getPlayer2Username()
                : GameClient.Client.currentRoom.getPlayer1Username());
    }

    private void SetTurnLabel(String username) {
        turnLabel.setText("Turn: " + username);
    }

    public void onChangeBoardButtonActive(boolean isActive) {
        // set active for all board buttons that are not clicked
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                if (!clickedButtons[i][j]) {
                    boardButtons[i][j].setEnabled(isActive);
                }
            }
        }
    }

    private void onCreateBg() {
        // Create background image label
        setContentPane(Utilities.createBackground(BACKGROUND_IMAGE_PATH, SCREEN_WIDTH, SCREEN_HEIGHT));
        setLayout(new GridBagLayout());
    }

    private void onCreatePanel() {

        // Create panel
        belowContainerPanel = new JPanel();
        belowContainerPanel.setLayout(new FlowLayout());
        belowContainerPanel.setOpaque(false);

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(24, 24));
        boardPanel.setOpaque(false);

        // Add top panel to the frame
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;

        // Move boardPanel and buttonContainerPanel down
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(boardPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0;
        add(belowContainerPanel, gbc);
    }

    private void onCreateBoard() {
        // Create a 24x24 board, each cell size is 25x25
        boardButtons = new JButton[24][24];
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                boardButtons[i][j] = new JButton();
                boardButtons[i][j].setPreferredSize(new Dimension(25, 25));
                boardButtons[i][j].setBackground(Color.WHITE);
                boardPanel.add(boardButtons[i][j]);
            }
        }
    }

    private void onCreateComponent() {
        // Initialize labels
        turnLabel = new JLabel("Player 1's turn");
        turnLabel.setForeground(Color.WHITE);
        turnLabel.setFont(new Font("Arial", Font.BOLD, 15));
        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 15));

        // Add the components to the belowContainerPanel
        belowContainerPanel.setLayout(new BorderLayout());
        belowContainerPanel.add(turnLabel, BorderLayout.LINE_START);
        belowContainerPanel.add(timeLabel, BorderLayout.LINE_END);
    }

    private void onAddButtonListener() {
        // Add action listener for board buttons, open a dialog with position of the button
        for (int i = 0; i < 24; i++) {
            int finalI = i;
            for (int j = 0; j < 24; j++) {
                int finalJ = j;
                boardButtons[i][j].addActionListener(e -> {
                    // msg = <username>,<room_id>,<x>,<y>
                    String msg = GameClient.Client.thisUser.username() + "," + GameClient.Client.currentRoom.getRoomId() + "," + finalI + "," + finalJ;
                    GameClient.Client.sendMsg(MessageType.PLAY_ACTION, msg);
                });
            }
        }
    }

    // Draw X or O on the board
    private void drawXO(int x, int y, String value) {
        ImageIcon icon = new ImageIcon(value.equals("X") ? X_IMAGE_PATH : O_IMAGE_PATH);
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(boardButtons[x][y].getWidth(), boardButtons[x][y].getHeight(), Image.SCALE_SMOOTH);
        boardButtons[x][y].setIcon(new ImageIcon(scaledImg));
    }

    private synchronized void soundClick() {
        Thread thread = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                        new File(CLICK_SOUND_PATH));
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }
}