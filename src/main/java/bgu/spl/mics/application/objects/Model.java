package bgu.spl.mics.application.objects;
import bgu.spl.mics.application.objects.*;
import com.google.gson.annotations.Expose;

import java.util.Locale;


/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {
    enum Status {PreTrained, Training, Trained, Tested};
    enum Results {None, Good, Bad};
    private transient Data data;
    private String name;
    private transient int size;
    private Status status;
    private transient Results results;
    private boolean published;
    private transient Student student;

    public String getName(){
        return name;
    }

    public int getSize(){
        return size;
    }

    public String getDataType(){
        return data.getType();
    }


    public Model(String _name, String _type, int _size){
        name=_name;
        size=_size;
        data = new Data(_type,size);
        status = Status.PreTrained;
        results = Results.None;
        published=false;
    }

    /*
        Model(String _name, Data _data, Student _student){
        name = _name;
        data = _data;
        student = _student;
        status = PreTrained;
        results = None;
    }

    public Student gedModelStudent(){
        return student;
    }
    */
    public Student getStudent(){
        return student;
    }
    public void setStudent(Student _student){
        student = _student;
    }
    public void setPublished(){
        published=true;
    }
    public boolean isPublished(){return published;}
    public void setModelStatus(String _status){
        if (_status=="Training")
            status=Status.Training;
        else if (_status=="Tested")
            status=Status.Tested;
        else if (_status=="Trained")
            status=Status.Trained;
    }
    public void setModelResults(boolean _results){
        if(_results){
            results = Results.Good;
        }
        else{
            results = Results.Bad;
        }
    }
    public String getModelName(){
        return name;
    }
    public Data getModelData(){
        return data;
    }
    public boolean getModelResults(){
        if(results== Results.Good)
            return true;
        return false;
    }

}
