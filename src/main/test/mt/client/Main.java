package mt.client;

public class Main {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        User user = User.class.newInstance();//特殊方法获取
        user.login();
        user.getPack();
        user.getAPackage().pack();
    }
}
