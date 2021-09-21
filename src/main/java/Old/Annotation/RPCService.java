package Old.Annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCService {
    String[] parameters =null;
    public Object authority = null;

}
