package client.GUI.Popup;

import client.GUI.Screen.Base.MainScreen;
import client.GUI.ScreenManager;
import client.Manager.GameClient;
import entity.message.MessageType;

import javax.swing.*;

public class EndGamePopup extends BasePopup {
    JDialog dialog;
    @Override
    public void Show(String message) {
        dialog = new JDialog();
        JButton mainMenuButton = new JButton("Go to Main Menu");
        mainMenuButton.addActionListener(e -> {
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(ScreenManager.Ins().getCurrentScreen(), "You are not connected to the server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.REFRESH, GameClient.Client.thisUser.username());
            dialog.dispose();
        });
        final JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{}, null);

        optionPane.add(mainMenuButton);

        javax.swing.SwingUtilities.invokeLater(() -> {
            dialog = optionPane.createDialog(message);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });

    }
}
