package com.xz.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import javax.swing.JOptionPane;

import com.xz.encrypt.MD5Encrypt;
import com.xz.frame.EditorFrame;

public class Main {

	public static String pwfile=".XZJEditor/password.dat";
	
	public static void main(String[] args) {
		
		if (new Main().verifyPassword())
			new EditorFrame();
		
	}

	public void initialPassword(String password){
		String userhome = System.getProperty("user.home");
		String filename=userhome + '/' + pwfile;
		String abspwdir=userhome + "/.XZJEditor";
		
		if (!Files.exists(Paths.get(abspwdir))) {
			try {
				Files.createDirectories(Paths.get(abspwdir));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
			fw.write(MD5Encrypt.stringMD5(password));
			fw.flush();
			fw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean verifyPassword(){
		boolean res=false;
		
		String pw = null;
		
		if (!Files.exists(Paths.get(System.getProperty("user.home") + '/' + pwfile))) {
			do {
				do {
					pw= JOptionPane.showInputDialog("Please initialize password: ");
					if (pw == null || pw.length()==0){
						JOptionPane.showMessageDialog(null, "You must input a valid password!", "Error Message",JOptionPane.ERROR_MESSAGE);
					}else{
						break;
					}
				}while (true);
			}while(JOptionPane.showConfirmDialog(null, "your password is "+pw+" , do you confirm?", "password confirm",JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION);
		
			this.initialPassword(pw);
			
			res = true;
		} else {
			for (int i =0 ; i < 5 ; i++){
				pw= JOptionPane.showInputDialog("Please input password: ");
				if (pw == null){
					break;
				}else if (pw.length() ==0){
					JOptionPane.showMessageDialog(null, "You must input a valid password!", "Error Message",JOptionPane.ERROR_MESSAGE);
				}else {
					if (this.comparePassword(pw)){
						res = true;
						break;
					}else{
						if (i < 4)
							JOptionPane.showMessageDialog(null, "incorrect password! you have " + (5-i-1) + " more chance(s)", "Error Message",JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
		return res;
	}
	
	private boolean comparePassword(String password){
		boolean res = false;
		String userhome = System.getProperty("user.home");
		String filename=userhome + '/' + pwfile;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String enpw=br.readLine();
			res=enpw.endsWith(MD5Encrypt.stringMD5(password));
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
}
