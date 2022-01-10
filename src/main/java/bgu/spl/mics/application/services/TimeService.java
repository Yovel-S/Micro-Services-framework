package bgu.spl.mics.application.services;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminationBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int duration;
	private int tickTime;
	private int currentTick;
	private CountDownLatch end;


	public TimeService(int _duration, int _tickTime, CountDownLatch _end) {
		super("TimeService");
		currentTick = 1;
		duration = _duration;
		tickTime = _tickTime;
		end=_end;
	}

	@Override
	protected void initialize() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				if (currentTick > duration) {
					sendBroadcast(new TerminationBroadcast());
					timer.cancel();
					terminate();
				} else {
					sendBroadcast(new TickBroadcast(currentTick));
					currentTick = currentTick + 1;
				}
			}

		}, 0, tickTime);
		this.subscribeBroadcast(TerminationBroadcast.class, callback -> {
			this.terminate();
			end.countDown();
		});
	}
}
