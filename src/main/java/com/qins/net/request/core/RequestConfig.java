package com.qins.net.request.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @ProjectName: YiXian_Client
 * @Package: com.yixian.material.RPC
 * @ClassName: RPCNetRequestConfig
 * @Description: java类作用描述
 * @Author: Jianxian
 * @CreateDate: 2021/3/5 18:07
 * @UpdateUser: Jianxian
 * @UpdateDate: 2021/3/5 18:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class RequestConfig {
    int timeout = 30000;
}
