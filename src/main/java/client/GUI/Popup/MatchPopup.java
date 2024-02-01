package client.GUI.Popup;

import client.Manager.GameClient;
import entity.message.MessageType;

import javax.swing.*;

public class MatchPopup extends BasePopup {
    JDialog dialog;
    @Override
    public void Show(String message){
        dialog = new JDialog();
        JButton cancelMatch = new JButton("Cancel Match");
        cancelMatch.addActionListener(e -> {
            if (client.Manager.GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(client.GUI.ScreenManager.Ins().getCurrentScreen(), "You are not connected to the server!");
                return;
            }
            client.Manager.GameClient.Client.sendMsg(MessageType.CANCEL_MATCH, GameClient.Client.thisUser.username());
        });

        final JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{}, null);

        optionPane.add(cancelMatch);

        javax.swing.SwingUtilities.invokeLater(() -> {
            dialog = optionPane.createDialog(message);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        });
    }

    @Override
    public void Close(){
        super.Close();
        dialog.dispose(); // Close the popup
    }
}
