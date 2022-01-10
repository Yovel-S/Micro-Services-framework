package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PublishConferenceBroadcast implements Broadcast {
    private BlockingQueue<Model> papers;
    private ConfrenceInformation confrenceInformation;

    public PublishConferenceBroadcast(BlockingQueue<Model> _papers, ConfrenceInformation _confrenceInformation){
        papers = new LinkedBlockingQueue<Model>(_papers);
        confrenceInformation = _confrenceInformation;
    }

    public BlockingQueue<Model> getPapers(){
        return papers;
    }
    public ConfrenceInformation getConfrenceInformation(){
        return confrenceInformation;
    }

}
