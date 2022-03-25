package com.qins.net.core.lang.serialize;

import com.qins.net.core.exception.ObjectLangException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Getter
@Setter
public class FieldLang extends ObjectLang{
    Map<String, ObjectLang> children = new HashMap<>();
    //处理字符串,分解成三部分Name,同步描述,异步描述
    public FieldLang(String description) throws ObjectLangException {
        name = description;
        Stack<Character> signStack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<description.length();i++){
            char ch = description.charAt(i);
            if('{' == ch){
                if(signStack.empty()){
                    name = sb.toString();
                    sb = new StringBuilder();
                }
                else sb.append(ch);
                signStack.push(ch);
            }
            else if('}' == ch){
                signStack.pop();
                if(!signStack.empty())sb.append(ch);
            }
            else if('[' == ch){
                signStack.push(ch);
                sb.append(ch);
            }
            else if(']' == ch){
                signStack.pop();
                if(!signStack.empty())sb.append(ch);
            }
            else if(',' == ch && signStack.size() == 1){
                ObjectLang child = process(sb.toString());
                children.put(child.name,child);
                sb = new StringBuilder();
            }
            else sb.append(ch);
        }
        if(signStack.empty()){
            ObjectLang child = process(sb.toString());
            children.put(child.name,child);
        }
        else if('.' == signStack.peek()){
            sb.append('}');
            ObjectLang child = process(sb.toString());
            children.put(child.name,child);
        }
        else throw new ObjectLangException("语法错误:" + description);
    }
}
