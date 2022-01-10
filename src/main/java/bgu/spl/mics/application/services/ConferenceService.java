package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private CountDownLatch start;
    private CountDownLatch end;
    private int currentTick;
    private ConfrenceInformation confrenceInformation;

    public ConferenceService(String _name,ConfrenceInformation _confrenceInformation, CountDownLatch _start, CountDownLatch _end) {
        super(_name);
        start = _start;
        end = _end;
        int currentTick=0;
        confrenceInformation =_confrenceInformation;
        start = _start;
        end = _end;
    }


    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast->{
            terminate();
            end.countDown();
        });
        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            currentTick=tickBroadcast.getTick();
            if(currentTick==confrenceInformation.getDate()) {
                sendBroadcast(new PublishConferenceBroadcast(confrenceInformation.getPapers(),confrenceInformation));
                confrenceInformation.setPublished();
                terminate();
                end.countDown();
            }
        });
        subscribeEvent(PublishResultsEvent.class, publishResultsEvent -> {
            confrenceInformation.addPaper(publishResultsEvent.getModel());
        });
        start.countDown();
    }
}
