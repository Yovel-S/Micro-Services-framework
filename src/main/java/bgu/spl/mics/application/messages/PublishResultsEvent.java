package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class PublishResultsEvent implements Event<Model> {
    private final Model model;

    public PublishResultsEvent(Model m){
        model = m;
    }

    public Model getModel(){
        return model;
    }
}
