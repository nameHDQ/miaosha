package com.hdq.miaosha.redis;

public class UserKey extends BasePrefix{


    /**
     * 默认永不过期
     * @param prefix
     */
    private UserKey( String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");
}
