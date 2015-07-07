package com.bentyn.traincoll.android.map;

import android.graphics.Color;
import android.util.Log;

import com.bentyn.traincoll.android.MainActivity;
import com.bentyn.traincoll.android.train.TrainController;
import com.bentyn.traincoll.commons.data.TrainData;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by Kuba on 2015-05-17.
 */
public class TrainMarkerController {
    private Map<String,TrainMarker> trainMarkerMapping = new HashMap<>();

    private static final String TAG="TrainMarker";

    public void insertOrUpdate (TrainData train, GoogleMap googleMap, MainActivity mainActivity){
        if (trainMarkerMapping.containsKey(train.getId())){
            // update
            TrainMarker trainMarker = trainMarkerMapping.get(train.getId());
            trainMarker.setTrainData(train);
            //update marker
            trainMarker.getMarker().remove();
            addMarker(trainMarker, train.getId(), googleMap,mainActivity);

        }else{
            //insert

            TrainMarker trainMarker = new TrainMarker();
            trainMarker.setTrainData(train);
            if (train.getId().equals(TrainController.TRAIN_ID)){
                trainMarker.setColor(MainActivity.MARKER_COLOR);
            }else {
                trainMarker.setColor(randomColor());
            }
            addMarker(trainMarker, train.getId(), googleMap, mainActivity);
            trainMarkerMapping.put(train.getId(), trainMarker);
        }

    }

    private int randomColor(){
        Random rnd = new Random();
        int color= Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        if (color == MainActivity.MARKER_COLOR){
            color=Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }
        return color;
    }

    private void addMarker(TrainMarker trainMarker,String trainId,GoogleMap googleMap, MainActivity mainActivity){
        MarkerOptions options= new MarkerOptions();
        options.position(trainMarker.getLatLng());
        options.title(trainId);
        options.anchor(0.5f,0.5f);
        IconGenerator generator=new IconGenerator(mainActivity);
        generator.setColor(trainMarker.getColor());
        BitmapDescriptor icon=BitmapDescriptorFactory.fromBitmap(generator.makeIcon(trainId));
        options.icon(icon);
        trainMarker.setMarker(googleMap.addMarker(options));
    }

    public void remove(String trainId){
        if (trainMarkerMapping.containsKey(trainId)){
            TrainMarker tm=trainMarkerMapping.get(trainId);
            tm.getMarker().remove();
            trainMarkerMapping.remove(trainId);
        }
    }

    public void removeOutOfRange(TrainData thisTrain, double range) {

        LatLng currentPosition= new LatLng(thisTrain.getLatitude(),thisTrain.getLongitude());
        Iterator<Map.Entry<String,TrainMarker>> iter=trainMarkerMapping.entrySet().iterator();


        while(iter.hasNext()){
            Map.Entry <String,TrainMarker> entry = iter.next();
            TrainMarker tm = entry.getValue();
            if (SphericalUtil.computeDistanceBetween(currentPosition,tm.getLatLng()) > range){
                remove(tm.getTrainData().getId());
                Log.i(TAG,"Train with ID: "+tm.getTrainData().getId()+" was removed from map");
            }

        }
    }
}
