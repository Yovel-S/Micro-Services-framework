package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link }.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;
    private LinkedList<TestModelEvent> TestModelsQueue;
    private LinkedList<TrainModelEvent> TrainModelsQueue;
    private TrainModelEvent currentTrain;
    private int smallestModelSize = -1;
    private CountDownLatch start;
    private CountDownLatch end;



    public GPUService(String name, GPU _gpu, CountDownLatch _start, CountDownLatch _end) {
        super("GPUService "+name);
        gpu = _gpu;
        TestModelsQueue = new LinkedList<TestModelEvent>();
        TrainModelsQueue = new LinkedList<TrainModelEvent>();
        start = _start;
        end = _end;
    }

    private int sizeXdatatype(Model m) {
        String str = m.getDataType();
        int size = m.getSize();
        if (str == "Images") {
            size = size * 4;
        } else if (str == "Text") {
            size = size * 2;
        }
//        else {
//            size = size * 1;
//        }
        return size;

    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast->{
            terminate();
            end.countDown();
        });
        // subscribe to TestModelEv
        subscribeEvent(TestModelEvent.class,(TestModelEvent t)->
        {
            this.TestModelsQueue.addLast(t);
        });
        // subscribe to TrainModel
        subscribeEvent(TrainModelEvent.class,(TrainModelEvent t)->
        {
            this.TrainModelsQueue.addFirst(t);
        });
        // subscribe to TickEvents
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast t)->
        {
            gpu.updateMyGlobalTicks(t);
            gpu.trainProcessedData();
            // GPU holding null model OR hold real model and complete his training
            if(gpu.isComplete()){
                // if actually finish model process
                if(currentTrain != null){
                    complete(currentTrain, currentTrain.getEventModel());
                    currentTrain = null;
                    gpu.setModel(null);
                }
                // GPU is free now - Test any model who waiting for tests
                while(!TestModelsQueue.isEmpty()){
                    // test one model - announce mBUS for results **WRONG TYPE IN COMPLETE METHOD
                    TestModelEvent tempForTest = TestModelsQueue.removeFirst();
                    if(tempForTest.getEventModel() != null){
                        gpu.testModel(tempForTest.getEventModel(),tempForTest.getDegree());
                        complete(tempForTest,tempForTest.getEventModel());
                    }

                }
                // gpu take  model to process
                if(TrainModelsQueue.isEmpty() == false){
                    int bestIndex=0;
                    int bestApproxSize = sizeXdatatype(TrainModelsQueue.getFirst().getEventModel());
                    for(int i=1 ; i < TrainModelsQueue.size() ; i++){
                        if(sizeXdatatype(TrainModelsQueue.get(i).getEventModel()) < bestApproxSize){
                            bestIndex = i;
                            bestApproxSize = sizeXdatatype(TrainModelsQueue.get(i).getEventModel());
                        }

                    }
                    currentTrain = TrainModelsQueue.remove(bestIndex);
                    gpu.setModel(currentTrain.getEventModel());

                }

            }

            // lets try to take step in the processing
            if(currentTrain != null){
                // if gpu work on model and not training batch - try to send as much databatch as AvailableBW
                if(gpu.getIsTrainingBatchNow() == false){
                    int Available_BandWidth_Now = gpu.getAvailableBandWidth();
                    while(Available_BandWidth_Now>0){
                        gpu.sendDataBatch();
                        Available_BandWidth_Now--;
                    }
                }

            }
            gpu.trainProcessedData();
        });
        start.countDown();
    }

}
