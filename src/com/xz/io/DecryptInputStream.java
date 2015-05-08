package com.xz.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.xz.encrypt.Encrypt;

public class DecryptInputStream extends FilterInputStream {
	Encrypt encrypt;

	public DecryptInputStream(InputStream arg0, Encrypt encrypt) {
		super(arg0);
		this.encrypt = encrypt;
	}
	
	public int read() throws IOException {
		int c = super.read();
		return (c==-1) ? c : encrypt.decrypt((byte)c);
	}
	
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}
		
	public int read(byte[] b, int offset, int len) throws IOException {
		int result = super.read(b, offset, len);
		for (int i = offset; i < offset+result; i++) {
			//b[i] = (byte)Character.toLowerCase((char)b[i]);
			b[i] = encrypt.decrypt(b[i]);
		}
		return result;
	}


}
