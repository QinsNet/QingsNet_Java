---
description: 带您快速了解QinsNet
---

# 快速入门

## 引论

### QinsNet

> **状态同步式网络服务框架**



  单机版的开发优势以及安全等问题的劣势，作为单机版，安全、网络数据交互等问题是不可解决的，但是我们虽然无法解决单机版的劣势，但是我们可以解决网络版的劣势。

  网络版的劣势在于传递数据、数据同步这个步骤是人为操控，不可避免的生出了网络化逻辑。

  QinsNet正是解决了网络化导致的业务逻辑增生分化问题，网络单机化。

  更好的定义是：**状态同步式网络框架**，

  更好的理解是：**共享实体**

### 网络化

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

#### 问题三[内存]：引用与拷贝

```java
//服务端逻辑
public void setPackage(User user,Package aPackage){
	user.setPackage(aPackage);
}
```

```java
//客户端逻辑
User user = new User();
UserService userService = new UserService();
Package aPackage = new Package();
Package bPackage = userService.setPackage(user,aPackage);
user.getPackage();//报错，因为服务端的实体状态是无法同步到客户端的。
//aPackage 与 bPackage 不为同一个实体，一般是基于序列化的深拷贝对象。
```

当服务完成后，User内部的aPackage应与返回值aPackage为同一对象，且User的状态改变。

  但作为网络服务一方，显然无法具备让客户端User状态改变、引用同步这样的逻辑，这也是为什么造成了单机版编程与网络版编程差异的主要原因。

## 入门

### 一纸契约

```java
@NodeMapping("Beijing","localhost:28015")//节点一
@NodeMapping("Shanghai","localhost:28016")//节点二
@Meta(nodes = "Shanghai")//默认上海节点
public abstract class User{
    @Meta//共享资源
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta
    private ArrayList<Package> packages;
    @Meta//默认选择上海节点
    public abstract boolean login();
    @Meta
    public abstract boolean newPack();
    @Meta(nodes = {"Beijing","Shanghai"})//允许通过北京、上海两个节点进行网络请求
    public abstract boolean addPack(@Meta Package aPackage);
    @Meta(nodes = "User")//用户自身节点
    public abstract void hello();
}
```

  客户端和服务端均可以通过此契约来配置网络服务，其中@Meta表明该资源为网络资源，具备网络共享（同步）的能力。

  契约支持**多节点**同时网络资源的发布与服务是共享的，倘若符合锥形NAT，用户本地方也是服务可达状态，即用户方拥有自己的节点以及自己的网络函数，服务方可以主动向用户方发起网络请求，与P2P概念一致。

### Client

```java
@NodeMapping("Beijing","localhost:28015")//节点一
@NodeMapping("Shanghai","localhost:28016")//节点二
@Meta(nodes = "Shanghai")
public abstract class User{
    @Meta
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta
    private ArrayList<Package> packages;
    @Meta/
    public abstract boolean login();
    @Meta
    public abstract boolean newPack();
    @Meta(nodes = {"Beijing","Shanghai"})
    public abstract boolean addPack(@Meta Package aPackage);
    @Meta(nodes = "Beijing")
    public void hello(){//不同节点只需负责好自己节点应该实现的方法就可以了
       System.out.println(name + " Hello!!!");
    }
}
```

### Server

```java
@NodeMapping("Beijing","localhost:28015")//节点一
@NodeMapping("Shanghai","localhost:28016")//节点二
@Meta(nodes = "Shanghai")
public abstract class User{
    @Meta
    private String username;
    @Meta
    private String password;
    @Meta
    private Integer apiToken;
    @Meta
    private ArrayList<Package> packages;

    @Meta
    public boolean login() throws NewInstanceException {
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            this.password = "***";
            return true;
        }
        else return false;
    }
    
    @Meta(nodes = {"Server_2","Server1"})
    public boolean newPack(){
        try {
            Package aPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,aPackage);
            aPackage.setName("A背包");
            Package bPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,bPackage);
            bPackage.setName("B背包");
            packages = new ArrayList<>();
            packages.add(aPackage);
            packages.add(bPackage);
            return true;
        } catch (NewInstanceException | TrackException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    @Meta
    public Boolean addPack(@Meta Package aPackage){
        packages.add(aPackage);
        return true;
    }

    @Meta(nodes = "User")
    public abstract void hello();
}
```

### 部署

​       *网络化的请求逻辑与单机逻辑保持一致。*

```java
MetaApplication.run("client.yaml");//加载配置文件
MetaApplication.defineNode("User", "localhost:28017");//定义全局节点
User user = MetaApplication.create(User.class);
if(user.login()){
	System.out.println(name + " Hello!!!");
}
else{
	System.out.println(name + " 登录失败.");
}
```

## 技术文档

### 数据传输

- RequestMeta(请求元)


```json
{
	"protocol": "Sync-Request-1.0",
	"mapping": "User/addPack",
	"params": {
		"aPackage": {
			"instance": "mt.client.Package$$EnhancerByCGLIB$$7713a45b@35cabb2a",
			"nodes": {
				"User": "localhost:28017",
				"Server_1": "localhost:28003",
				"Server_2": "localhost:28003"
			}
		}
	},
	"references": {
		"1234": 1234,
		"mt.client.Package$$EnhancerByCGLIB$$7713a45b@69a2f3cc": {
			"name": "A背包"
		},
		"mt.client.User$$EnhancerByCGLIB$$713f5d40@534df152": {
			"password": "***",
			"apiToken": "1234",
			"packages": "[mt.client.Package$$EnhancerByCGLIB$$7713a45b@69a2f3cc, mt.client.Package$$EnhancerByCGLIB$$7713a45b@67651088]",
			"username": "m839336369"
		},
		"mt.client.Package$$EnhancerByCGLIB$$7713a45b@35cabb2a": {
			"name": "C背包"
		},
		"***": "***",
		"A背包": "A背包",
		"m839336369": "m839336369",
		"B背包": "B背包",
		"[mt.client.Package$$EnhancerByCGLIB$$7713a45b@69a2f3cc, mt.client.Package$$EnhancerByCGLIB$$7713a45b@67651088]": [{
			"instance": "mt.client.Package$$EnhancerByCGLIB$$7713a45b@69a2f3cc",
			"nodes": {
				"User": "localhost:28017",
				"Server_1": "localhost:28003",
				"Server_2": "localhost:28003"
			}
		}, {
			"instance": "mt.client.Package$$EnhancerByCGLIB$$7713a45b@67651088",
			"nodes": {
				"User": "localhost:28017",
				"Server_1": "localhost:28003",
				"Server_2": "localhost:28003"
			}
		}],
		"C背包": "C背包",
		"mt.client.Package$$EnhancerByCGLIB$$7713a45b@67651088": {
			"name": "B背包"
		}
	},
	"instance": {
		"instance": "mt.client.User$$EnhancerByCGLIB$$713f5d40@534df152",
		"nodes": {
			"User": "localhost:28017",
			"Server_1": "localhost:28003",
			"Server_2": "localhost:28003"
		}
	}
}
```

- ResponseMeta(返回元)

```json
{
	"protocol": "Sync-Response-1.0",
	"result": "true",
	"instance": {
		"instance": "mt.client.User$$EnhancerByCGLIB$$db49c047@8e0379d",
		"nodes": {
			"User": "localhost:28017",
			"Server_1": "localhost:28003",
			"Server_2": "localhost:28003"
		}
	},
	"params": {},
	"references": {
		"mt.client.User$$EnhancerByCGLIB$$db49c047@8e0379d": {
			"password": "***",
			"apiToken": "1234",
			"packages": "[mt.server.Package$$EnhancerByCGLIB$$20a58593@4018c77f, mt.server.Package$$EnhancerByCGLIB$$20a58593@44275ba4]",
			"username": "m839336369"
		},
		"1234": 1234,
		"mt.server.Package$$EnhancerByCGLIB$$20a58593@44275ba4": {
			"name": "B背包"
		},
		"true": true,
		"[mt.server.Package$$EnhancerByCGLIB$$20a58593@4018c77f, mt.server.Package$$EnhancerByCGLIB$$20a58593@44275ba4]": [{
			"instance": "mt.server.Package$$EnhancerByCGLIB$$20a58593@4018c77f",
			"nodes": {
				"User": "localhost:28017",
				"Server_1": "localhost:28003",
				"Server_2": "localhost:28003"
			}
		}, {
			"instance": "mt.server.Package$$EnhancerByCGLIB$$20a58593@44275ba4",
			"nodes": {
				"User": "localhost:28017",
				"Server_1": "localhost:28003",
				"Server_2": "localhost:28003"
			}
		}],
		"***": "***",
		"A背包": "A背包",
		"m839336369": "m839336369",
		"mt.server.Package$$EnhancerByCGLIB$$20a58593@4018c77f": {
			"name": "A背包"
		},
		"B背包": "B背包"
	}
}
```

- 采用了引用池的概念，避免了深拷贝问题。
- 网络传输分有两种类型实例，一种是节点实例，一种是普通实例，节点实例会附带Nodes节点信息
- 值得一提的是，在方法传参后，节点实例依旧保持着节点特性。

### 网络引用

  无论以什么方式（属性、参数、返回值），所有的数据都保持着一致的引用原则，客户端的引用状态与服务端的引用状态一致，回传同步之后，依旧保持一致。

  与本地引用一致，当所有共享网络资源都无法引用到该类时，该类将不会共享同步，如下述代码

```java
    @Meta(nodes = {"Server_2","Server1"})
    public void removePackage(List<Package> packages){
        Package aPackages = packages.get(0);
        packages.remove(0);
        aPackages.setName("已删除");//值设置无效，因为该引用未引用到新的共享资源实例中，不会回传同步。
    }
```

  当然，这虽然符合引用的概念，但不排除需要同步的情况，故我们做了额外的拓展，默认开启回传同步检查，以上即使丢失网络引用，一样具备共享性质。

###  网络节点

-   全局节点

  所有类节点都会包含全局节点信息，若类节点已存在，则不进行更替。

```java
MetaApplication.defineNode("User", "localhost:28017");
MetaApplication.defineNode("Server_1", "localhost:28003");
MetaApplication.defineNode("Server_2", "localhost:28003");
```

- 类节点

  类节点保存内部方法请求的所有节点信息（包含节点地址）

```java
@NodeMapping(name="Beijing",host="localhost:28015")//节点一
@NodeMapping(name="Shanghai",host="localhost:28016")//节点二
@Meta(nodes = "Shanghai")//默认上海节点
public abstract class User
```

- 请求节点

  请求节点具备多节点支持，节点不包含地址信息，只包含节点名映射

  若无节点定义，则采用类节点中的*@Meta(nodes = "Shanghai")*作为默认节点

```java
	@Meta(nodes = {"Server_2", "Server1"})
    public abstract boolean addPack(@Meta Package aPackage);

    @Meta(nodes = "Server_2")
    public abstract void hello();

    @Meta
    public abstract boolean removePackage(Package aPackage);
```

- 动态配置节点

  MetaApplication 允许动态设置节点信息

```java
//配置全局节点
public static MetaApplication defineNode(String name, String address);
//配置实体节点
public static MetaApplication defineNode(Object instance, String name, String address);
```

### Meta注解

- 类注解：网络元

  value:映射名

  nodes:默认节点

```java
@Meta(value = "User",nodes = "Shanghai")//默认上海节点
public abstract class User
```

- 属性注解：共享资源

  value:映射名

```java
@Meta(value = "User")
private String password;
```

- 方法注解：网络请求

  value:映射名

  nodes:节点映射，空表示默认节点

```java
@Meta(value = "Login",nodes = "Shanghai")
public abstract boolean login();
```

- 参数注解：共享资源

  value:映射名

```java
public abstract boolean addPack(@Meta Package aPackage);
```

## 关于我们

### 白阳

_“黄昏，垂夜，星海，白珑。”_

## 责任说明

1. QinsNet采用LGPL开源协议，我们希望QinsNet在社区帮助下持续健康的成长，更好的为社区做贡献。
2. QinsNet长期支持，我们欢迎开发者对QinsNet进行尝鲜。
