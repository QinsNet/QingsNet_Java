package com.ethereal.net.net.core;

import com.ethereal.net.net.config.NodeConfig;
import lombok.Getter;
import lombok.Setter;

/**
 * @ProjectName: YiXian_Client
 * @Package: com.yixian.material.RPC
 * @ClassName: RPCNetConfig
 * @Description: java类作用描述
 * @Author: Jianxian
 * @CreateDate: 2021/3/5 18:10
 * @UpdateUser: Jianxian
 * @UpdateDate: 2021/3/5 18:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Getter
@Setter
public abstract class NetConfig {
    NodeConfig node;
}
