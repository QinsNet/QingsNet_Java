package ServiceDemo;

import com.ethereal.net.core.manager.event.Annotation.Event;

public class EventClass {
    @Event(mapping = "after")
    public void Add(int ddd,String  s){
        System.out.println("After");
        System.out.println(ddd);
        System.out.println(s);
    }
}
