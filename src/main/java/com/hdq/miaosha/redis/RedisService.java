package com.hdq.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author hdq
 */
@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;



    /**
     * 通过 jedispool 获取jedis  然后获取单个对象
     * @param key
     * @param clazz 获取数据的class对象模型 根据class 调用fastjson生成类
     * Class<T> clazz 泛型类 任何继承该类的类都可以
     * @param <T>
     * @return
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        }finally {
                returnToPoll(jedis);
        }
    }

    /**
     * 使用fastjson 转换成 bean
     * @param str
     * @param clazz 数据的对想模型
     * @param <T>
     * @return
     */
    //: TODO: 2022/2/3 支持list类型 转换
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() == 0 || clazz == null){
            return null;
        }
        // 类型判断
        if (clazz == int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if (clazz == long.class || clazz == Long.class){
            return (T) Long.valueOf(str);
        }else if (clazz == String.class){
            return (T) str;
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    /**
     * 存储数据
     * @param prefix key前缀
     * @param key   key
     * @param value value
     * @param <T>  泛型
     * @return
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            // 转换成json
            String str = beanToValue(value);
            if (str == null || str.length() <= 0){
                return false;
            }
            int expireSeconds = prefix.expireSeconds();
            String realKey = prefix.getPrefix() + key;
            // 过期时间
            if (expireSeconds <= 0){
                jedis.set(realKey, str);
            }else {
                jedis.setex(realKey, expireSeconds, str);
            }
            return true;
        }finally {
            returnToPoll(jedis);
        }
    }

    /**
     * 使用fastjson 转换成json string
     * @param value
     * @param <T>
     * @return
     */
    // TODO: 2022/2/3 支持list类型 转换
    public static <T> String beanToValue(T value) {
        if (value == null){
            return null;
        }
        Class clazz = value.getClass();
        // 类型判断
        if (clazz == int.class || clazz == Integer.class){
            return "" + value;
        }else if (clazz == long.class || clazz == Long.class){
            return "" + value;
        }else if (clazz == String.class){
            return (String) value;
        }
        return JSON.toJSONString(value);
    }

    /**
     *  是否已经存在key
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean exits(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            if (key == null || key.length() <= 0){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            Boolean exists = jedis.exists(realKey);
            return exists;
        }finally {
            returnToPoll(jedis);
        }
    }

    /**
     * incr     
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    // TODO: 2022/2/4 若是  key 不存在 如何加以限制呢？
    public <T> Long incr(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        }finally {
            returnToPoll(jedis);
        }
    }


    /**
     * decr
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    // TODO: 2022/2/4 若是  key 不存在 如何加以限制呢？
    public <T> Long decr(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        }finally {
            returnToPoll(jedis);
        }
    }


    /**
     *@Description : 删除
     *@Author : huangdaoquan
     *@Date : 2022/6/27 17:40
     *@Version : 1.0
     **/
    public boolean delete(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try {
            // 通过连接池获取 jedis
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            Long del = jedis.del(realKey);
            return del > 0;
        }finally {
            returnToPoll(jedis);
        }
    }

    /**
     * 关闭jedis 返回连接池
     * @param jedis
     */
    private void returnToPoll(Jedis jedis) {
        if (jedis != null){
            //jedis关闭 但是连接池不关闭  ，返回连接池
            jedis.close();
        }
    }


}
