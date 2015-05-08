package com.xz.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.xz.encrypt.BitComplementEncrypt;
import com.xz.encrypt.BitReverseEncrypt;
import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;

public class EncryptOutputAttachEncInfo {
	private String filepath;
	private String encfile;
	private String md5password;
	private String encType;
	private int offset;
	private FileOutputStream fileOutput;
	private FileInputStream fileInput;
	
	public EncryptOutputAttachEncInfo(String filepath, String md5password, String encType, int offset) throws FileNotFoundException{
		this.filepath=filepath;
		this.md5password=md5password;
		this.encType = encType;
		this.offset=offset;
		
		fileInput = new FileInputStream(this.filepath);
		
		encfile = this.filepath+Encrypt.ENC_FILE_SUFF;
		
		fileOutput = new FileOutputStream(encfile);
	}
	
	public EncryptOutputAttachEncInfo(String filepath, String md5password, String encType) throws FileNotFoundException{
		this(filepath, md5password, encType, 0);
	}

	public String encrypt() throws EncryptException, IOException{
		this.attachEncInfo();
		
		Encrypt encrypt;
		if (this.encType.equals(Encrypt.ENC_BR))
			encrypt = new BitReverseEncrypt();
		else if (this.encType.equals(Encrypt.ENC_BC))
			encrypt = new BitComplementEncrypt(offset);
		else
			throw new EncryptException("Unsupported Encryption Type!");
		
		BufferedOutputStream bos = new BufferedOutputStream(new EncryptOutputStream(this.fileOutput, encrypt));
		
		BufferedInputStream bis = new BufferedInputStream(this.fileInput);
		
		byte[] buf = new byte[512];
		
		int len;
		while ((len = bis.read(buf)) != -1){
			bos.write(buf, 0, len);
		}
		
		bis.close();
		bos.flush();bos.close();
		
		return this.encfile;
	}
	
	public void attachEncInfo() throws IOException{
		//4byte? copyright
		fileOutput.write(Encrypt.ENC_COPYRIGHT.getBytes("UTF-8"));
		//32byte md5
		fileOutput.write(this.md5password.getBytes("UTF-8"));
		//2byte type
		fileOutput.write(this.encType.getBytes("UTF-8"));
		//1byte offset
		fileOutput.write(offset);
		
		fileOutput.flush();
	}
}
