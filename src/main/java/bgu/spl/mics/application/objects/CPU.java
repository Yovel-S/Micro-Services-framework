package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.messages.TickBroadcast;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {

    private class Pair{
        private DataBatch first;
        private GPU second;

        Pair(DataBatch _first , GPU _second){
            first = _first;
            second = _second;
        }
    }

    private int cores;
    private Queue<Pair> rawData;
    private Cluster cluster;
    private int myTimeCounter;
    private int myGlobalTicksCounter;
    private int cpuTimeUsed;
    private boolean isProcessingNow=false;
    private int ticksForOneProcess;
    private int thisProcessStatingTick;
    private AtomicInteger AtomicTicksToFinish  = new AtomicInteger(0);
    private Pair currentPairCPU;

    public CPU(int _cores, Cluster _cluster){
        cores = _cores;
        cluster = _cluster;
        rawData = new LinkedList<Pair>();
    }

    /**
     * Gets DataBatch from GPU(cluster/messagebys) and add it to the DataBatches container.
     * <p>
     * @param d    The DataBatch
     *               {@code e}.
     * @post raw DataBatch container's size increased by one
     */
    public synchronized void addDataBatch(DataBatch d, GPU gpu){
        if(d != null & gpu != null){
            Pair p = new Pair(d, gpu);
            rawData.add(p);
            boolean completeSet = false;
            int oldValue = AtomicTicksToFinish.get();
            while(!completeSet){
                completeSet = AtomicTicksToFinish.compareAndSet(oldValue,oldValue + dataTypetoTicks(p));
            }
        }


    }
    /**
     * Takes DataBatch from the container and process it.
     * <p>
     * @pre raw DataBatch container's size greater then 0
     * @post raw DataBatch container's size decreased by one.TimeCounter increased(num of ticks depends on data type and num of cores)
     * @post processed DataBatch container's size increased by one.
     * @post myTimeCounter increased(num of ticks depends on data type and num of cores)
     */
    public synchronized void processDataBatch(){
        if(!isProcessingNow){
            if(!rawData.isEmpty()){
                currentPairCPU = rawData.remove();
            }
            if(currentPairCPU != null){
                isProcessingNow = true;
                thisProcessStatingTick = getGlobalTime();
                ticksForOneProcess = dataTypetoTicks(currentPairCPU);
            }
        }
        if(isProcessingNow){
            if(thisProcessStatingTick+ticksForOneProcess<=getGlobalTime()){
                isProcessingNow = false;
                cluster.cpuUpdateStatistics(ticksForOneProcess);
                boolean completeSet = false;
                int oldValue = AtomicTicksToFinish.get();
                while(!completeSet){
                    completeSet = AtomicTicksToFinish.compareAndSet(oldValue,oldValue - ticksForOneProcess);
                }
                cpuTimeUsed = cpuTimeUsed + ticksForOneProcess;
                cluster.setProcessedDataBatch(currentPairCPU.first,currentPairCPU.second);
                ticksForOneProcess = -1;
                currentPairCPU = null;
            }
        }
    }


    /**
     * Sends processed DataBatch to the GPU(via cluster).
     * <p>
     * @pre processed DataBatch container's size greater then 0
     * @post processed DataBatch container's size decreased by one
     */


    public void updateMyGlobalTicks(TickBroadcast t){
        myGlobalTicksCounter = t.getTick();
    }
    /**
     * Gets global time.
     */
    public int getGlobalTime(){
        return myGlobalTicksCounter;
    }


    /**
     * Returns the total time this cpu processed.
     * @inv myTimeCounter>=0
     */
    public int getTimeCounter(){
        return cpuTimeUsed;
    }

    public int getTimeUsed(){
        return cpuTimeUsed;
    }
    public int getNumOfRawDB(){
        if(rawData==null)
            return 0;
        return rawData.size();
    }


    private int dataTypetoTicks(Pair p){
        int ticks;
        if(p.first==null){
            int j = 0;
        }
        String str = p.first.data.getType();
        if(str=="Images"){
            ticks = 4*(32/cores);
        }
        else if (str=="Text"){
            ticks = 2*(32/cores);
        }
        else{
            ticks = 1*(32/cores);
        }
        return ticks;

    }

    public int getTicksToFinish(){
        return AtomicTicksToFinish.get();
    }

}
