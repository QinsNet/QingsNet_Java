package com.qins.net.core.lang.serialize;

import com.qins.net.core.exception.ObjectLangException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Stack;

@Getter
@Setter
public class PrimitiveLang extends ObjectLang{
    public PrimitiveLang(String description){
        this.name = description;
    }
}
