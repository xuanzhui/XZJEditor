package com.xz.io;

import java.io.*;

import com.xz.encrypt.BitComplementEncrypt;
import com.xz.encrypt.BitReverseEncrypt;
import com.xz.encrypt.Encrypt;
import com.xz.encrypt.EncryptException;
import com.xz.utils.CommonOperation;

public class DecryptInputDetachEncInfo {
	private String encfile;
	private String decfile;
	private String md5password;
	private String encType;
	private int offset;
	private FileInputStream fileInput;
	private OutputStream output;
	private String decryptContent;
	
	public DecryptInputDetachEncInfo(String encfile, String md5password) throws EncryptException, FileNotFoundException{
		if (!encfile.endsWith(Encrypt.ENC_FILE_SUFF))
			throw new EncryptException("Invalid file name!");
		
		this.encfile = encfile;
		fileInput = new FileInputStream(this.encfile);
		
		this.md5password = md5password;
	}
	
	private void detachEncInfo() throws IOException, EncryptException{
		byte[] encpr = new byte[Encrypt.ENC_COPYRIGHT_LEN];
		if (fileInput.read(encpr) < Encrypt.ENC_COPYRIGHT_LEN)
			throw new EncryptException("Invalid file content -- ENC_COPYRIGHT issue!");
		if (! new String(encpr, "UTF-8").equals(Encrypt.ENC_COPYRIGHT))
			throw new EncryptException("ENC_COPYRIGHT does NOT match!");

		byte[] password = new byte[Encrypt.ENC_PASSWD_LEN];
		if (fileInput.read(password) < Encrypt.ENC_PASSWD_LEN)
			throw new EncryptException("Invalid file content -- password issue!");
		if (! new String(password, "UTF-8").equals(this.md5password))
			throw new EncryptException("password does NOT match!");

		byte[] etype = new byte[Encrypt.ENC_TYPE_LEN];
		if (fileInput.read(etype) < Encrypt.ENC_TYPE_LEN)
			throw new EncryptException("Invalid file content -- encrypt type issue!");

		encType = new String(etype);

		byte[] boffset = new byte[Encrypt.ENC_OFFSET_LEN];
		if (fileInput.read(boffset) < Encrypt.ENC_OFFSET_LEN)
			throw new EncryptException("Invalid file content -- encrypt offset issue!");
		offset = 0x0FF & boffset[0];
	}
	
	private void decrypt(boolean writeToFile, String fileCharset) throws IOException, EncryptException {
		this.detachEncInfo();

		if (writeToFile){
			this.decfile = this.encfile.substring(0, this.encfile.lastIndexOf('.'));
			output = new FileOutputStream(this.decfile);
		} else {
			output = new ByteArrayOutputStream();
		}

		Encrypt encrypt;
		if (this.encType.equals(Encrypt.ENC_BR))
			encrypt = new BitReverseEncrypt();
		else if (this.encType.equals(Encrypt.ENC_BC))
			encrypt = new BitComplementEncrypt(offset);
		else
			throw new EncryptException("Unsupported Encryption Type!");

		BufferedInputStream bis = new BufferedInputStream(new DecryptInputStream(fileInput, encrypt));
		BufferedOutputStream bos = new BufferedOutputStream(output);

		byte[] buf = new byte[512];

		int len;
		while ((len = bis.read(buf)) != -1){
			bos.write(buf, 0, len);
		}

		bis.close();
		bos.flush();

		if (!writeToFile) {
			if (fileCharset == null)
				fileCharset = "UTF-8";
			decryptContent = new String(((ByteArrayOutputStream)output).toByteArray(), fileCharset);
		}

		bos.close();
	}

	public String decryptToFile() throws IOException, EncryptException {
		this.decrypt(true, null);
		return this.decfile;
	}

	public String decryptToString() throws IOException, EncryptException {
		this.decrypt(false, null);
		return this.decryptContent;
	}
}
