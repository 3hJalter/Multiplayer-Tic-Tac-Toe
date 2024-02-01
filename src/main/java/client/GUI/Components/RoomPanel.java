package client.GUI.Components;
import client.Manager.GameClient;
import entity.Room;
import entity.message.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class RoomPanel extends JPanel {
    public static final int ROWS = 2;
    public static final int COLS = 3;

    public RoomPanel() {
        setLayout(new GridLayout(ROWS, COLS, 10, 10));
    }

    public void showRoomsOnPage(int page, List<Room> rooms) {
        removeAll();
        int startIndex = page * ROWS * COLS;
        int endIndex = Math.min(startIndex + ROWS * COLS, rooms.size());

        for (int i = startIndex; i < endIndex; i++) {
            JPanel roomEntry = createRoomEntry(rooms.get(i));
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

    private JPanel createRoomEntry(Room room) {
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));

        // Add image with size 100x100
        ImageIcon roomImageIcon = new ImageIcon("src\\com\\client\\GUI\\asset\\BG.jpg"); // Replace with the path to your image
        Image roomImage = roomImageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel roomImageLabel = new JLabel(new ImageIcon(roomImage));
        entryPanel.add(roomImageLabel);

        // Add text: "Room ID: {room_id}"
        JLabel roomIdLabel = new JLabel("Room ID: " + room.getRoomId());
        entryPanel.add(roomIdLabel);

        // Add Join button
        if (room.getPlayer2Username() == null) {
            JButton joinButton = new JButton("Join");
            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String content = GameClient.Client.thisUser.username() + "," + room.getRoomId();
                    GameClient.Client.sendMsg(MessageType.FIND_ROOM, content);
                    // Add logic to handle joining the room
                }
            });
            entryPanel.add(joinButton);
        }

        return entryPanel;
    }
}