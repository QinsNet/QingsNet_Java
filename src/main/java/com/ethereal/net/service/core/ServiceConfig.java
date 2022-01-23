package com.ethereal.net.service.core;

import com.ethereal.net.core.entity.RequestMeta;
import com.ethereal.net.core.entity.ResponseMeta;
import com.ethereal.net.request.core.Request;
import com.ethereal.net.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @ProjectName: YiXian_Client
 * @Package: com.yixian.material.RPC
 * @ClassName: RPCNetServiceConfig
 * @Description: java类作用描述
 * @Author: Jianxian
 * @CreateDate: 2021/3/5 17:47
 * @UpdateUser: Jianxian
 * @UpdateDate: 2021/3/5 17:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Getter
@Setter
public class ServiceConfig {
    protected Charset charset = StandardCharsets.UTF_8;

    public ServiceConfig(){

    }

}
