package main.userstat;


import com.google.gson.Gson;
import main.tools.HttpRequestTools;
import main.pojo.UserState;
import main.pojo.UserStateInfo;

import java.io.IOException;

import static main.tools.HttpRequestTools.delete;
import static main.tools.HttpRequestTools.post;


public class UserStateService {


    public boolean isOnline(Integer userid) {
        String resp = null;
        try {
            resp = HttpRequestTools.get("http://localhost/userstate?userid=" + userid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp != null && resp.length() != 0;
    }

    public void online(Integer userid, UserState userState) {
        String resp = null;
        try {
            UserStateInfo userStateInfo = new UserStateInfo();
            userStateInfo.setUserState(userState);
            userStateInfo.setUserid(userid);
            Gson gson = new Gson();
            resp = post("http://localhost/userstate", gson.toJson(userStateInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(resp);
    }

    public void offline(Integer userid) {
        String resp = null;
        try {
            Gson gson = new Gson();
            resp = delete("http://localhost/userstate", gson.toJson(userid));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(resp);
    }


}
