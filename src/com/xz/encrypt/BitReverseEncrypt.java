package com.xz.encrypt;

public class BitReverseEncrypt implements Encrypt {

	@Override
	public byte encrypt(byte abyte) {
		return this.bitreverse(abyte);
	}

	@Override
	public byte decrypt(byte abyte) {
		return this.bitreverse(abyte);
	}
	
	private byte bitreverse(byte abyte){
		return (byte) (~abyte & 0x0FF);
	}
	
	private byte[] bitreverse(byte[] bytes){
		byte[] res = new byte[bytes.length];
		for (int i =0 ; i<bytes.length; i++)
			res[i]=this.bitreverse(bytes[i]);
		return res;
	}

	@Override
	public byte[] encrypt(byte[] bytes) {
		return this.bitreverse(bytes);
	}

	@Override
	public byte[] decrypt(byte[] bytes) {
		return this.bitreverse(bytes);
	}
}
