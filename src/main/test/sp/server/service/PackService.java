package sp.server.service;

import sp.server.entity.Package;

public class PackService {
    public void pack(Package aPackage){
        System.out.println(aPackage.getName());;//输出背包Name
    }
}
