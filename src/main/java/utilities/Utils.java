package utilities;

import entity.message.ClientMessage;
import entity.message.MessageType;

public class Utils {
    public static ClientMessage stringToMessage(String receivedString) {
        String[] message = receivedString.split(":");
        // Message will be like this: "0:username,password"
        return new ClientMessage(MessageType.valueOf(message[0]), message[1]);
    }

    public static boolean ValidateClientMessage(String message){
        String[] messageArr = message.split(":");
        if(messageArr.length != 2){
            return false;
        }
        try{
            MessageType.valueOf(messageArr[0]);
        }catch(Exception e){
            return false;
        }
        return true;
    }

    public static boolean ValidateServerMessage(String message){
        String[] messageArr = message.split(":");
        if(messageArr.length != 3){
            return false;
        }
        try{
            MessageType.valueOf(messageArr[0]);
        }catch(Exception e){
            return false;
        }
        try{
            Integer.parseInt(messageArr[1]);
        }catch(Exception e){
            return false;
        }
        return true;
    }
}
