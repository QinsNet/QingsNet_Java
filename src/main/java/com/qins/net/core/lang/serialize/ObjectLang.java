package com.qins.net.core.lang.serialize;

import com.qins.net.core.exception.ObjectLangException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Stack;

@Getter
@Setter
@AllArgsConstructor
public class ObjectLang {
    String name;
    public ObjectLang(){

    }

    public static ObjectLang process(String description) throws ObjectLangException {
        Stack<Character> signStack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<description.length();i++){
            char ch = description.charAt(i);
            if('{' == ch || '[' == ch){
                signStack.push(ch);
                sb.append(ch);
            }
            else if('}' == ch){
                sb.append(ch);
                while (!signStack.empty() && '.' == signStack.peek()){
                    sb.append('}');
                    signStack.pop();
                }
                signStack.pop();
                if(signStack.empty()){
                    return new FieldLang(sb.toString());
                }
            }
            else if(']' == ch){
                sb.append(ch);
                while (!signStack.empty() && '.' == signStack.peek()){
                    sb.append('}');
                    signStack.pop();
                }
                signStack.pop();
                if(signStack.empty()){
                    return new ArrayLang(sb.toString());
                }
            }
            else if('.' == ch){
                sb.append('{');
                signStack.push('.');
            }
            else if(',' == ch){
                while (!signStack.empty() && '.' == signStack.peek()){
                    sb.append('}');
                    signStack.pop();
                }
                sb.append(ch);
            }
            else sb.append(ch);
        }
        if(signStack.size() == 0){
            return new PrimitiveLang(sb.toString());
        }
        else throw new ObjectLangException("语法错误:" + description);
    }
}
