package com.qins.net.core.lang.serialize;

import com.qins.net.core.exception.ObjectLangException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Getter
@Setter
public class ArrayLang extends ObjectLang{
    List<ObjectLang> objects = new ArrayList<>();
    public ArrayLang(String description) throws ObjectLangException {
        name = description;
        Stack<Character> signStack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<description.length();i++){
            char ch = description.charAt(i);
            if('[' == ch){
                if(signStack.empty()){
                    name = sb.toString();
                    sb = new StringBuilder();
                }
                else sb.append(ch);
                signStack.push(ch);
            }
            else if('}' == ch){
                signStack.pop();
                sb.append(ch);
            }
            else if('{' == ch){
                signStack.push(ch);
                sb.append(ch);
            }
            else if(']' == ch){
                signStack.pop();
                if(!signStack.empty()) sb.append(ch);
            }
            else if('.' == ch && signStack.size() == 0){
                if(signStack.empty()){
                    name = sb.toString();
                    sb = new StringBuilder();
                }
                signStack.push(ch);
                sb.append(ch);
            }
            else if(',' == ch && signStack.size() == 1){
                ObjectLang child = process(sb.toString());
                objects.add(child);
                sb = new StringBuilder();
            }
            else sb.append(ch);
        }
        if(signStack.size() == 0){
            ObjectLang child = process(sb.toString());
            objects.add(child);
        }
    }
}
