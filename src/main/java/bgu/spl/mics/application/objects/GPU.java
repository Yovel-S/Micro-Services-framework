package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.messages.TickBroadcast;


import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private int myTimeCounter = 0;
    private int myGlobalTicksCounter;
    private int gpuTimeUsed = 0;
    private Queue<DataBatch> rawData;
    private BlockingQueue<DataBatch> processedData;
    private int vramSize;
    private Queue<DataBatch> trainedData;
    private int modelDataSize;//total size of this Model data.
    private int availableBandWidth;
    private boolean isComplete = false;
    private boolean isTrainingNow;
    private int ticksForOneTrain;
    private int thisTrainStartingTick;


    public GPU(){
    }
    public GPU(String _type, Cluster _cluster){
        cluster = _cluster;
        if(Objects.equals(_type, "RTX3090")){
            type = Type.RTX3090;
            vramSize = 32;
            ticksForOneTrain = 1;
        }
        else if(Objects.equals(_type, "RTX2080")) {
            type = Type.RTX2080;
            vramSize = 16;
            ticksForOneTrain = 2;
        }
        else if(Objects.equals(_type, "GTX1080")) {
            type = Type.GTX1080;
            vramSize = 8;
            ticksForOneTrain = 2;
        }
        availableBandWidth = vramSize;

        rawData = new LinkedList<DataBatch>();
        processedData = new LinkedBlockingDeque<DataBatch>(vramSize);
        trainedData = new LinkedList<DataBatch>();

    }

    /**
     * set Model to this GPU
     * @param _model		the {@link Model}
     * @return return the result of type T if it is available, if not,
     * @post getModel()==model
     */
    public void setModel(Model _model){
        rawData.clear();
        processedData.clear();
        trainedData.clear();
        model = _model;
        isComplete = false;
        isTrainingNow = false;
        if(_model != null){
            dataSlicer(); // set the data in rawData as databatch
            modelDataSize = _model.getSize();
            availableBandWidth = vramSize;
            _model.setModelStatus("Training");
        }

    }
    /**
     * send DataBatch from rawData to Cluster (heading to cpu) for processing
     * if there's no data to send or availableBandWidth is empty - do nothing
     * @pre getModel!=null
     * @pre availableBandWidth >= 0
     * @post if(availableBandWidth > 0){ availableBandWidth--}
     */
    public void sendDataBatch(){
        if(rawData.isEmpty()==false){
            if(getAvailableBandWidth()>0){
                cluster.addRawDataBatch(rawData.remove(),this);
                availableBandWidth--;
            }
        }

    }
    /**
     * //set processed DataBatch in the relevant container
     * @param one_batch	data from the cluster
     * @pre processedData.size() < vramSize
     * @post processedData.size()++
     */

    public void setProcessedDataBatch(DataBatch one_batch){
        //should be synchronized
        try {
            processedData.put(one_batch);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * //train processed Data
     * @post if(processedData.size()>0) {trainedData.size()++}
     * @post if(trainedData.size()==dataSize){isComplete=true}
     */
    public void trainProcessedData(){
        // ** !!! *** this action should  take X time/tics
        // worktime  should by increased
        if(processedData.isEmpty()==false){
            if(isTrainingNow==false){
                //at least one bach availble . train will start now
                isTrainingNow = true;
                thisTrainStartingTick = getGlobalTime();
            }
            //train is already working , waiting X-Ticks before success
            else{
                // success
                if((thisTrainStartingTick+ticksForOneTrain)<= getGlobalTime()){
                    trainedData.add(processedData.remove());
                    model.getModelData().increaseProcessedByOne();
                    gpuTimeUsed = gpuTimeUsed + ticksForOneTrain;
                    cluster.gpuUpdateTimeUsed(ticksForOneTrain);
                    isTrainingNow = false;
                    availableBandWidth++;
                }
            }
        }
    }

    /**
     * //random test for the model
     * @param model	to test
     * @param student_degree false represent MSc student and True represent PhD
     * @return true: by 60%  MSc and by 80% for PhD. else: false
     */
    public void testModel(Model model, boolean student_degree){
        int randomNum = ThreadLocalRandom.current().nextInt(1,101);
        model.setModelStatus("Tested");
        model.setModelResults(false);
        //MSc student 60% true
        if(student_degree == false){
            if (randomNum <= 60){
                model.setModelResults(true);
            }
        }
        else{ if(randomNum <= 80){
            model.setModelResults(true);
        }
        }
    }

    /**
     * // return the status of current model training
     * @return true if the model training is complete
     *
     */
    public boolean isComplete(){
        if(this.model == null){
            return true;
        }
        if(isComplete == false){
            if(model.getModelData().getProcessedSize() == model.getModelData().getSize()){
                isComplete = true;
                model.setModelStatus("Trained");
                cluster.updateTrainedModelName(model.getName());
            }
        }
        return isComplete;
    }

    /**
     * //random test for the model
     * @return currnet model
     */
    public Model getModel(){
        return this.model;
    }


    /**
     * slice the model data to Batches and assign it in GPU.rawData
     * @post this.rawData!=null
     */
    private void dataSlicer(){
        Data data = this.model.getModelData();
        for(int i=0 ; i < data.getSize() ; i=i+1000){
            DataBatch sole_daba = new DataBatch(data,i);
            rawData.add(sole_daba);
        }
    }



    /**
     * returns the total amount of GPU work
     */
    public int getTimeCounter(){
        return 1;
    }

    public int getTimeUsed(){
        return gpuTimeUsed;
    }


    public int getProcessedDataSize(){
        return model.getModelData().getProcessedSize();
    }

    public int getProcessedDataInternalQueueSize(){
        return this.processedData.size();
    }

    public int getAvailableBandWidth(){
        return availableBandWidth;
    }

    public int getModelDataSize(){
        return modelDataSize;
    }

    public boolean getIsComplete(){
        return isComplete;
    }

    public boolean getIsTrainingBatchNow(){
        return isTrainingNow;
    }


    public void updateMyGlobalTicks(TickBroadcast t){
        myGlobalTicksCounter = t.getTick();
    }
    /**
     * Gets global time.
     */
    public int getGlobalTime(){
        return myGlobalTicksCounter;
    }

}
