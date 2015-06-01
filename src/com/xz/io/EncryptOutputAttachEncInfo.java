package com.xz.io;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.xz.encrypt.BitComplementEncrypt;
import com.xz.encrypt.BitReverseEncrypt;
import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;

public class EncryptOutputAttachEncInfo {
	private String destFilePath;
	private String md5password;
	private String encType;
	private int offset;
	private FileOutputStream fileOutput;
	//private FileInputStream fileInput;
	
	public EncryptOutputAttachEncInfo(String destFilePath, String md5password, String encType, int offset) throws FileNotFoundException {
		this.destFilePath=destFilePath;
		this.md5password=md5password;
		this.encType = encType;
		this.offset=offset;
		
		//fileInput = new FileInputStream(this.filepath);

		if (! this.destFilePath.endsWith(Encrypt.ENC_FILE_SUFF))
			this.destFilePath = this.destFilePath+Encrypt.ENC_FILE_SUFF;
		
		fileOutput = new FileOutputStream(this.destFilePath);
	}
	
	public EncryptOutputAttachEncInfo(String destFilePath, String md5password, String encType) throws FileNotFoundException{
		this(destFilePath, md5password, encType, 0);
	}

	private void encrypt(boolean fromFile, String srcFilePath, byte[] srcBytes) throws EncryptException, IOException{
		this.attachEncInfo();
		
		Encrypt encrypt;
		if (this.encType.equals(Encrypt.ENC_BR))
			encrypt = new BitReverseEncrypt();
		else if (this.encType.equals(Encrypt.ENC_BC))
			encrypt = new BitComplementEncrypt(offset);
		else
			throw new EncryptException("Unsupported Encryption Type!");
		
		BufferedOutputStream bos = new BufferedOutputStream(new EncryptOutputStream(this.fileOutput, encrypt));
		
		BufferedInputStream bis = null;

		if (fromFile)
			bis = new BufferedInputStream(new FileInputStream(srcFilePath));
		else
			bis = new BufferedInputStream(new ByteArrayInputStream(srcBytes));
		
		byte[] buf = new byte[512];
		
		int len;
		while ((len = bis.read(buf)) != -1){
			bos.write(buf, 0, len);
		}
		
		bis.close();
		bos.flush();bos.close();
	}

	public String encryptFromFile(String srcFilePath) throws IOException, EncryptException {
		if (!Files.exists(Paths.get(srcFilePath)))
			throw new FileNotFoundException("can't find file "+srcFilePath);

		this.encrypt(true, srcFilePath, null);

		return this.destFilePath;
	}

	public String encryptFromString(String srcStringContent, String stringCharset) throws IOException, EncryptException {
		String realCset = stringCharset;
		if (stringCharset == null)
			realCset = "UTF-8";

		this.encrypt(false, null, srcStringContent.getBytes(realCset));

		return this.destFilePath;
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
