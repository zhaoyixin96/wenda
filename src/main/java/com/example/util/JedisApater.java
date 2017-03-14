package com.example.util;

import com.alibaba.fastjson.JSONObject;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

/**
 * Created by YiXin on 2017/3/8.
 */
@Service
public class JedisApater implements InitializingBean{
    private static final Logger logger = LoggerFactory.getLogger(JedisApater.class);
    private JedisPool pool;
    public static void print(int index, Object obj) {

        System.out.println(String.format("{%d}, %s", index, obj));
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("redis://localhost:6379/9");
        jedis.flushDB();

        jedis.set("hello", "world");
        print(1, jedis.get("hello"));
        jedis.rename("hello", "newHello");
        print(2, jedis.get("newHello"));
//        jedis.setex("wa", 15, "0");

        // get and set
        jedis.set("pv", "100");
        jedis.incr("pv");
        print(3, jedis.get("pv"));
        jedis.incrBy("pv", 5);
        print(4, jedis.get("pv"));
        jedis.decrBy("pv", 2);
        print(5, jedis.get("pv"));

        print(6, jedis.keys("*"));

        //List
        String listName = "list";
        jedis.del("list");
        for (int i = 0; i < 10; i++) {
            jedis.lpush(listName, "a" + String.valueOf(i));
        }
        print(7, jedis.lrange(listName, 0, 11));
        print(8, jedis.lrange(listName, 0, 4));
        print(9, jedis.llen(listName));
        jedis.lpop(listName);
        print(10, jedis.llen(listName));

        //Hash
        String userKey = "userxx";
        jedis.hset(userKey, "name", "sam");
        jedis.hset(userKey, "age", "22");
        jedis.hset(userKey, "phone", "818181818");
        print(11, jedis.hget(userKey, "name"));
        print(12, jedis.hgetAll(userKey));
        jedis.hsetnx(userKey, "address", "1223");
        jedis.hsetnx(userKey, "age", "1223");
        print(13, jedis.hgetAll(userKey));

        //Set
        String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i = 0; i < 10; i++) {
            jedis.sadd(likeKey1, String.valueOf(i));
            jedis.sadd(likeKey2, String.valueOf(i*i));
        }
        print(14, jedis.smembers(likeKey1));
        print(15, jedis.smembers(likeKey2));
        print(16, jedis.sunion(likeKey1, likeKey2));
        print(17, jedis.sdiff(likeKey1, likeKey2));
        print(18, jedis.sinter(likeKey1, likeKey2));
        print(19, jedis.srem(likeKey1, "4"));
        print(20, jedis.smove(likeKey1, likeKey2, "8"));
        print(21, jedis.smembers(likeKey1));
        print(22, jedis.smembers(likeKey2));

        //Sorted Sets
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 100, "lee");
        jedis.zadd(rankKey, 45, "laa");
        jedis.zadd(rankKey, 89, "dan");
        jedis.zadd(rankKey, 47, "poo");
        jedis.zadd(rankKey, 97, "wr");
        print(23, jedis.zscore(rankKey, "lee"));
        print(24, jedis.zcard(rankKey));
        print(25, jedis.zcount(rankKey, 60, 100));
        print(26, jedis.zrange(rankKey, 0, 100));
        print(27, jedis.zrange(rankKey, 0, 10));
        print(28, jedis.zrange(rankKey, 0, 2));
        print(29, jedis.zrange(rankKey, 1, 4));
        print(30, jedis.zrevrange(rankKey, 1, 4));
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) {
            print(31, tuple.getElement() + ":" +tuple.getScore());
        }

        print(32, jedis.zrank(rankKey, "poo"));
        print(33, jedis.zrevrank(rankKey, "poo"));

        String setKey = "zset";
        jedis.zadd(setKey, 1, "a");
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");

        print(34, jedis.zlexcount(setKey, "-", "+"));
        print(35, jedis.zlexcount(setKey, "[b", "[d"));
        print(36, jedis.zlexcount(setKey, "(b", "[d"));
        jedis.zrem(setKey, "b");
        print(37, jedis.zrange(setKey, 0, 10));
        jedis.zremrangeByLex(setKey, "(c", "+");
        print(38, jedis.zrange(setKey, 0, 2));

        /*
        JedisPool pool = new JedisPool();
        for (int i = 0; i < 100; i++) {
            Jedis j = pool.getResource();
            print(39, j.get("a"));
            j.close();
        }*/

        User user = new User();
        user.setName("xx");
        user.setPassword("pp");
        user.setHeadUrl("a.jpg");
        user.setSalt("salt");
        user.setId(1);
        print(40, JSONObject.toJSONString(user));
        jedis.set("user1", JSONObject.toJSONString(user));

        String value = jedis.get("user1");
        User user2 = JSONObject.parseObject(value, User.class);
        print(41, user2);


    }

    @Override
    public void afterPropertiesSet() throws Exception {
        pool = new JedisPool("redis://localhost:6379/10");
    }

    public long sadd(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sadd(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long scard(String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public boolean sismember(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.sismember(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return false;
    }




}