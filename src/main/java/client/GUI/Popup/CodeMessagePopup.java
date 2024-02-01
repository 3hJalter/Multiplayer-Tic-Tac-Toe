package client.GUI.Popup;

public class CodeMessagePopup extends BasePopup{
    @Override
    public void Show(String message) {
        javax.swing.SwingUtilities.invokeLater(()
                -> javax.swing.JOptionPane.showMessageDialog(null, message));
    }
}
