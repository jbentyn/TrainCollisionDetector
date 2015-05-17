package com.bentyn.traincoll.android;

import com.bentyn.traincoll.android.map.TrainMarkerController;
import com.bentyn.traincoll.commons.communication.Message;
import com.bentyn.traincoll.commons.communication.MessageSerializer;
import com.bentyn.traincoll.commons.data.EventData;
import com.bentyn.traincoll.commons.data.EventDataSerializer;
import com.bentyn.traincoll.commons.data.TrainData;
import com.bentyn.traincoll.commons.data.TrainDataSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module (
        injects={
                com.google.gson.Gson.class,
               MainActivity.class
        }

)
public class MainModule {

    private MainActivity mainActivity;

    public MainModule(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Provides
    @Singleton
    public MainActivity getMainActivity(){
        return mainActivity;
    }

    @Provides
    public Gson provideGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Message.class, new MessageSerializer());
        gsonBuilder.registerTypeAdapter(EventData.class, new EventDataSerializer());
        gsonBuilder.registerTypeAdapter(TrainData.class, new TrainDataSerializer<TrainData>(TrainData.class));
        return gsonBuilder.create();
    }
    @Provides
    @Singleton
    public TrainMarkerController provideTrainMarkerController(){
        return new TrainMarkerController();
    }

}
