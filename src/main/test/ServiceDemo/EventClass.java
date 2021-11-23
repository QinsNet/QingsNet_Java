package ServiceDemo;

import com.ethereal.server.Core.Manager.Event.Annotation.Event;

public class EventClass {
    @Event(mapping = "after")
    public void Add(int ddd,String  s){
        System.out.println("After");
        System.out.println(ddd);
        System.out.println(s);
    }
}
