package com.bentyn.traincoll.android.communication;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bentyn.traincoll.android.MainActivity;
import com.bentyn.traincoll.android.map.TrainMarkerController;
import com.bentyn.traincoll.android.train.TrainController;
import com.bentyn.traincoll.commons.algorithms.CDAlgorithmResponse;
import com.bentyn.traincoll.commons.communication.Message;
import com.bentyn.traincoll.commons.communication.MessageType;
import com.bentyn.traincoll.commons.data.EventData;
import com.bentyn.traincoll.commons.data.TrainData;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import de.tavendo.autobahn.WebSocketHandler;

public class MessageHandler extends WebSocketHandler{

   private MainActivity mainActivity;
    private static final String TAG="MESSAGE_HANDLER";

    private Gson gson;
    MessageController messageController;
    TrainMarkerController markerController;
    TrainController trainController;

    Toast toast;

    private boolean open =false;
    @Inject
    public MessageHandler(MainActivity mainActivity,Gson gson,TrainMarkerController markerController,TrainController trainController) {
        this.mainActivity = mainActivity;
        this.gson=gson;
        this.markerController = markerController;
        this.trainController=trainController;
    }

    @Override
    public void onOpen() {
        // send position_update
        open =true;
        messageController.sendMessage(MessageType.POSITION_UPDATE,trainController.getMyPosition());
        super.onOpen();
    }

    @Override
    public void onTextMessage(String msg) {
        //if position_update chceck collision (in new thread?)
        // if event show notiffication
        Message message =  gson.fromJson(msg, Message.class);
        Log.d(TAG, "Message recived: " + message);
        switch(message.getType()){
            case POSITION_UPDATE:

                TrainData train = gson.fromJson(message.getData(), TrainData.class);

                Log.i(TAG, "Train " + train.getId() + " position update was recived");
                // TODO ADD COLLISION DETECTION HERE
                trainController.insertOrUpdate(train);
                CDAlgorithmResponse algorithResponse = trainController.checkForCollision(train.getId());
                if (algorithResponse.isDetected()){
                    EventData collisionEventData = new EventData();
                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTimeInMillis(algorithResponse.getCollisionTimestamp());
                    collisionEventData.setPriority("HIGH");
                    collisionEventData.setText(
                            "Collision detected: " + train.getId() + " and " + TrainController.TRAIN_ID+
                            "./nPredicted time: "+ cal.getTime()
                    );

                    messageController.sendMessage(MessageType.EVENT, collisionEventData);
                    showEvent(collisionEventData);
                }
                // show updated train position on map
                markerController.insertOrUpdate(train,mainActivity.getGoogleMap(),mainActivity);
                break;
            case EVENT:
                EventData event =  gson.fromJson(message.getData(),EventData.class);
                Log.i(TAG, "Event received" + event);
                // show on screen
                showEvent(event);
                break;
            default:
                Log.i(TAG, "Unknown Message Type: " + message.getType());
                break;
        }
    }

    @Override
    public void onClose(int code, String reason) {
        // show notiffication connection lost
        Log.i(TAG, "Connection Lost");
        open =false;
        super.onClose(code, reason);
    }

    private void showEvent(EventData event){
        Context context = mainActivity;
        int duration = Toast.LENGTH_LONG;

         showToast(context, event.getText(), duration);
    }
    public void showToast (Context context, String text, int duration){
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context,text ,duration );
        toast.show();
    }
    public MessageController getMessageController() {
        return messageController;
    }

    public void setMessageController(MessageController messageController) {
        this.messageController = messageController;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
