package client.GUI.Components;

import client.GUI.Utilities;
import client.Manager.GameClient;
import entity.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {
    public static final int ROWS = 10;
    public static final int COLS = 1;
    public UserPanel() {
        setLayout(new GridLayout(ROWS, COLS, 10, 10));
    }

    public void showUsersOnPage(int page, List<GameClient.UserContext> users) {
        removeAll();
        int startIndex = page * ROWS * COLS;
        int endIndex = Math.min(startIndex + ROWS * COLS, users.size());

        for (int i = startIndex; i < endIndex; i++) {
            JPanel roomEntry = createUserEntry(users.get(i));
            add(roomEntry);
        }

        // Add empty panels to fill the remaining slots in the last row
        int emptySlots = ROWS * COLS - (endIndex - startIndex);
        for (int i = 0; i < emptySlots; i++) {
            JPanel emptyPanel = new JPanel();
            add(emptyPanel);
        }

        revalidate();
        repaint();
    }

    private JPanel createUserEntry(GameClient.UserContext user) {
        JPanel entryPanel = Utilities.createRoundedPanel(false, 10, 0, 2);
//        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
        // Show username and user status
        JLabel usernameLabel = new JLabel("Username: " + user.username());
        JLabel userStatusLabel = new JLabel("Status: " + user.status().toString());
        entryPanel.add(usernameLabel);
        entryPanel.add(userStatusLabel);
        return entryPanel;
    }
}
