package bgu.spl.mics.application.objects;

import java.util.Locale;
import java.util.Objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }
    private Type type;
    private int processed;
    private int size;

    public Data(String _type, int _size){
        if(Objects.equals(_type.toLowerCase(), "images")){
            type=Type.Images;
        }
        else if(Objects.equals(_type.toLowerCase(), "text")){
            type=Type.Text;
        }
        else if(Objects.equals(_type.toLowerCase(), "tabular")){
            type=Type.Tabular;
        }
        processed=0;
        size=_size;
    }
    public String getType(){
        return type.toString();
    }
    public int getSize(){
        return size;
    }
    public int getProcessedSize(){
        return processed;
    }
    public synchronized void increaseProcessedByOne(){
        processed = processed + 1000;
    }
}
