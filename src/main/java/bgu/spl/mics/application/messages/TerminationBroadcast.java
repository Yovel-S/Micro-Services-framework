package bgu.spl.mics.application.messages;
import bgu.spl.mics.Broadcast;

/**
 * a broad cast which sends to all services that are subscribes to it
 * in order to terminate their activity
 *
 */
public class TerminationBroadcast implements Broadcast {
    public TerminationBroadcast(){}


}