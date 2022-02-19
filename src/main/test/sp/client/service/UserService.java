package sp.client.service;

import sp.client.entity.Package;
import sp.client.entity.User;

public interface UserService {
    public int login(User user);
    public Package getPack(int apiToken);
}
