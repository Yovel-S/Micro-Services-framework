package bgu.spl.mics;

import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CPUTest {
    public static CPU cpu;
    public static GPU gpu;


    @Before
    public void setUp() {
        Cluster cluster = new Cluster();
        cpu = new CPU(32,cluster);
        gpu = new GPU("RTX3090",cluster);
        GPU GPUs[] = {gpu};
        CPU CPUs[] = {cpu};
        cluster.setGPUsCPUs(GPUs, CPUs);

    }
    @Test
    public void addDataBatch(){
        Model model = new Model("model_test","Images",10000);
        DataBatch d = new DataBatch(model.getModelData(),0);
        int before = cpu.getNumOfRawDB();
        cpu.addDataBatch(d,gpu);
        int after = cpu.getNumOfRawDB();
        assertEquals(before+1,after);
    }

    @Test
    public void processDataBatch(){
        int timeBefore = cpu.getGlobalTime();
        Model model = new Model("model_test","Images",10000);
        DataBatch d = new DataBatch(model.getModelData(),0);
        cpu.addDataBatch(d,gpu);
        int rawBefore = cpu.getNumOfRawDB();
        cpu.processDataBatch();
        cpu.updateMyGlobalTicks(new TickBroadcast(10));
        cpu.processDataBatch();
        int timeAfter = cpu.getGlobalTime();
        int rawAfter = cpu.getNumOfRawDB();
        assertEquals(rawBefore-1,rawAfter);
        assert (timeBefore<timeAfter);
    }

}