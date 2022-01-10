package bgu.spl.mics;

import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    MessageBusImpl messageBus;
    ExampleEvent event;
    ExampleBroadcast broadcast;
    MicroService m;
    @Before
    public void setUp() throws Exception {
        messageBus =  MessageBusImpl.getInstance();
        event = new ExampleEvent("test");
        broadcast = new ExampleBroadcast("test");
        Cluster cluster = new Cluster();
        m = new GPUService("TestGPUSERVICE", new GPU("RTX3090", cluster), new CountDownLatch(0), new CountDownLatch(3));
    }

    @Test
    public void subscribeEvent() {
        assertEquals(messageBus.isSubscribedToEvent(event.getClass(), m),false);
        messageBus.subscribeEvent(event.getClass(), m);
        assertEquals(messageBus.isSubscribedToEvent(event.getClass(), m),true);
    }

    @Test
    public void subscribeBroadcast() {
        assertEquals(messageBus.isSubscribedToBroadCast(broadcast.getClass(), m),false);
        messageBus.subscribeBroadcast(broadcast.getClass(), m);
        assertEquals(messageBus.isSubscribedToBroadCast(broadcast.getClass(), m),true);
    }

    @Test
    public void complete() {
        messageBus.register(m);
        messageBus.subscribeEvent(event.getClass(),m);
        messageBus.sendEvent(event);
        assertEquals(messageBus.isComplete(event),false);
        messageBus.complete(event, "completed");
        assertEquals(messageBus.isComplete(event),true);
    }

    @Test
    public void sendBroadcast() {
        messageBus.register(m);
        messageBus.subscribeBroadcast(broadcast.getClass(),m);
        assertEquals(messageBus.isBroadcastSent(broadcast, m),false);
        messageBus.sendBroadcast(broadcast);
        assertEquals(messageBus.isBroadcastSent(broadcast, m),true);
    }

    @Test
    public void sendEvent() {
        messageBus.register(m);
        messageBus.subscribeEvent(event.getClass(),m);
        assertEquals(messageBus.isEventSent(event ,m),false);
        messageBus.sendEvent(event);
        assertEquals(messageBus.isEventSent(event ,m),true);
    }

    @Test
    public void register() {
        assertEquals(messageBus.isRegistered(m),false);
        messageBus.register(m);
        assertEquals(messageBus.isRegistered(m),true);
    }

    @Test
    public void unregister() {
        messageBus.register(m);
        assertEquals(messageBus.isRegistered(m),true);
        messageBus.unregister(m);
        assertEquals(messageBus.isRegistered(m),false);
    }

    @Test
    public void awaitMessage() throws InterruptedException {
        messageBus.register(m);
        messageBus.subscribeBroadcast(broadcast.getClass(),m);
        messageBus.sendBroadcast(broadcast);
        assertEquals(false,messageBus.isAwaitSucceded(m));
        messageBus.awaitMessage(m);
        assertEquals(true,messageBus.isAwaitSucceded(m));
    }
}