package com.xz.encrypt;

public class BitComplementEncrypt implements Encrypt{
	private int seed=1;
	
	public BitComplementEncrypt(int seed){
		this.seed=seed;
	}

	@Override
	public byte encrypt(byte abyte) {
		return this.bitcomplement(abyte);
	}

	@Override
	public byte decrypt(byte abyte) {
		return this.bitcomplement(abyte);
	}

	@Override
	public byte[] encrypt(byte[] bytes) {
		return this.bitcomplement(bytes);
	}

	@Override
	public byte[] decrypt(byte[] bytes) {
		return this.bitcomplement(bytes);
	}
	
	private byte bitcomplement(byte abyte){
		return (byte) (((~abyte)+seed) & 0x0FF);
	}
	
	private byte[] bitcomplement(byte[] bytes){
		byte[] res = new byte[bytes.length];
		for (int i =0 ; i<bytes.length; i++)
			res[i]=this.bitcomplement(bytes[i]);
		return res;
	}
}