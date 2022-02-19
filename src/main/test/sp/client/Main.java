package sp.client;


import sp.client.entity.Package;
import sp.client.entity.User;
import sp.client.service.PackService;
import sp.client.service.UserService;

class Main {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
        User user = new User();
        UserService userService = UserService.class.newInstance();//通过某种途径获取
        int apiToken = userService.login(user);
        user.setApiToken(apiToken);
        Package aPackage = userService.getPack(apiToken);
        user.setAPackage(aPackage);
        PackService packService = PackService.class.newInstance();//通过某种途径获取
        packService.pack(user.getAPackage());
    }
}

