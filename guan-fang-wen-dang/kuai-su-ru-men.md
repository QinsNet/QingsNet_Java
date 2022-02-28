---
description: 带您快速了解Ethereal
---

# 快速入门

## 引论

### QinsNet

> **状态同步式网络服务框架**

### 单机版的网络化

> “网络的透明化是单机”

  这里有一个经典的需求：客户端和服务端的登录请求（login）

  我们先假设以**单机**来写这段逻辑

  逻辑思路：服务收到User信息，对User实体进行赋置，然后返回逻辑值。

```c#
// DTO实体
public class User{
    String username;
    String password;
    long id;
}
// 服务端
public class UserService{
    public boolean login(User user){
        if(user.username == "username" && user.password == "password"){
            user.id = 1112;
            return true;
        }
        else return false;
    }
}
// 客户端
public interface UserService{
    public User login(User user);
}
```

  我们再来看一下**网络**化的逻辑

  客户端发送相应字段信息，服务端服务时，根据字段信息，登录成功则**返回一个User实体**，登录失败则返回null值。

```c#
// DTO实体
public class User{
    String username;
    String password;
    long id;
}
// 服务端
public class UserService{
    public User login(User user){
        if(user.username == "username" && user.password == "password"){
            user.id = 1112;
            return user;
        }
        else return null;
    }
}
// 客户端
public interface UserService{
    public User login(String username,String password);
}
```

#### 问题一[逻辑]：如果将登录视为一种权限验证，那理应返回逻辑值真假

  上述代码之所以没有返回逻辑值真假，本质是通过判断返回结果是否为null来决定。

  所以我们可以猜到客户的的逻辑代码为：

```c#
UserService userService = new UserService();
User user = new User("username","password");
user = userService.login(username,password);
if(user != null){
    Console.print("登录成功:" + user.id);
}
else Console.print("登录失败");
```

  我们不妨以纯粹的单机写法来试试看：

```c#
UserService userService = new UserService();
User user = new User("username","password");
if(userService.login(User)) {
    Console.print("登录成功:" + user.id);
}
else Console.print("登录失败");
```

  我们可以发现，代码节省了一行，并且在逻辑上更贯通[判断登录成功与否，而不是判断登录是否有返回值]

  事实上，网络版会更复杂一些

```c#
//在之前逻辑中，如果user为空，那么之前的user信息也会丢失
//所以一般又会单独开辟一个新的字段（result）来存储登录的结果值
UserService userService = new UserService();
User user = new User("username","password");
User result = userService.login(username,password);
if(result != null){
	user = result;
    Console.print("登录成功:" + user.id);
}
else Console.print("登录失败");
```

​    像上述这样的网络化细节问题不计其数，从最简单的单机版向复杂的网络版跃迁时，简明的单机版逻辑将面目全非。

#### 问题二[设计]：BO（业务对象）与 Service（服务层）的混淆

  最佳的设计方式：BO含有通用的业务逻辑，而Service层可以通过控制多个BO业务对象交互，从而实现最终业务逻辑。

  但由于网络化的问题，当下框架大多倾向于网络层无BO对象，BO对象仅在客户端本地、服务端本地设计，取而代之的是采用只包含单一BO对象的Service来替代BO对象的通用业务逻辑，BO对象被迫成为纯粹的DTO（数据传输对象）。

  纵然所有的BO对象都可以被抽离成为DTO+Service，但抽离最基础的业务逻辑，必然会造成逻辑上的繁琐，与Service层控制多个BO业务交互的理念也大相径庭。

  我们可以对上述代码进行简单地诠释：

上述代码的Service层如果再加一个方法：获取用户ID

```c#
public class UserService{
    public User login(User user){
        if(user.username == "username" && user.password == "password"){
            user.id = 1112;
            return user;
        }
        else return null;
    }
	public long getId(User user){
        if(user != null){
        	return user.id;
        }
    }
}
//客户端
UserService userService = new UserService();
User user = new User("username","password");
user = userService.login(username,password);
if(user != null){
    Console.print("登录成功:" + playerService.getId(User));
}
else Console.print("登录失败");
```

  我们会发现，一个极为简单的User通用方法[User能够自实现]，都放在Service，会在阅读上充满不适。

  我们来看一下单机的写法

```java
//BO实体
public class User{
    String username;
    String password;
    long id;
	public boolean login(){
        if(username == "username" && password == "password"){
            id = 1112;
            return true;
        }
        else false;
    }
    public long getId(){
        return id;
    }
}
//客户端
User user = new User("username","password");
if(user.login()) {
	Console.print("登录成功:" + user.getId());
}
else Console.print("登录失败");
```

  可以发现，我们将Service业务方法置入User[DTO]中，转化为了新的User[BO],  代码逻辑上不断在提高可读性，同时代码的行数却在不断地降低。

  单机编程在安全、维护、数据交互上都存在着极为的不便，网络化愈发流行，但网络化却又一直无法摆脱逻辑复杂问题。

### QinsNet

  正题来了，前文说了许多单机版的开发优势以及安全等问题的劣势，作为单机版，安全、网络数据交互等问题是不可解决的，但是我们虽然无法解决单机版的劣势，但是我们可以解决网络版的劣势。

  网络版的劣势在于传递数据、数据同步这个步骤是人为操控，不可避免的生出了网络化逻辑。

  QinsNet正是解决了网络化导致的业务逻辑增生分化问题，网络单机化。

  更好的定义是：**状态同步式网络框架**，

  更好的理解是：**共享实体**

  总结就是，单机如何编程，网络化时

#### 责任说明

1. Ethereal并非所有语言都会实现一套C\S，我们理性的认为，用C++搭建服务器是一个糟糕的决定，所以我们长期不会对C++的服务器版本进行支持，且短期并无意于C++客户端版本。我们深知C++客户端的迫切，所以我们采用WebSocket协议，同时也支持了HTTP协议，这两种协议无论在何种流行语言，都有完整的框架支持，所以依旧可以与Ethereal进行交互，确保了无Ethereal版本支持下的最低交互保证！
2. Ethereal热衷于支持流行语言，无论是C\#、Java还是Python都有了可靠的支持，但也并非局限于这几种语言，我们仍在招募着志同道合的道友，同我们一起维护与拓展。
3. Ethereal采用LGPL开源协议，我们希望Ethereal在社区帮助下持续健康的成长，更好的为社区做贡献。
4. Ethereal长期支持，我们欢迎开发者对Ethereal进行尝鲜。

### 入门

接下来我们以C\#和Java版本来快速了解三步曲：类型、网关、服务\请求。

#### server\[C\#\]

```text
public class ServerService
{
    [Service]
    public int Add(int a,int b)
    {
        return a + b;
    }
}

//注册数据类型
AbstractTypes types = new AbstractTypes();
types.Add<int>("Int");
types.Add<long>("Long");
types.Add<string>("String");
types.Add<bool>("Bool");
types.Add<User>("User");
Net node = NetCore.Register("name", Net.NetType.WebSocket); //注册网关
server server = ServerCore.Register(node,"127.0.0.1:28015/NetDemo/");//注册服务端
Service serviceNet = ServiceCore.Register<ServerService>(node, "server", types);//注册服务
node.Publish();//启动
```

#### Client\[Java\]

```java
public interface ServerService
{
    @Request
    public Integer Add(Integer a,Integer b);
}
//注册数据类型
AbstractTypes types = new AbstractTypes();
types.add(Integer.class,"Int");
types.add(Long,"Long");
types.add(String,"String");
types.add(Boolean,"Bool");
types.add(User.class,"User");
Net node = NetCore.register("name", Net.NetType.WebSocket); //注册网关
Client client = ClientCore.Register(node,"127.0.0.1:28015/NetDemo/");//注册客户端
Request requestMeta = RequestCore.register(ServerRequest.class,node, "server", types);//注册请求
node.publish();//启动
```

### 架构

#### Core

> 维护特有类，作为全局静态类，唯一对外公开接口，确保了对应实体注册/销毁/访问时安全性。

Core一般含有Register、UnRegister、Get三大公开方法，Ethereal拥有四个Core，分别为：

* **NetCore**：Net网络节点的管理
* **ClientCore/ServerCore**：Client客户端或Server服务端的管理
* **ServiceCore**：Service请求体的管理
* **RequestCore**：Request请求体的管理

  Core并非实质保存着对该实体的实例，实际上，Request、Service、Client/Server都归于Net，Net作为一个网络节点，与其他网络节点交互（管理中心、注册中心）。

  Core的目标是屏蔽注册细节，也是为了保证访问安全，Core是用户交互操作的唯一入口。

#### Config

Config含有各式各样的配置项，以此满足用户的个性化配置。

* **NetConfig**：Net网络节点配置项
* **ClientConfig/ServerConfig**：Client/Server配置项
* **ServiceConfig**：Service配置项
* **RequestConfig**：Request配置项

  同时Config可作为蓝本，在多个实体间共享。

#### Object

Core根据Config配置产生具体的Object（实体），实体完成具体的工作。

* **Net**：对内作为管理中心，管理实体，对外负责作为注册中心向外暴露服务。
* **Client/server**：通讯框架，Java使用Netty框架，Python使用Twisted。
* **Service**：服务实现类，负责请求的具体实现。
* **Request**：服务请求类，负责向远程具体的服务实现发起请求。

## 技术文档

### 中心配置

Ethereal中心配置涵盖了注册中心、管理中心的功能。

中心配置十分轻效，在Net的Config中开启集群配置，并提供集群地址和对应集群配置项即可。

**server\[C\#\]**

```text
Net node = NetCore.Register("name", Net.NetType.WebSocket); //注册网关
//开启集群模式
node.Config.NetNodeMode = true;
List<Tuple<string, ClientConfig>> ips = new();
//添加集群地址
ips.Add(new Tuple<string,ClientConfig>($"{ip}:{28015}/NetDemo/", new ClientConfig()));
ips.Add(new Tuple<string,ClientConfig>($"{ip}:{28016}/NetDemo/", new ClientConfig()));
ips.Add(new Tuple<string,ClientConfig>($"{ip}:{28017}/NetDemo/", new ClientConfig()));
ips.Add(new Tuple<string,ClientConfig>($"{ip}:{28018}/NetDemo/", new ClientConfig()));
node.Config.NetNodeIps = ips;
```

**Client\[Java\]**

```java
Net node = NetCore.register("name", Net.NetType.WebSocket); //注册网关
//开启集群模式
node.getConfig().setNetNodeMode(true);
ArrayList<Pair<String, ClientConfig>> ips = new ArrayList<>();
//添加集群地址
ips.add("127.0.0.1:28015/NetDemo/",new ClientConfig());
ips.add("127.0.0.1:28016/NetDemo/",new ClientConfig());
ips.add("127.0.0.1:28017/NetDemo/",new ClientConfig());
ips.add("127.0.0.1:28018/NetDemo/",new ClientConfig());
node.getConfig().setNetNodeIps(ips);
```

> Ethereal的中心服务部署在Net，与正常Service属于同一层级，这也意味着不需要额外的端口，一个Net节点，就是一个中心，不需要关心集群部署时的端口配置问题，您在**部署服务的同时，也是在部署集群！**

### Token理念

还是简单分析一下场景需求：

一个用户发起登录请求，服务器执行登录逻辑之后，有时会需要将用户信息暂存内存，需要时再查询该用户信息。

比如用户登录，服务器生成一个User类的实体，内部包涵了该用户的临时信息，比如登录时间、用户等级、用户凭证.....当用户断开连接时，再将这个实体销毁。

并且服务时，往往是多个客户端对标一个服务端，如果遇到聊天系统的功能需求，客户端之间也往往会通过服务端进行通讯，这样服务端也需要区别不同的客户端。

基于上述需求，Ethereal开放了Token类别，Token相当于一个客户端连接体，用户控制BaseToken即控制客户端连接体。

BaseToken内含有唯一Key值属性，Ethereal通过用户给予的Key值属性，对Token进行生命周期处理。

```text
[Service]
public bool login(BaseToken node, string username,string password)
{
    node.Key = username;//为该token设置键值属性
    BaseToken.Register();//将token注册，受Ethereal管理其生命周期
}
```

通过上面的函数，我们似乎发现了一个特殊之处，token放在了服务类的**首参**，其实刚刚的加法函数也可以改写为：

```text
public class ServerService
{
    [Service]
    public int Add(BaseToken node,int a,int b)
    {
        return a + b;
    }
}
```

Ethereal会根据用户的首参情况，来决定是否为首参注入token实体。

在Request中，不必提供token参数的定义，对于Request，仍保持基本接口规范即可。

`public Integer Add(Integer a,Integer b);`

```text
[Service]
public bool login(User user, string username,string password)
{
    user.Key = username;//为该token设置键值属性
    user.Register();//将token注册，受Ethereal管理其生命周期
}
```

BaseToken是可继承的，那就代表了用户可以通过自定义一个User类，并继承BaseToken，这进一步的转换了设计理念，从面向连接体，变为了面向用户。

### 双工通讯

Ethereal致力于服务尽可能多的需求业务，虽然现今单工请求占据了大量业务需求，但绝不是所有，作者本人也是经常需要用到双工通讯的，尤其是在游戏业务这一块。

所以就拿游戏业务这一块进行阐明，假设一个游戏角色拥有移动、攻击、聊天等行为，服务端可以通过执行一套请求逻辑，从而达到控制目标角色的需求，可以极大简化服务端的编程逻辑。

```text
public interface ServerService
{
    //Player继承BaseToken
    @Request
    public void Move(Player user);
    @Request
    public void Attack(Player user);
    @Request
    public void Chat(Player user);
}
```

这里的请求，是服务端发起，客户端接收，请求的注册方式与客户端的注册方式相一致，也是通过RequestCore进行注册。

> 与客户端的请求不同点在于：
>
> 1. 服务端的首参必为BaseToken，前文说道BaseToken实际就是连接体，所以传递BaseToken实际代表了将请求发送到目标客户端。
> 2. 服务端的请求函数必定没有返回值，我们不认为服务端等待客户端的结果返回是一个明智的选择。\[当然，这是基于目前的需求来看，需求也在不断地变动，后续也许Ethereal会开放返回值\]

我们这里有一套完整向某用户发送消息Demo：

```text
public class ServerService
{
    /// <summary>
    /// 向服务端注册用户
    /// </summary>
    /// <param name="user">客户端用户</param>
    /// <param name="username">用户名</param>
    /// <param name="id">用户ID</param>
    [Service]
    public bool Register(User user, string username, long id)
    {
        user.Username = username;
        user.Key = id;
        return user.Register();//BaseToken方法，向Ethereal注册Token。
    } 
    /// <summary>
    /// 接受客户端发送来的发送消息给某个用户的命令请求
    /// </summary>
    /// <param name="sender">客户端用户</param>
    /// <param name="recevier_key">目标接收用户的唯一Key值</param>
    /// <param name="message">消息内容</param>
    [Service]
    public bool SendSay(User user, long recevier_key, string message)
    {
        //从Ethereal的Net节点中查找目的用户（经过Register注册的）
        User reciver = user.GetToken(recevier_key);
        if (reciver != null)
        {
            //向listener用户发送Hello请求
            requestMeta.Say(reciver,user.Name + "说:" + message);
            return true;
        }
        else return false;
    }
}
```

### **日志系统**

Ethereal的日志系统（TrackLog）力图最大化的信息输出，TrackLog实体中，包含了从该点向上一层不断抛出时的抛出实体信息。

TrackLog中含有Net、Request\Service、Client\Server实体，输出日志时，Log会根据事件发生点进行注入抛出，比如一个Service日志，将包含Service、Client、Net三个实体，同时应注意，事件输出之后，应保证这些核心实体不应该被外部保存，避免造成内存泄漏。

每一个核心实体，都包含了日志事件，您可以通过注册事件，实现日志输出事件的捕获，并且可以根据选择，捕获不同层级的事件。

通常捕获Net事件，代表了该Net节点的所有日志输出。

```text
node.ExceptionEvent += ExceptionEventFunction;
private static void ExceptionEventFunction(TrackException exception)
{
    Console.print(exception.Message);
}
```

### **异常系统**

Ethereal的日志系统（TrackException）力图最大化的信息输出，TrackException实体中，包含了从该点向上一层不断抛出时的抛出实体信息。

TrackException中含有Net、Request\Service、Client\Server实体，抛出异常时，TrackException会根据事件发生点进行注入抛出，比如一个Service异常，将包含Service、Client、Net三个实体，同时应注意，事件输出之后，应保证这些核心实体不应该被外部保存，避免造成内存泄漏。

每一个核心实体，都包含了异常事件，您可以通过注册事件，实现日志输出事件的捕获，并且可以根据选择，捕获不同层级的事件。

通常捕获Net事件，代表了该Net节点的所有异常输出。

**与Log不同的是，TrackException内部包含了一个Exception字段，该字段是真正的异常事件，有时为TrackException本身，但也有时是一些其他异常，Ethereal捕获所有异常并封装在其内部。**

> ```text
> node.ExceptionEvent += ExceptionEventFunction;
> private static void ExceptionEventFunction(TrackException exception)
> {
>     Console.print(exception.Message);
> }
> ```

### **服务拦截**

Ethereal的服务拦截分为Net层拦截，以及Service层拦截，且两层拦截均含有Net、Service、Method、Token信息，用户可以充分的获取有用信息来进行判断。

在拦截委托中，如果您返回`True`将进行下一个拦截事件检测，而返回`False`，则消息立即拦截，后续的拦截策略不会执行。

```text
serviceNet.InterceptorEvent += Interceptor;
private static bool Interceptor(Net node, Service serviceNet, MethodInfo method, Token node)
{
    if (node.Key == "123")
    {
        return false;
    }
}
```

**同时，基于拦截器，Ethereal开发了权限拦截的功能拓展。**

```text
[Service(authority = 3)]
public bool SendSay(User user, long recevier_key, string message)
{
    //从Ethereal的Net节点中查找目的用户（经过Register注册的）
    User reciver = user.GetToken(recevier_key);
    if (reciver != null)
    {
        //向listener用户发送Hello请求
        requestMeta.Say(reciver,user.Name + "说:" + message);
        return true;
    }
    else return false;
}
public class User:BaseToken,IAuthorityCheck
{
    public bool Check(IAuthoritable authoritable){
        if(this.Authority >= authoritable.Authority)return true;
        else return false;
    }
}
```

1. `BaseToken`类实现`IAuthorityCheck`接口，实现权限检查函数
2. 在方法注解中设置添加authority参数：`[Service(authority = 3)]`，这里3就是提供的权限信息
3. 在拦截器中添加Ethereal权限检查函数

   `serviceNet.InterceptorEvent += Extension.Authority.AuthorityCheck.ServiceCheck;`

   等待收到请求到达该方法，Ethereal会主动调用`BaseToken`类实现`IAuthorityCheck`接口中的Check函数，具体权限判断逻辑，用户可以根据自己的情况自行设计，最简单的就是大于该等级，即可通过。

## 关于我们

### 团队成员

## 白阳

_“黄昏，垂夜，星海，白珑。”_

### 项目分配

| 板块 | 语言\|框架 | 开发人员 |
| :---: | :---: | :---: |
| server | C\# | 白阳 |
| Client | C\# | 白阳 |
| server | Java | 007 |
| Client | Java | anmmMa |
| server | Python | Ckay |
| Client | Python | 青山 |
| Center | Vue | Laity |
| Document | Jekyll | 白阳 |

