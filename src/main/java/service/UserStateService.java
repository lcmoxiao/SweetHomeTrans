package service;


import com.google.gson.Gson;
import pojo.UserState;
import pojo.UserStateInfo;

import java.io.IOException;

import static tools.OkHttpTools.*;


public class UserStateService {


    public boolean isOnline(Integer userid) {
        String resp = null;
        resp = get("userstate?userid=" + userid);

        return resp.length() != 0;
    }

    public void online(Integer userid, UserState userState) {
        String resp = null;
        try {
            UserStateInfo userStateInfo = new UserStateInfo();
            userStateInfo.setUserState(userState);
            userStateInfo.setUserid(userid);
            Gson gson = new Gson();
            resp = post("userstate", gson.toJson(userStateInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(resp);
    }

    public void offline(Integer userid) {
        String resp = null;
        Gson gson = new Gson();
        resp = delete("userstate", gson.toJson(userid));

        System.out.println(resp);
    }


}
