package bgu.spl.mics;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;

import bgu.spl.mics.application.objects.Model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GPUTest {
    private static GPU gpu;

    @Before
    public void setUp(){
        Cluster cluster = new Cluster();
        gpu = new GPU("RTX3090",cluster);
        GPU GPUs[] = {gpu};
        CPU CPUs[] = {new CPU(32,cluster)};
        cluster.setGPUsCPUs(GPUs, CPUs);
        Model model = new Model("model_test","Images",10000);
        gpu.setModel(model);
      }

    @Test
    public void setModel() {
        Model model = new Model("model_test","Images",10000);
        gpu.setModel(model);
        assertEquals(gpu.getModel(),model);
    }

    @Test
    public void sendDataBatch() {
        int before_size = gpu.getAvailableBandWidth();
        gpu.sendDataBatch();
        assertEquals(before_size-1,gpu.getAvailableBandWidth());
        gpu.sendDataBatch();
    }

    @Test
    public void setProcessedDataBatch() {
        DataBatch one_batch = new DataBatch(gpu.getModel().getModelData(), 0);
        int before_size = gpu.getProcessedDataInternalQueueSize();
        gpu.setProcessedDataBatch(one_batch);
        assertEquals(before_size + 1,gpu.getProcessedDataInternalQueueSize());
    }

    @Test
    public void trainProcessedData() {
        DataBatch one_batch = new DataBatch(gpu.getModel().getModelData(), 0);
        gpu.setProcessedDataBatch(one_batch);
        int cur_size = gpu.getProcessedDataSize();
        gpu.trainProcessedData();
        gpu.updateMyGlobalTicks(new TickBroadcast(10));
        gpu.trainProcessedData();
        assertEquals(gpu.getProcessedDataSize(),cur_size+1000);
        int tick = 20;
        for(int i=1000 ; i<gpu.getModelDataSize() ; i=i+1000) {
            gpu.setProcessedDataBatch(new DataBatch(gpu.getModel().getModelData(), i));
            gpu.trainProcessedData();
            tick = tick + 10;
            gpu.updateMyGlobalTicks(new TickBroadcast(tick));
            gpu.trainProcessedData();
        }
        assertEquals(true,gpu.isComplete());


    }

    @Test
    public void testModel() {
    }


}