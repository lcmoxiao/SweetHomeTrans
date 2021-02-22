package service;

import pojo.Offset;
import tools.GsonTools;
import tools.OkHttpTools;

import java.io.IOException;

import static tools.OkHttpTools.post;

public class OffsetService {

    public static int getOffset(int userid) {
        String s = OkHttpTools.get("offset?userid=" + userid);
        return Integer.parseInt(s);
    }

    public static void insertOffset(Offset offset) {
        try {
            post("offset", GsonTools.getGson().toJson(offset));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
