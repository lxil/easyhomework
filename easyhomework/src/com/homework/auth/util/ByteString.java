package com.homework.auth.util;

public class ByteString {

    public static String bytesToString(byte[] bs) {  
    	int len = bs.length;
    	if (bs == null || len == 0)  
            return "";  
        StringBuffer sb = new StringBuffer();        
        for (int i = 0; i < len; i++) {  
            // System.out.println(bs[i]+":"+String.format("%02X", bs[i]));  
            sb.append(String.format("%02X", bs[i]));  
        }  
        return sb.toString();  
    }  
  
    public static byte[] stringToBytes(String str) {  
        if (str == null || str.length() < 2 || str.length() % 2 != 0)  
            return new byte[0];  
        int len = str.length();  
        byte[] bs = new byte[len / 2];  
        for (int i = 0; i * 2 < len; i++) {  
            bs[i] = (byte) (Integer.parseInt(str.substring(i * 2, i * 2 + 2),  
                    16) & 0xFF);  
            // System.out.println(str.substring(i * 2, i * 2 + 2)+":"+bs[i]);  
        }  
        return bs;  
    }  
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}


