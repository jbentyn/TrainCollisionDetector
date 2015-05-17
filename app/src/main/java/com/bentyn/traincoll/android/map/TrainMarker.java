package com.bentyn.traincoll.android.map;

import com.bentyn.traincoll.commons.data.TrainData;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Kuba on 2015-05-17.
 */
public class TrainMarker {

    private TrainData trainData;
    private Marker marker;
    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public LatLng getLatLng() {
        return  new LatLng ( trainData.getLatitude(),trainData.getLongitude());
    }


    public TrainData getTrainData() {
        return trainData;
    }

    public void setTrainData(TrainData trainData) {
        this.trainData = trainData;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
