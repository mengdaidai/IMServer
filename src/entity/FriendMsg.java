package entity;


public class FriendMsg {
    String friend_name;
    String friend_msg;
    String friend_sign;
    byte[] head_picture;

    public String getFriend_name() {
        return friend_name;
    }

    public void setFriend_name(String friend_name) {
        this.friend_name = friend_name;
    }

    public String getFriend_msg() {
        return friend_msg;
    }

    public void setFriend_msg(String friend_msg) {
        this.friend_msg = friend_msg;
    }

    public byte[] getHead_picture() {
        return head_picture;
    }

    public void setHead_picture(byte[] head_picture) {
        this.head_picture = head_picture;
    }

    public String getFriend_sign() {
        return friend_sign;
    }

    public void setFriend_sign(String friend_sign) {
        this.friend_sign = friend_sign;
    }
}
