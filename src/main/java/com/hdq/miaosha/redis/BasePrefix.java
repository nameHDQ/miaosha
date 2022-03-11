package com.hdq.miaosha.redis;

/**
 * @author hdq
 */
public abstract class BasePrefix implements KeyPrefix {


    /**
     * 默认0代表永不过期
     */
    public int expireSeconds;

    public String prefix;

    /**
     * 0 用户key默认永远不过期
     * @param prefix
     */
    public BasePrefix(String prefix) {
        this(0, prefix);
    }
    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }


    @Override
    public int expireSeconds() {
        return expireSeconds;
    }


    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}

