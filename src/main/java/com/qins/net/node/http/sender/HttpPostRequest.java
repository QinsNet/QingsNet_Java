package com.qins.net.node.http.sender;

import com.qins.net.core.console.Console;
import com.qins.net.core.entity.RequestMeta;
import com.qins.net.core.entity.ResponseMeta;
import com.qins.net.node.core.Node;
import com.qins.net.util.Http2Util;
import com.qins.net.util.SerializeUtil;
import io.netty.handler.codec.http.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: YiXian
 * @Package: com.xianyu.yixian.com.ethereal.client.Core.Model
 * @ClassName: EchoClient
 * @Description: TCP客户端
 * @Author: Jianxian
 * @CreateDate: 2020/11/16 20:17
 * @UpdateUser: Jianxian
 * @UpdateDate: 2020/11/16 20:17
 * @UpdateRemark: 类的第一次生成
 * @Version: 1.0
 */
public class HttpPostRequest extends Node {
    OkHttpClient client;

    private void send(Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.setResponseMeta(new ResponseMeta(e));
                metaClass.getRequest().receive(context);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() != HttpResponseStatus.OK.code()){
                    context.setResponseMeta(new ResponseMeta("Http客户端:" + response.body().string()));
                    metaClass.getRequest().receive(context);
                    return;
                }
                ResponseMeta responseMeta = new ResponseMeta()
                        .setProtocol(Http2Util.urlDecode(response.headers().get("protocol"),"UTF-8"))
                        .setException(Http2Util.urlDecode(response.headers().get("exception"),"UTF-8"))
                        .setInstance(Http2Util.urlDecode(response.headers().get("instance"),"UTF-8"))
                        .setParams(SerializeUtil.gson.fromJson(Http2Util.urlDecode(response.headers().get("params"),"UTF-8"),HashMap.class))
                        .setResult(response.body().string());
                if(context.getParams() == null)context.setParams(new HashMap<>());
                context.setResponseMeta(responseMeta);
                metaClass.getRequest().receive(context);
            }
        });
    }

    @Override
    public boolean send(Object data,int timeout) {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
        if(data instanceof RequestMeta){
            Console.debug(data.toString());
            RequestMeta requestMeta = (RequestMeta) data;
            try {
                Request.Builder request =
                        new Request.Builder()
                        .url(new HttpUrl.Builder()
                                .scheme("http")
                                .host(context.getRemote().getHost())
                                .port(context.getRemote().getPort())
                                .addPathSegments(requestMeta.getMapping())
                                .build())
                        .addHeader(HttpHeaderNames.CONTENT_TYPE.toString(), HttpHeaderValues.APPLICATION_JSON.toString())
                        .addHeader("protocol", requestMeta.getProtocol());
                if(requestMeta.getParams() != null){
                    RequestBody requestBody = RequestBody.create(SerializeUtil.gson.toJson(requestMeta.getParams()).getBytes(StandardCharsets.UTF_8));
                    request.post(requestBody);
                }
                if(requestMeta.getInstance() != null){
                    request.addHeader("instance", URLEncoder.encode(requestMeta.getInstance(),"UTF-8"));
                }
                send(request.build());
                return true;
            } catch (UnsupportedEncodingException e) {
                metaClass.onException(e);
                return false;
            }
        }
        return false;
    }

}
