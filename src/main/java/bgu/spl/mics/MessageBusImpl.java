package bgu.spl.mics;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.services.*;
import java.util.HashMap;
import java.util.Queue;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentLinkedQueue<MicroService> microServicesQueue = new ConcurrentLinkedQueue<>();
	private ConcurrentHashMap<Class<? extends Message>, ConcurrentLinkedQueue<MicroService>> msgToMicroService = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceToQueue = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Event, Future> eventToFuture = new ConcurrentHashMap<>();
	private AtomicBoolean isProgramTerminated = new AtomicBoolean(false);

	private static class MessageBusHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}

	public <T> boolean isSubscribedToEvent(Class<? extends Event<T>> type, MicroService m){
		if(msgToMicroService.get(type)!=null)
			if(msgToMicroService.get(type).contains(m))
				return true;
		return false;
	}

	public <T> boolean isSubscribedToBroadCast(Class<? extends Broadcast> type, MicroService m){
		if(msgToMicroService.get(type)!=null)
			if(msgToMicroService.get(type).contains(m))
				return true;
		return false;
	}

	public <T> boolean isComplete(Event<T> e){
		return eventToFuture.get(e).isDone();
	}

	public boolean isBroadcastSent(Broadcast b, MicroService m){
		if(microServiceToQueue.get(m)!=null)
			if(microServiceToQueue.get(m).contains(b))
				return true;
		return false;
	}

	public <T> boolean isEventSent(Event<T> e, MicroService m){
		if(microServiceToQueue.get(m)!=null)
			if(eventToFuture.get(e)!=null)
				return true;
		return false;
	}

	public boolean isRegistered(MicroService m){
		if(microServiceToQueue.get(m)==null)
			return false;
		return true;
	}


	public boolean isAwaitSucceded(MicroService m) {
		if (this.microServiceToQueue.get(m).size() == 0)
			return true;
		return false;
	}

	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		msgToMicroService.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		synchronized (msgToMicroService.get(type)) {
			if (!msgToMicroService.get(type).contains(m)) {
				msgToMicroService.get(type).add(m);
			}
		}
	}

	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		msgToMicroService.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		synchronized (msgToMicroService.get(type)) {
			if (!msgToMicroService.get(type).contains(m))
				msgToMicroService.get(type).add(m);
		}

	}

	public <T> void complete(Event<T> e, T result) {
		eventToFuture.get(e).resolve(result);
	}

	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> interestedMicroServices = msgToMicroService.get(b.getClass());
		if (interestedMicroServices == null)
			return;
		synchronized (interestedMicroServices) {
			for (MicroService m : interestedMicroServices) {
				if (microServiceToQueue.get(m) != null)
					microServiceToQueue.get(m).add(b);
			}
		}
	}

	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = new Future<>();
		eventToFuture.put(e, future);
		ConcurrentLinkedQueue<MicroService> microServicesList = msgToMicroService.get(e.getClass());
		MicroService ms;
		synchronized (msgToMicroService.get(e.getClass())) {
			if (microServicesList == null || microServicesList.isEmpty()) {
				future.resolve(null);
				return null;
			}
			ms = microServicesList.poll();
			microServicesList.add(ms);
		}
		synchronized (ms) {
			BlockingQueue<Message> msEventsQueue = microServiceToQueue.get(ms);
			/*if(msEventsQueue==null){
				future.resolve(null);
				return null;
			}*/ //should we check??
			msEventsQueue.add(e);
		}
		return future;
	}

	public void register(MicroService m) {
		BlockingQueue<Message> queue = new LinkedBlockingQueue();
		microServiceToQueue.put(m, queue);
	}

	public void unregister(MicroService m) {
		BlockingQueue<Message> queueToDelete;
		synchronized (m) {
			queueToDelete = microServiceToQueue.remove(m);
		}
		while (!queueToDelete.isEmpty()) {
			if (queueToDelete.peek() instanceof Event) {
				Event<?> event = (Event) queueToDelete.poll();
				Future<?> eventToDelete = eventToFuture.get(event);
				eventToDelete.resolve(null);
			} else
				queueToDelete.poll();
		}
		for (ConcurrentLinkedQueue<MicroService> val : msgToMicroService.values()) {
			synchronized (val) {
				val.remove(m);
			}
		}
	}

	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message msg = null;
		if (!microServiceToQueue.containsKey(m))
			throw new IllegalStateException("The microservice " + m + " is not registered to message bus");
		else {
			msg = microServiceToQueue.get(m).take();
		}
		return msg;
	}

}
