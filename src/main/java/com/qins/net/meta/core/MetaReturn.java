package com.qins.net.meta.core;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.lang.serialize.SerializeLang;
import com.qins.net.meta.annotation.Components;
import com.qins.net.meta.annotation.parameter.ParameterPact;
import com.qins.net.meta.annotation.returnval.ReturnPact;
import com.qins.net.util.AnnotationUtil;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Getter
@Setter
public class MetaReturn {
    protected BaseClass baseClass;
    protected SerializeLang serializeLang;

    public MetaReturn(Method method, Components components) throws NewInstanceException {
        try {
            baseClass = MetaApplication.getContext().getMetaClassLoader().loadClass(method.getReturnType());
            ReturnPact pact = AnnotationUtil.getMethodReturnPact(method);
            assert pact != null;
            this.serializeLang = pact.getSerializeLang();
        }
        catch (Exception e){
            throw new NewInstanceException(e);
        }
    }
}
