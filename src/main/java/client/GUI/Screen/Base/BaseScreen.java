package client.GUI.Screen.Base;

import client.GUI.GUIConstant;
import client.GUI.ScreenManager;

import javax.swing.*;

public abstract class BaseScreen extends JFrame {
    public ScreenManager.Screen type;
    public void SetDefaultValue(ScreenManager.Screen type) {
        this.type = type;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(GUIConstant.SCREEN_WIDTH, GUIConstant.SCREEN_HEIGHT);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    public void onUpdateDataWhenOpen() {
    }
}
