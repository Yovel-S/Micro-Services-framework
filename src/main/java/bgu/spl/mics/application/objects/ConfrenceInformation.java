package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private transient BlockingQueue<Model> papers;
    private LinkedList<String> PublishedPapers;

    public ConfrenceInformation(String _name,int _date){
        name = _name;
        date = _date;
        papers = new LinkedBlockingQueue<Model>();
        PublishedPapers = new LinkedList<>();
    }

    public String getName(){
        return name;
    }

    public int getDate(){
        return date;
    }


    public void setPublished(){
        Iterator<Model> it = papers.iterator();
        while(it!=null&&it.hasNext())
            it.next().setPublished();
    }

    public void addPaper(Model paper){
        papers.add(paper);
        PublishedPapers.add(paper.getName());
    }
    public BlockingQueue<Model> getPapers(){
        return papers;
    }

}
