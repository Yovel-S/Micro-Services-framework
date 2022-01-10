package bgu.spl.mics.application.objects;

import bgu.spl.mics.application.objects.*;
import com.google.gson.annotations.*;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name; /**Should bt in output file**/

    private transient String department;
    private transient Degree status;
    private transient int publications;
    private Model[] models; /**Should bt in output file**/
    private int papersRead;


    public String getName(){
        return name;
    }
    public String getdepartment(){
        return department;
    }
    /** @return false if MSc or True if PhD **/
    public boolean getDegree(){
        return status.equals(Degree.PhD);
    }
    public Model[] getModels(){
        return models;
    }
    public void readPapers(BlockingQueue<Model> papers){
        papersRead+=papers.size();
        for(Model model: papers){
            if(this==model.getStudent())
                publications++;
        }
    }
    public int getNumOfPapers(){return papersRead;}
    public void publishe(){publications++;}

    public Student(String name, String department, String status, Model[] _models){
        this.name = name;
        this.department = department;
        if(Objects.equals(status, "PhD")) {
            this.status = Degree.PhD;
        }
        else {
            this.status = Degree.MSc;
        }
        this.models = _models;
        publications=0;
        papersRead=0;
    }

}
