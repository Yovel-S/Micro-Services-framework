package bgu.spl.mics.application.objects;
import java.util.HashMap; // import the HashMap class
import java.util.LinkedList;
import java.util.concurrent.*;
import bgu.spl.mics.application.objects.*;


/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
	private static class SingletonHolder {
		private static Cluster instance = new Cluster();
	}

	private transient GPU[] GPUs;
	private transient CPU[] CPUs;
	private transient BlockingQueue<DataBatch> rawData;
	private transient LinkedList<String> TrainedModelsNames = new LinkedList<String>();
	private int totalCPUsTime;
	private int totalGPUsTime;
	private int totalProcessedDataBatches;

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Cluster getInstance() {
		return SingletonHolder.instance;
	}

	//Radnom CPU pick. got random total process between 16-26K of batches

//				/**
//				 * Gets DataBatch from GPU in order to send to CPU.
//				 */
//				public void addRawDataBatch(DataBatch dataBatch, GPU gpu) {
//					int randomCPUindex = ThreadLocalRandom.current().nextInt(0, CPUs.length);
//					CPU randomCPU = CPUs[randomCPUindex];
//					randomCPU.addDataBatch(dataBatch, gpu);
//				}//should be thread-safe


	// SMART CPU pick Got most of times 32K batches. but also 13K 1-2times
	/**
	 * Gets DataBatch from GPU in order to send to CPU.
	 */
	public void addRawDataBatch(DataBatch dataBatch, GPU gpu) {
		int bestIndex=-1;
		int minTicks=Integer.MAX_VALUE;
		for(int i=0 ; i < CPUs.length ; i++){
			if(CPUs[i].getTicksToFinish()==0){
				bestIndex = i;
				break;
			}
			if(CPUs[i].getTicksToFinish() < minTicks){
				minTicks = CPUs[i].getTicksToFinish();
				bestIndex = i;
			}
		}
		CPUs[bestIndex].addDataBatch(dataBatch, gpu);

	}//should be thread-safe

	/**
	 * Returns RawDataBatch to CPU in order to send to GPU.
	 */
	public DataBatch getRawDataBatch() {
		return null;
	}//should be thread-safe


	/**
	 * Gets ProcessedDataBatch from CPU in order to send to GPU.
	 */
	public void setProcessedDataBatch(DataBatch databatch, GPU gpu) {
		gpu.setProcessedDataBatch(databatch);
	}//should be thread-safe

	public void setGPUsCPUs(GPU[] _GPUs, CPU[] _CPUs) {
		GPUs = _GPUs;
		CPUs = _CPUs;
	}

	// update 1 batch process and time used
	public synchronized void cpuUpdateStatistics(int time_used){
		totalProcessedDataBatches++;
		totalCPUsTime = totalCPUsTime + time_used;
	}

	public synchronized void gpuUpdateTimeUsed(int time_used){
		totalGPUsTime = totalGPUsTime + time_used;
	}

	public synchronized void updateTrainedModelName(String name){
		TrainedModelsNames.addLast(name);
	}

}

