package com.ethereal.meta.meta;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.request.annotation.RequestAnnotation;
import com.ethereal.meta.request.core.Request;
import com.ethereal.meta.request.core.RequestInterceptor;
import com.ethereal.meta.util.AnnotationUtil;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Field;

public abstract class Meta extends Request {

    public static  <T extends Meta> T connect(Class<T> metaClass) throws IllegalAccessException {
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(metaClass);
        RequestInterceptor interceptor = new RequestInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method ->
        {
            if(AnnotationUtil.getAnnotation(method, RequestAnnotation.class) != null){
                return 1;
            }
            else return 0;
        });
        T meta = (T)enhancer.create();
        //Life Cycle
        meta.onConfigure();
        meta.onRegister();
        meta.onInstance();
        meta.onInitialize();
        return meta;
    }

    protected Meta() {
        try {
            for (Field field : this.getClass().getFields()){
                if(field.getAnnotation(MetaMapping.class) != null){
                    field.set(this,connect((Class<? extends Meta>) field.getType()));
                }
            }
        }
        catch (IllegalAccessException exception){
            onException(exception);
        }
    }
}
