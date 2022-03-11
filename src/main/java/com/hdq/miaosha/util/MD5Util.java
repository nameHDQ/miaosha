package com.hdq.miaosha.util;


import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {



    public static final String SALT = "1a2b3c4d";


    /**
     * md5加密
     * @param src
     * @return
     */
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }


    /**
     * 二次md5加密  防止破解
     * 第一次，将用户输入的转换成 表单提交的
     * @param inputPass 用户输入密码
     * @return
     */
    public static String inputPassToFormPass(String inputPass){
        String str ="" + SALT.charAt(0) + SALT.charAt(2) + inputPass + SALT.charAt(5) + SALT.charAt(4);
        //System.out.println(str);
        return md5(str);
    }

    /**
     * 二次md5加密  防止破解
     * 第二次次，将FORM输入的转换成 数据库提交的
     * @param formPass 表单密码
     * @param salt 随机码
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt){
        String str ="" + salt.charAt(0) + salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }


    public static String inputPassToDBPass(String inputPass, String saltDB){
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456"));
        System.out.println(formPassToDBPass(inputPassToFormPass("123456"), "1a2b3c4d"));
    }
}
