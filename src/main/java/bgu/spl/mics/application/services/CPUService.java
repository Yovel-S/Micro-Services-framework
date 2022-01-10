package bgu.spl.mics.application.services;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;

import java.util.concurrent.CountDownLatch;


/**
 * CPU service is responsible for handling the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private CountDownLatch start;
    private CountDownLatch end;

    public CPUService(String name, CPU _cpu, CountDownLatch _start, CountDownLatch _end) {
        super("CPUService "+name);
        cpu = _cpu;
        start = _start;
        end = _end;
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast->{
            terminate();
            end.countDown();
        });
        // subscribe to Tick broadcast
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast t)->
        {
            cpu.updateMyGlobalTicks(t);
            cpu.processDataBatch();
        });
        start.countDown();


    }
}
