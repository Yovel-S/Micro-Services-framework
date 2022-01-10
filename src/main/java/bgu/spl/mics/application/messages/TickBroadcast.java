package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;


/**
 * a broadcast which is send to all services which are subscribe to it
 * in order to inform about tick
 */
public class TickBroadcast implements Broadcast {
    private int tick;

    /**
     * @param int tick-update tick to time
     */
    public TickBroadcast(int time){
        tick=time;
    }

    /**
     * @return tick
     */
    public int getTick(){
        return tick;
    }
}