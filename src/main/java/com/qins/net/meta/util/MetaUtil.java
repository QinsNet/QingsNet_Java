package com.qins.net.meta.util;

import java.util.Map;

public class MetaUtil {

    static String getID(Object instance,String name,Map<String,Object> hashToID){
        String id = name + "@" + System.identityHashCode(instance);
        Object obj = hashToID.get(id);
        if(obj == null){
            //如果没有就生成一个
            hashToID.put(id,instance);
            return id;
        }
        else {
            //如果有先判断一下是不是自己
            if(obj.equals(instance)){
                //是自己则直接返回
                return id;
            }
            else {
                int idx = System.identityHashCode(instance);
                //说明是新对象,但是Hash冲突了
                do {
                    id = name  + "@" +  (++idx);//冲突采用自增的方式去解决
                }
                while (hashToID.containsKey(id) && hashToID.get(id).equals(instance));//一定存在该Hash
                return id;
            }
        }
    }
}
