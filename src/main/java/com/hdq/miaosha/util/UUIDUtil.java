package com.hdq.miaosha.util;

import java.util.UUID;

/**
 * 生成随机UUID
 * @author hdq
 */
public class UUIDUtil {
	public static String uuid() {
		// 去掉“-”符号
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static void main(String[] args) {
		System.out.println(UUID.randomUUID().toString());
	}
}
