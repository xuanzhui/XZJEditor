package com.xz.encrypt;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.xz.utils.CommonOperation;

public class MD5Encrypt {

	public static void main(String[] args) {
		System.out.println(System.getProperty("user.home"));
		System.out.println(MD5Encrypt.stringMD5("hehe"));
	}

	public static String stringMD5(String input) {

		try {

			// 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）

			MessageDigest messageDigest = MessageDigest.getInstance("MD5");

			// 输入的字符串转换成字节数组

			byte[] inputByteArray = input.getBytes();

			// inputByteArray是输入字符串转换得到的字节数组

			messageDigest.update(inputByteArray);

			// 转换并返回结果，也是字节数组，包含16个元素

			byte[] resultByteArray = messageDigest.digest();

			// 字符数组转换成字符串返回

			return CommonOperation.byteArrayToHex(resultByteArray);

		} catch (NoSuchAlgorithmException e) {

			return null;

		}

	}
}
