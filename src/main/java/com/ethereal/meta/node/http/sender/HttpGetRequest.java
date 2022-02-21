package com.ethereal.meta.node.http.sender;

import com.ethereal.meta.core.console.Console;
import com.ethereal.meta.core.entity.RequestMeta;
import com.ethereal.meta.core.entity.ResponseException;
import com.ethereal.meta.core.entity.ResponseMeta;
import com.ethereal.meta.util.SerializeUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;
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
public class HttpGetRequest extends com.ethereal.meta.node.core.Node {
    OkHttpClient client;

    private void send(Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.setResponseMeta(new ResponseMeta("Http客户端:" + e.getMessage()));
                meta.getRequest().receive(context);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseMeta responseMeta = new ResponseMeta();
                if(response.headers().get("exception") != null){
                    responseMeta.setException(URLDecoder.decode(response.headers().get("exception"),"UTF-8"));
                }
                responseMeta.setProtocol(response.headers().get("protocol"));
                if(response.headers().get("instance") != null){
                    responseMeta.setInstance(URLDecoder.decode(response.headers().get("instance"),"UTF-8"));
                }
                responseMeta.setResult(Objects.requireNonNull(response.body()).string());
                context.setResponseMeta(responseMeta);
                meta.getRequest().receive(context);
            }
        });
    }

    @Override
    public boolean send(Object data,int timeout) {
        client = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
        if(data instanceof RequestMeta){
            Console.debug(data.toString());
            RequestMeta requestMeta = (RequestMeta) data;
            HttpUrl.Builder url = new HttpUrl.Builder()
                    .scheme("http")
                    .host(context.getRemote().getHost())
                    .port(Integer.parseInt(context.getRemote().getPort()))
                    .addPathSegments(requestMeta.getMapping().substring(1));
            for (Map.Entry<String ,String > params : requestMeta.getParams().entrySet()){
                url.addQueryParameter(params.getKey(),params.getValue());
            }
            Request.Builder request = new Request.Builder()
                    .get()
                    .url(url.build())
                    .addHeader(HttpHeaderNames.CONTENT_TYPE.toString(),HttpHeaderValues.APPLICATION_JSON.toString())
                    .addHeader("protocol", requestMeta.getProtocol())
                    .addHeader("instance", requestMeta.getInstance())
                    .addHeader("host", requestMeta.getHost())
                    .addHeader("port", requestMeta.getPort());
            send(request.build());
            return true;
        }
        return false;
    }
}
