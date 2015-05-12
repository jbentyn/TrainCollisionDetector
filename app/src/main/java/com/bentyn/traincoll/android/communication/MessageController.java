package com.bentyn.traincoll.android.communication;

import android.util.Log;

import com.bentyn.traincoll.android.MainActivity;
import com.bentyn.traincoll.commons.communication.Message;
import com.bentyn.traincoll.commons.communication.MessageType;
import com.google.gson.Gson;

import javax.inject.Inject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by Kuba on 2015-05-12.
 */
public class MessageController {

    private static final String TAG="MessageController";

    private WebSocketConnection connection;
    private Gson gson;
    private MessageHandler messageHandler;



    public final static String WS_URI = "ws://10.0.2.2:8080/TrainCollServer/collision";
    @Inject
    public MessageController(MessageHandler messageHandler,Gson gson) {
        this.messageHandler = messageHandler;
        this.messageHandler.setMessageController(this);
        this.connection = new WebSocketConnection();
        this.gson=gson;
    }

    public void connect() throws WebSocketException {
        connection.connect(WS_URI, messageHandler);
    }

    public void sendMessage (MessageType type,Object data){
        if (messageHandler.isOpen()) {
            Message message = new Message();
            message.setData(gson.toJsonTree(data));
            message.setType(type);
            System.out.println(message);
            connection.sendTextMessage(gson.toJson(message));
            Log.d(TAG, "Message send: " + message.toString());
        }else{
            Log.d(TAG, "Connection not opened");
        }
    }


    public WebSocketConnection getConnection() {
        return connection;
    }

    public void setConnection(WebSocketConnection connection) {
        this.connection = connection;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}
