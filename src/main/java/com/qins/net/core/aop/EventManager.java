package com.qins.net.core.aop;
import com.qins.net.core.aop.annotation.Event;
import com.qins.net.core.aop.annotation.EventContextParam;
import com.qins.net.core.aop.context.EventContext;
import com.qins.net.core.exception.TrackException;
import org.javatuples.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class EventManager {
    private static final Pattern pattern = Pattern.compile("\\w+");
    private HashMap<Pair<String, String>, Method> methodEvents = new HashMap<>();
    public void invokeEvent(Object instance,String function, HashMap<String, Object> params, EventContext context) throws TrackException, InvocationTargetException, IllegalAccessException {
        //这里性能会有问题，后期优化.
        java.util.regex.Matcher matcher = pattern.matcher(function);
        ArrayList<String> matches = new ArrayList<>();
        while(matcher.find()){
            matches.add(matcher.group());
        }
        if ((matches.size() % 2) != 0 || matches.size() < 2) throw new TrackException(TrackException.ExceptionCode.Initialize, String.format("%s不合法", function));
        String instanceName = matches.get(0);
        String mapping = matches.get(1);
        HashMap<String ,String > paramsMapping = new HashMap<>(matches.size()- 2);
        for (int i = 2; i < matches.size();)
        {
            paramsMapping.put(matches.get(i++), matches.get(i++));
        }
        Method method = methodEvents.get(new Pair<>(instanceName,mapping));
        if (method == null)
        {
            throw new TrackException(TrackException.ExceptionCode.Runtime, String.format("%s实例的%s方法未注册", instanceName,mapping));
        }
        Parameter[] parameterInfos = method.getParameters();
        Object[] eventParams = new Object[parameterInfos.length];
        for (int i = 0; i < eventParams.length; i++)
        {
            EventContextParam context_attribute = parameterInfos[i].getAnnotation(EventContextParam.class);
            if (context_attribute != null)
            {
                eventParams[i] = context;
                continue;
            }
            String name = paramsMapping.get(parameterInfos[i].getName());
            if (name!=null)
            {
                Object object = params.get(name);
                if (object != null)
                {
                    eventParams[i] = object;
                }
                else throw new TrackException(TrackException.ExceptionCode.Runtime,
                        String.format("%s方法发起%s事件时，发起方未提供自身的%s参数",context.getMethod().getName(), function,name));
            }
            else throw new TrackException(TrackException.ExceptionCode.Runtime,
                    String.format("%s方法发起%s事件时，发起方为定义被调用方的%s参数", context.getMethod(),function,parameterInfos[i].getName()));
        }
        method.invoke(instance, eventParams);
    }

    public void register(String name, Class<?> instance)
    {
        for (Method method : instance.getMethods())
        {
            Event attribute = method.getAnnotation(Event.class);
            if (attribute != null)
            {
                methodEvents.put(new Pair<>(name, attribute.mapping()), method);
            }
        }
    }
    public void unregister(String name, Class<?> instance)
    {
        for (Method method : instance.getMethods())
        {
            Event attribute = method.getAnnotation(Event.class);
            if (attribute != null)
            {
                methodEvents.remove(new Pair<>(name, attribute.mapping()), method);
            }
        }
    }
}
