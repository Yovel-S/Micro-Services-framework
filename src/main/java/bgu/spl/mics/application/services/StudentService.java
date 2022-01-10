package bgu.spl.mics.application.services;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.*;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
    private Student student;
    private int currentTick;
    private CountDownLatch start;
    private CountDownLatch end;
    private Model[] models;
    private LinkedList<TrainModelEvent> untrainedModels;
    private Future<Model> trainModelFuture = null;
    private Future<Model> testModelFuture = null;

    public StudentService(String name,Student _student, CountDownLatch _start, CountDownLatch _end) {
        super("StudentService "+name);
        student = _student;
        start = _start;
        end = _end;
        models=student.getModels();
        modelsToEvents(); //Builds and assigns untrainedModels LinkedList
    }

    private void modelsToEvents(){
        untrainedModels = new LinkedList<>();
        for(int i=0; i<models.length;i++){
            models[i].setStudent(student);
            untrainedModels.add(new TrainModelEvent(models[i]));
        }
        /*untrainedModels.sort(new Comparator<TrainModelEvent>() {
            public int compare(TrainModelEvent m1, TrainModelEvent m2) {
                return m1.getEventModel().getModelData().getSize() - m2.getEventModel().getSize();
            }
        }); //sort Models to Train by minimum Data size*/
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminationBroadcast.class, terminationBroadcast->{
            terminate();
            end.countDown();
        });

        subscribeBroadcast(PublishConferenceBroadcast.class, publishConferenceBroadcast->{
            student.readPapers(publishConferenceBroadcast.getPapers());
        });

        subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
            currentTick = tickBroadcast.getTick();
            if(currentTick==55000){
            }
            /** Try getting result from testModelFuture **/
            if (testModelFuture != null) {
                if (testModelFuture.get(1, TimeUnit.MICROSECONDS) != null){
                    if (testModelFuture.get().getModelResults()) {
                        student.publishe();
                        sendEvent(new PublishResultsEvent(testModelFuture.get()));
                    }
                    testModelFuture=null;
                }

            }
            /**else, Try getting result from training of model and then test it**/
            else {
                if (trainModelFuture != null) {
                    if (trainModelFuture.get(1, TimeUnit.MICROSECONDS) != null) {
                        TestModelEvent testModelEvent = new TestModelEvent(trainModelFuture.get(), student);
                        testModelFuture = sendEvent(testModelEvent);
                        trainModelFuture=null;
                    }
                }
                /**else, Try training model **/
                else{
                    TrainModelEvent trainModelEvent = untrainedModels.poll();
                    if(trainModelEvent!=null){
                        trainModelFuture = sendEvent(trainModelEvent);
                    }
                }
            }
        });
        start.countDown();
    }
}
