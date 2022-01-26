package com.ethereal.meta.meta;

import com.ethereal.meta.meta.annotation.MetaMapping;
import com.ethereal.meta.net.core.Net;
import com.ethereal.meta.net.network.Network;
import com.ethereal.meta.net.network.Server;
import com.ethereal.meta.net.network.http.server.Http2Server;
import com.ethereal.meta.request.annotation.RequestMapping;
import com.ethereal.meta.request.core.RequestInterceptor;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

import java.lang.reflect.Field;

public abstract class Meta extends Net {

    public static  <T extends Meta> T connect(Class<T> metaClass) throws IllegalAccessException {
        //Proxy Instance
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(metaClass);
        RequestInterceptor interceptor = new RequestInterceptor();
        Callback noOp= NoOp.INSTANCE;
        enhancer.setCallbacks(new Callback[]{noOp,interceptor});
        enhancer.setCallbackFilter(method ->
        {
            if(method.getAnnotation(RequestMapping.class) != null){
                return 1;
            }
            else return 0;
        });
        T meta = (T)enhancer.create();
        for (Field field : metaClass.getFields()){
            if(field.getAnnotation(MetaMapping.class) != null){
                field.set(meta,connect((Class<T>) field.getType()));
            }
        }
        //Life Cycle
        meta.onConfigure();
        meta.onRegister();
        meta.onInitialize();
        return meta;
    }
    public static <T extends Meta> Server publish(Class<T> metaClass, String protocol) {
        if("http2".equals(protocol)){
            return new Http2Server(metaClass);
        }
        return null;
    }

}
