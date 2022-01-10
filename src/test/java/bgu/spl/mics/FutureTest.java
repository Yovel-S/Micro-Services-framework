package bgu.spl.mics;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class FutureTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void get() {
        Future<Object> f=new Future<>();
        assertEquals(f.isDone(),false);
        Object obj = new Object();
        f.resolve(obj);
        assertEquals(f.isDone(),true);
        Object obj2=f.get();
        assert(obj2!=null);
    }

    @Test
    public void resolve() {
        Future<Object> f=new Future<>();
        Object obj = new Object();
        assertEquals(f.isDone(),false);
        f.resolve(obj);
        assertEquals(f.isDone(),true);
    }

    @Test
    public void isDone() {
        Future<Object> f=new Future<>();
        Object obj = new Object();
        assertEquals(f.isDone(),false);
        f.resolve(obj);
        assertEquals(f.isDone(),true);
    }

    @Test
    public void testGet() {
        Future<Object> f=new Future<>();
        assertEquals(f.isDone(),false);
        Object obj = new Object();
        f.resolve(obj);
        assertEquals(f.isDone(),true);
        long x=5;
        Object obj2=f.get(x,TimeUnit.MICROSECONDS);
        assertEquals(obj2,obj);
    }
}