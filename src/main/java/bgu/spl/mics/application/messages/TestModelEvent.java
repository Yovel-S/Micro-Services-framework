package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<Model> {
    private Model model;
    private String results;
    boolean studentDegree;

    public TestModelEvent(Model m, Student student){
        model = m;
        studentDegree = student.getDegree();
    }

    public Model getEventModel(){
        return model;
    }

    public boolean getDegree(){
        return studentDegree;
    }
}
