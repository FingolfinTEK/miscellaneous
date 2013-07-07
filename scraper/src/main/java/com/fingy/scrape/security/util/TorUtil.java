package com.fingy.scrape.security.util;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TorUtil {

	private static final byte[] NEW_IDENTITY_COMMAND_BYTES = "SIGNAL NEWNYM".getBytes();
	
	private static Process torProcess;

	public static void requestNewIdentity() {
		try {
			Map<Object, Object> backup = copySystemProperties();
			disableSocksProxy();
			
			Socket socket = new Socket("127.0.0.1", 9151);
			socket.getOutputStream().write(NEW_IDENTITY_COMMAND_BYTES);
			
			restoreSystemProperties(backup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void restoreSystemProperties(Map<?, ?> backup) {
		System.getProperties().putAll(backup);
	}

	private static HashMap<Object, Object> copySystemProperties() {
		return new HashMap<>(System.getProperties());
	}

	public static void disableSocksProxy() {
		System.getProperties().remove("socksProxyHost");
		System.getProperties().remove("socksProxyPort");
	}

	public static void useTorAsProxy() {
		System.getProperties().setProperty("socksProxyHost", "127.0.0.1");
		System.getProperties().setProperty("socksProxyPort", "9150");
	}

	public static void startTor() {
		try {
			if (torProcess == null || processTerminated())
				torProcess = Runtime.getRuntime().exec("C:/Users/Fingy/Downloads/Tor Browser/Start Tor Browser.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean processTerminated() {
		try {
			torProcess.exitValue();
		} catch (IllegalThreadStateException e) {
			return false;
		}
		return true;
	}
	
	public static void startAndUseTorAsProxy() {
		startTor();
		useTorAsProxy();
	}

	public static void stopTor() {
		try {
			Runtime.getRuntime().exec("Taskkill /IM tbb-firefox.exe /F").waitFor();
			Runtime.getRuntime().exec("Taskkill /IM vidalia.exe /F").waitFor();
			Runtime.getRuntime().exec("Taskkill /IM tor.exe /F").waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
