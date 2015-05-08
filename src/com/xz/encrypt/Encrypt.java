package com.xz.encrypt;

public interface Encrypt {
	//BitReverseEncrypt
	String ENC_BR = "BR";
	//BitComplementEncrypt
	String ENC_BC = "BC";
	String ENC_FILE_SUFF = ".xzenc";
	String ENC_COPYRIGHT = "XZED";
	int ENC_PASSWD_LEN = 32;
	int ENC_TYPE_LEN = 2;
	int ENC_OFFSET_LEN = 1;
	int ENC_COPYRIGHT_LEN = ENC_COPYRIGHT.length();
	public byte encrypt(byte abyte);
	public byte decrypt(byte abyte);
	public byte[] encrypt(byte[] bytes);
	public byte[] decrypt(byte[] bytes);
}
