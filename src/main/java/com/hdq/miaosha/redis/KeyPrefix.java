package com.hdq.miaosha.redis;

/**
 * redis 标准接口
 */
public interface KeyPrefix {

    /**
     *
     * @return
     */
    public int expireSeconds();

    /**
     *
     * @return
     */
    public String getPrefix();
}
