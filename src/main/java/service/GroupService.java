package service;

import com.google.gson.reflect.TypeToken;
import proto.MatchGroupRelation;
import tools.GsonTools;
import tools.OkHttpTools;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GroupService {

    public static List<MatchGroupRelation> getMatchGroupRelationsByGroupID(int groupID) {
        return parseMatchGroupRelation(OkHttpTools.get("matchgroup/alluser?matchgroupid=" + groupID));
    }


    private static List<MatchGroupRelation> parseMatchGroupRelation(String json) {
        Type type = new TypeToken<ArrayList<MatchGroupRelation>>() {
        }.getType();
        return GsonTools.getGson().fromJson(json, type);
    }

}
