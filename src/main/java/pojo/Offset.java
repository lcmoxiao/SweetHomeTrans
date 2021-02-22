package pojo;

public class Offset {
    private Integer userid;

    private Integer offset;

    public Offset(int userid, int offset) {
        this.userid = userid;
        this.offset = offset;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}