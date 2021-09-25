package Net.NetNode.NetNodeServer.Service;

import Net.NetNode.Model.NetNode;
import Net.NetNode.NetNodeServer.Request.ClientNodeRequest;
import Server.Abstract.Token;
import Service.Abstract.ServiceConfig;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ServerNodeService {
    private ServiceConfig config;
    private HashMap<String, Pair<Token, NetNode>> netNodes = new HashMap<>();
    private ClientNodeRequest distributeRequest;
    private Random random = new Random();

    public HashMap<String, Pair<Token, NetNode>> getNetNodes() {
        return netNodes;
    }

    public void setNetNodes(HashMap<String, Pair<Token, NetNode>> netNodes) {
        this.netNodes = netNodes;
    }

    public ClientNodeRequest getDistributeRequest() {
        return distributeRequest;
    }

    public void setDistributeRequest(ClientNodeRequest distributeRequest) {
        this.distributeRequest = distributeRequest;
    }

    /// <summary>
    /// 获取对应服务的网络节点
    /// </summary>
    /// <param name="sender"></param>
    /// <param name="servicename"></param>
    /// <returns></returns>

    public NetNode GetNetNode(Token sender, String servicename)
    {
        //负载均衡的优化算法后期再写，现在采取随机分配
        List<NetNode> nodes = new ArrayList<NetNode>();
        Pair<Token,NetNode> pair = null;
        for(int i = 0; i<getNetNodes().size();i++)
        {
            if (pair.getValue().getServices().containsKey(servicename))
            {
                nodes.add(pair.getValue());
            }
        }
        if(nodes.size() > 0)
        {
            //成功返回对应节点
            return nodes[random.nextInt(0, nodes.size())];
        }
        return null;
    }
    // <summary>
    /// 如果断开连接，字典中删掉该节点
    /// </summary>
    /// <param name="token"></param>
    private void Sender_DisConnectEvent(Token token)
    {
       getNetNodes().remove((String)token.getKey());
       System.out.print("成功删除节点{(token.Key)}");
        StringBuilder sb = new StringBuilder();

        for (Pair<Token, NetNode> tuple in NetNodes.Values)
        {
            sb.AppendLine($"{tuple.Item2.Name}");
        }
        System.out.println($"当前节点信息:\n{sb}");
    }
}
