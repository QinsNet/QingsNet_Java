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
public Package setPackage(User user,Package aPackage){
	user.setPackage(aPackage);
	return aPackage;
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

