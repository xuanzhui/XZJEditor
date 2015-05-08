package com.xz.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.xz.encrypt.Encrypt;

public class EncryptOutputStream extends FilterOutputStream {
	Encrypt encrypt;
	
	public EncryptOutputStream(OutputStream arg0, Encrypt encrypt) {
		super(arg0);
		this.encrypt = encrypt;
	}
	
	public void write(int b) throws IOException{
		this.out.write(encrypt.encrypt((byte) b));
	}
	
	public void write(byte[] b) throws IOException{
		this.write(encrypt.encrypt(b), 0, b.length);
	}
	
	public void write(byte[] b, int off, int len) throws IOException{
		this.out.write(encrypt.encrypt(b), off, len);
	}
}
