package com.xz.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.xz.encrypt.BitComplementEncrypt;
import com.xz.encrypt.BitReverseEncrypt;
import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.io.DecryptInputDetachEncInfo;
import com.xz.io.DecryptInputStream;
import com.xz.io.EncryptOutputAttachEncInfo;
import com.xz.io.EncryptOutputStream;

public class TestEncrypt {

	public static void main(String[] args) throws IOException, EncryptException {
		//byte[] bytes = Encrypt.ENC_BR.getBytes("UTF-8");

		/*try {
			FileOutputStream fos = new FileOutputStream("test.txt");
			fos.write(258);
			fos.write(12);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		/*
		byte b255 = (byte) 0xFF;
		System.out.println(b255);
		
		System.out.println(b255 & 0x0FF);
		
		System.out.println(new String(new byte[]{66,68}));
		*/

		/*
		testEncrypt();
		testDecrypt();
		System.out.println("done");*/

		String srcFile = "testenc.txt";
		EncryptOutputAttachEncInfo eo = new EncryptOutputAttachEncInfo(srcFile, "529CA8050A00180790CF88B63468826A", Encrypt.ENC_BC, 2);
		String encFile = eo.encrypt();

		DecryptInputDetachEncInfo dd = new DecryptInputDetachEncInfo(encFile, "529CA8050A00180790CF88B63468826A");
		System.out.println(dd.decryptToString());

	}

	public static void testEncrypt(){
		Encrypt encrypt=new BitComplementEncrypt(22);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new EncryptOutputStream(new FileOutputStream("test.encrp"), encrypt));
		
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream("src/com/xz/encrypt/MD5Encrypt.java"));
			
			byte[] buf = new byte[512];
			
			int len=0;
			while ((len = bis.read(buf)) != -1){
				bos.write(buf, 0, len);
			}
			
			bis.close();
			bos.flush();bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void testDecrypt() {
		Encrypt encrypt=new BitComplementEncrypt(22);
		
		try {
			BufferedInputStream bis = new BufferedInputStream(new DecryptInputStream(new FileInputStream("test.encrp"), encrypt));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("test.decry"));
			
			byte[] buf = new byte[512];
			
			int len=0;
			while ((len = bis.read(buf)) != -1){
				bos.write(buf, 0, len);
			}
			
			bis.close();
			bos.flush();bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
