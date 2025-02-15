package mt.client;

import com.qins.net.core.boot.MetaApplication;
import com.qins.net.core.exception.ObjectLangException;
import com.qins.net.core.exception.TrackException;
import com.qins.net.core.exception.NewInstanceException;
import com.qins.net.core.lang.serialize.ObjectLang;
import com.qins.net.service.core.Service;
import com.qins.net.util.SerializeUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {
    public static void main(String[] args) throws NewInstanceException {
        MetaApplication.run("client.yaml");

        MetaApplication.getContext().getMetaClassLoader().getScanner().getPaths().add("mt.client");
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
