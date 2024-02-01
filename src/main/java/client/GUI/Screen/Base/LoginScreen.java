package client.GUI.Screen.Base;

import client.GUI.ScreenManager;
import client.GUI.Utilities;
import client.Manager.GameClient;
import entity.message.MessageType;

import javax.swing.*;
import java.awt.*;

import static client.GUI.GUIConstant.*;
import static client.GUI.Utilities.*;

public class LoginScreen extends BaseScreen {
    //#region DATA
    // Panel
    private JPanel containerPanel;
    // Field
    private JTextField usernameField;
    private JPasswordField passwordField;
    // Button
    private JButton loginButton;
    private JButton registerButton;
    // Label
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    //#endregion
    public LoginScreen() {
        SetDefaultValue(ScreenManager.Screen.LOGIN);
        onCreateBg();
        onCreatePanel();
        onCreateComponent();
        onAddComponent();
        onAddButtonListener();
    }

    private void onCreateBg(){
        // Create background image label
        setContentPane(Utilities.createBackground(BACKGROUND_IMAGE_PATH, SCREEN_WIDTH, SCREEN_HEIGHT));
        setLayout(new GridBagLayout());
    }

    private void onAddButtonListener(){
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Not connected to server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.LOGIN, username + "," + String.valueOf(password));
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            if (GameClient.Client.isNotConnected()) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Not connected to server!");
                return;
            }
            GameClient.Client.sendMsg(MessageType.REGISTER, username + "," + String.valueOf(password));
        });
    }

    private void onCreatePanel(){
        containerPanel = createRoundedPanel(false, 20, 0, 2);
        // Set layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        // Add the panel to the frame with GridBagLayout
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(containerPanel, gbc);

    }

    private void onCreateComponent(){
        usernameLabel = new JLabel(USERNAME_LABEL_TEXT);
        passwordLabel = new JLabel(PASSWORD_LABEL_TEXT);
        usernameField = createRoundedTextField();
        passwordField = createRoundedPasswordField();
        loginButton = createRoundedButton(LOGIN_BUTTON_TEXT);
        registerButton = createRoundedButton(REGISTER_BUTTON_TEXT);
    }

    private void onAddComponent(){
        containerPanel.add(usernameLabel);
        containerPanel.add(usernameField);
        containerPanel.add(passwordLabel);
        containerPanel.add(passwordField);
        containerPanel.add(loginButton);
        containerPanel.add(registerButton);
    }
}
