package main;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Main {
	
	public static String[] splitRules(String s){
		if(s == null || s.length() == 0) return null;
		return s.split(";");
	}
	
	public static String[] splitIPandMAC(String s){
		if(s == null || s.length() == 0) return null;
		return s.split("\\t");
	}
	
	
	public static void main(String[] args) throws UnknownHostException, SocketException, IOException, InterruptedException  {
        // TODO code application logic here
		RawPacketSender sender = new RawPacketSender();
		byte[] packet = "Hello".getBytes();
		String s = "10.0.0.1\t10.0.0.2\t00:00:00:00:00:00\t00:00:00:00:00:01;";
				//+ "192.1.10.2\t10.0.0.2\t00:00:00:00:00:02\t00:00:00:00:00:03";
		String [] tuple = splitRules(s);
		while(true){
			for(String t : tuple){
				sender.sendPacket(splitIPandMAC(t), packet);
			}
			Thread.sleep(1000);
		}
		
		
    }
}
