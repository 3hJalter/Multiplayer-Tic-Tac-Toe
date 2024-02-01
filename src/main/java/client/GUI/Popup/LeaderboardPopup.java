package client.GUI.Popup;

public class LeaderboardPopup extends BasePopup{
    @Override
    public void Show(String message) {
        //Message format: number_of_user, username_1,score_1,username_2,score_2,...username_n,score_n
        String[] messageArr = message.split(",");
        StringBuilder leaderboard = new StringBuilder();
        // first element is the number of users
        String header = String.format("%-10s %-20s %-10s", "Rank", "Username", "Score");
        leaderboard.append(header).append("\n");
        for(int i = 0; i < Integer.parseInt(messageArr[0]); i++){
            // rank length = 13, username length = 30, score length = 10
        String formattedString = String.format("%-14s %-20s %-10s", i + 1, messageArr[i * 2 + 1], messageArr[i * 2 + 2]);
            leaderboard.append(formattedString).append("\n");
        }
        String finalLeaderboard = leaderboard.toString();
        javax.swing.SwingUtilities.invokeLater(()
                -> javax.swing.JOptionPane.showMessageDialog(null, finalLeaderboard));
    }
}
