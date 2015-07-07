package com.bentyn.traincoll.android.train;

import com.bentyn.traincoll.commons.algorithms.AbstractCDAlgorithm;
import com.bentyn.traincoll.commons.algorithms.BasicCDAlgorithm;
import com.bentyn.traincoll.commons.algorithms.FixedSizeQueue;
import com.bentyn.traincoll.commons.data.TrainData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Kuba on 2015-07-07.
 */
public class TrainController {
    public static final String TRAIN_ID="TRAIN_A";
    private final static int TRAIN_QUEUE_SIZE=10;
    private Map<String,FixedSizeQueue<TrainData>>  trainsData = new ConcurrentHashMap<>();
    private FixedSizeQueue<TrainData> myPositions = new FixedSizeQueue<>(TRAIN_QUEUE_SIZE);
    private AbstractCDAlgorithm algorithm = new BasicCDAlgorithm();

    public void insertOrUpdate(TrainData trainData){
        if(trainsData.containsKey(trainData.getId())){
            trainsData.get(trainData.getId()).add(trainData);
        }else{
            FixedSizeQueue<TrainData> queue= new FixedSizeQueue<>(TRAIN_QUEUE_SIZE);
            queue.add(trainData);
            trainsData.put(trainData.getId(),queue);
        }
    }

    public void remove(String trainId){
        trainsData.remove(trainId);
    }


    public FixedSizeQueue <TrainData> getDataForTrain(String trainId){
        return trainsData.get(trainId);
    }

    public boolean checkForCollision(String trainId){
        FixedSizeQueue<TrainData> otherPositions = getDataForTrain(trainId);
        return algorithm.checkCollision(myPositions, otherPositions);
    }

    public TrainData addMyPosition(TrainData train){
        train.setId(TRAIN_ID);
        //TODO calculate heading and speed
        myPositions.add(train);
        return train;
    }
    public  TrainData getMyPosition(){
        if (myPositions.isEmpty()){
            return new TrainData();
        }
        return myPositions.getLast();
    }
    public FixedSizeQueue<TrainData> getMyPositions() {
        return myPositions;
    }

}
