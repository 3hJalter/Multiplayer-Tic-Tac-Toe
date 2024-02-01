package client.GUI;

import client.GUI.Popup.*;
import client.GUI.Screen.Base.*;
import entity.message.ServerMessage;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class ScreenManager {
    private static ScreenManager instance;

    private final Map<Screen, BaseScreen> screens = new HashMap<>();
    @Getter
    private BaseScreen currentScreen;
    @Getter
    private BasePopup currentPopup;
    private final Map<Popup, BasePopup> popups = new HashMap<>();

    public static ScreenManager Ins() {
        if (instance == null) {
            instance = new ScreenManager();
        }
        return instance;
    }

    private String GetMessage(ServerMessage.StatusCode code) {
        String message = "";
        switch (code) {
            case C_200 -> message = "Success!";
            case C_401 -> message = "Number of argument is incorrect!";
            case C_402 -> message = "Username is currently in use!";
            case C_403 -> message = "Username or password is incorrect!";
            case C_404 -> message = "User is not online!";
            case C_405 -> message = "User is existed!";
            case C_406 -> message = "User can not added to database!";
            case C_407 -> message = "Room not found!";
            case C_408 -> message = "Room is full!";
            case C_409 -> message = "User not found or not playing!";
            case C_410 -> message = "User is not in room!";
            case C_411 -> message = "User is not owner of room!";
            case C_412 -> message = "Room is not ready!";
            case C_413 -> message = "Room is not playing!";
            case C_414 -> message = "Invalid cell value!";
        }
        return message;
    }

    public void ShowMessagePopup(ServerMessage.StatusCode code) {
        // Show error message dialog
        ShowPopup(Popup.CODE_MESSAGE, GetMessage(code));
    }


    public void ShowPopup(Popup popup, String message){
        BasePopup oPopup = popups.get(popup);
        if (oPopup == null) {
            oPopup = switch (popup) {
                case CODE_MESSAGE -> new CodeMessagePopup();
                case LEADERBOARD -> new LeaderboardPopup();
                case END_GAME -> new EndGamePopup();
                case MATCHING -> new MatchPopup();
            };
            popups.put(popup, oPopup);
        }
        oPopup.Show(message);
        currentPopup = oPopup;
    }

    private void ShowMessageDialog(String message) {
        // Show a dialog with message
        javax.swing.SwingUtilities.invokeLater(()
                -> javax.swing.JOptionPane.showMessageDialog(null, message));
    }


    public void UpdateNewCurrentScreen(){
        if (currentScreen != null) {
            currentScreen.onUpdateDataWhenOpen();
        }
    }

    public void OpenScreen(Screen screen) {
        BaseScreen oScreen = screens.get(screen);
        if (oScreen == null) {
            oScreen = switch (screen) {
                case LOGIN -> new LoginScreen();
                case MAIN -> new MainScreen();
                case ROOM -> new RoomScreen();
                case GAME -> new GameScreen();
            };
            screens.put(screen, oScreen);
        }
        oScreen.onUpdateDataWhenOpen();
        // If current screen is null -> Open new screen
        if (currentScreen == null) {
            currentScreen = oScreen;
            oScreen.setVisible(true);
        }
        // Close current screen if it is not null and not the same with the new screen
        else if (currentScreen != oScreen) {
            currentScreen.setVisible(false);
            currentScreen = oScreen;
            oScreen.setVisible(true);
        }
    }

    public enum Screen {
        LOGIN,
        MAIN,
        ROOM,
        GAME,
    }

    public enum Popup {
        CODE_MESSAGE,
        LEADERBOARD,
        END_GAME,
        MATCHING,
    }

}


