package com.hdq.miaosha.util;

import com.alibaba.fastjson.JSON;
import com.hdq.miaosha.result.CodeMsg;
import com.hdq.miaosha.result.Result;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Description : TODO
 * @Author : huangdaoquan
 * @Date : 2022/7/6 20:25
 * @Version : 1.0
 **/
public class WebUtil {
    /**
     *@Description : 拦截器返回错误信息
     *@Author : huangdaoquan
     *@Date : 2022/7/6 20:27
     *@Version : 1.0
     **/
    public static void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream outputStream = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(codeMsg));
        outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();

    }
}
