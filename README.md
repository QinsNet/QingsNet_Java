---
description: 带您快速了解QinsNet
---

# 快速入门

## 引论

### QinsNet

> **面向BO网络编程框架**

当下通过以数据为主的网络编程为面向DTO网络编程，人工处理DTO与BO（业务对象）的同步关系。单个网络请求中，主张以发送数据、获取数据、处理数据，通过对数据的操控来实现业务需求。

数据的构造发送与接收同步并不是业务本身希望关注的事情；并且在网络化的过程中，丢掉了业务方法特征，也丢掉了简明的业务语义。

面向BO网络编程是通过多个节点构造出网络业务对象NBO（Network Business Object），本质是合并DTO、Service、Entity，最终构造出NBO对象，对于使用者来说，入手即业务，而非业务拆解。

NBO经过网络传输后，依旧保持业务方法特征。

这一特征体现在：

①　NBO能够同步自身实体状态。

②　NBO能够同步参数实体状态。

③　NBO经历网络传输后仍保持稳定的引用关系。

④　NBO具备传输稳定性，NBO实体通过网络传输后，仍具备发起网络请求的能力，不会退化为BO。

## 入门

### 一纸契约

```java
@Meta(nodes = "Shanghai")//默认节点
public abstract class User {
    @Field
    private String username;
    @Field
    private String password;
    @Field
    private Integer apiToken;
    @Field
    private ArrayList<Package> packages;
    @Field
    Package aPackage;
    
    @Post
    @Sync("{username,password}")//同步的参数
    @Async("{apiToken}")//不同步的参数
    public abstract boolean login();
    
    @Post//默认Shanghai节点
    public abstract boolean newPack();
    
    @Post(nodes = {"Beijing", "Shanghai"})
    @Sync("{packages}")
    public abstract boolean addPack(@Async("{name}")Package aPackage);

    @Post(nodes = "User")
    public abstract void hello();
}
```

客户端和服务端均可以通过此契约来配置网络服务，其中@Meta表明该资源为网络资源，具备网络共享（同步）的能力。

契约支持**多节点**同时网络资源的发布与服务是共享的，倘若符合锥形NAT，用户本地方也是服务可达状态，即用户方拥有自己的节点以及自己的网络函数，服务方可以主动向用户方发起网络请求，与P2P概念一致。

### Client

```java
@Meta(nodes = "Shanghai")//默认节点
public abstract class User {
    @Field
    private String username;
    @Field
    private String password;
    @Field
    private Integer apiToken;
    @Field
    private ArrayList<Package> packages;
    @Field
    Package aPackage;
    @Post
    @Sync("{username,password}")
    public abstract boolean login();

    @Post//默认Shanghai节点
    public abstract boolean newPack();

    @Post(nodes = {"Beijing", "Shanghai"})
    @Sync("{packages}")
    public abstract boolean addPack(@Async("{name}")Package aPackage);

    @Post(nodes = "User")
    public abstract void hello()//不同节点只需负责好自己节点应该实现的方法就可以了
    {
     	  System.out.println("你好：" + username);
    }
}
```

### Server

```java
@Meta(value = "User",nodes = "Shanghai")
public abstract class User{
    @Field
    private String username;
    @Field
    private String password;
    @Field
    private Integer apiToken;
    @Field
    private ArrayList<Package> packages;
    @Field
    Package aPackage;

    @Post
    @Sync("{username,password}")
    public boolean login() throws NewInstanceException {
        if("m839336369".equals(username) && "password".equals(password)){
            this.apiToken = 1234;
            this.password = "***";
            return true;
        }
        else return false;
    }
    
    @Post(nodes = {"Server_2","Server1"})
    public boolean newPack(){
        try {
            Package aPackage = MetaApplication.create(Package.class);
            NodeUtil.copyNodeAll(this,aPackage);
            aPackage.setName("A背包");
            Package bPackage = MetaApplication.create(ServicePackage.class);
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
    
    @Post
    @Sync("{packages}")
    public Boolean addPack(@Async("{name}")Package aPackage){
        this.aPackage = aPackage;
        aPackage.setName("改名字不会传递回请求方");
        packages.add(aPackage);
        return true;
    }

    @Post(nodes = "User")
    public abstract void hello();
}
```

### 部署

​       *请求方*

```java
public class Client {
    public static void main(String[] args) throws NewInstanceException {
        MetaApplication.run("client.yaml");
        MetaApplication.getContext()
            .getMetaClassLoader()
            .getScanner().getPaths().add("mt.client");//添加包扫描路径
        MetaApplication.defineNode("User", "localhost:28017");
        MetaApplication.defineNode("Shanghai", "localhost:28003");
        MetaApplication.defineNode("Beijing", "localhost:28003");
        User user = MetaApplication.create(User.class);
        user.setUsername("m839336369");
        user.setPassword("password");
        if(user.login()){
            user.newPack();
            Package aPackage = MetaApplication.create(Package.class);
            aPackage.name = "C背包";
            user.addPack(aPackage);
            user.hello();
        }
    }
}
```

​       *服务方*

```java
public class Server {
    public static void main(String[] args) throws LoadClassException {
        MetaApplication.run("server.yaml");
        MetaApplication.defineNode("Shanghai", "localhost:28003");
        MetaApplication.defineNode("Beijing", "localhost:28003");
        MetaApplication.publish(User.class);//监听User实体的请求
    }
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
		"aPackage": "Package@335b5620"
	},
	"references": {
		"ServicePackage@73194df": {
			"instance": {
				"name": "java.lang.String@B背包"
			},
			"nodes": {
				"Beijing": "localhost:28003",
				"User": "localhost:28017",
				"Shanghai": "localhost:28003"
			}
		},
		"Package@335b5620": {
			"instance": {
				"name": "java.lang.String@C背包"
			},
			"nodes": {
				"Beijing": "localhost:28003",
				"User": "localhost:28017",
				"Shanghai": "localhost:28003"
			}
		},
		"java.util.ArrayList@29a0cdb": ["Package@32a68f4f", "ServicePackage@73194df"],
		"Package@32a68f4f": {
			"instance": {
				"name": "java.lang.String@A背包"
			},
			"nodes": {
				"Beijing": "localhost:28003",
				"User": "localhost:28017",
				"Shanghai": "localhost:28003"
			}
		},
		"User@78a773fd": {
			"instance": {
				"packages": "java.util.ArrayList@29a0cdb"
			},
			"nodes": {
				"Beijing": "localhost:28003",
				"User": "localhost:28017",
				"Shanghai": "localhost:28003"
			}
		}
	},
	"instance": "User@78a773fd"
}
```

- ResponseMeta(返回元)

```json
{
	"protocol": "Sync-Response-1.0",
	"result": "java.lang.Boolean@true",
	"references": {
		"ServicePackage@73194df": {
			"instance": {}
		},
		"Package@335b5620": {
			"instance": {}
		},
		"java.util.ArrayList@29a0cdb": ["Package@32a68f4f", "ServicePackage@73194df", "Package@335b5620"],
		"Package@32a68f4f": {
			"instance": {}
		},
		"User@78a773fd": {
			"instance": {}
		}
	}
}
```

- 采用了引用池的概念，避免了深拷贝问题。
- 网络传输分有两种类型实例，一种是节点实例，一种是普通实例，节点实例会附带Nodes节点信息
- 值得一提的是，在方法传参后，节点实例依旧保持着节点特性。

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

### 同步规则

@Sync与@Async表示制定同步规则，允许作用在方法体与参数。

@Sync表示同步该参数，@Async表示不同步该参数。

- 标准

```java
实体名{实体属性名}
```

- 嵌套

```java
A{B{C}}
```

- 允许简化


```java
A.B.C
```

更高级的自定义同步规则注解请参看官方文档。

## 关于我们

### 白阳

_“黄昏，垂夜，星海，白珑。”_

## 责任说明

1. QinsNet采用LGPL开源协议，我们希望QinsNet在社区帮助下持续健康的成长，更好的为社区做贡献。
2. QinsNet长期支持，我们欢迎开发者对QinsNet进行尝鲜。
