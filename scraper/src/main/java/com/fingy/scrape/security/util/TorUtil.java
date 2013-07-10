package com.fingy.scrape.security.util;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorUtil {

	private static final byte[] NEW_IDENTITY_COMMAND_BYTES = "SIGNAL NEWNYM".getBytes();

	private static final String TOR_SOKCS_HOST = "127.0.0.1";
	private static final String TOR_SOKCS_PORT = "9150";

	private static final String SOCKS_PROXY_HOST = "socksProxyHost";
	private static final String SOCKS_PROXY_PORT = "socksProxyPort";

	private static Logger logger = LoggerFactory.getLogger(TorUtil.class);

	private static Process torProcess;
	private static String torBundleLocation;

	public static void setTorBundleLocation(String location) {
		torBundleLocation = location;
	}

	public static void requestNewIdentity() {
		try {
			Map<Object, Object> backup = copySystemProperties();
			disableSocksProxy();

			Socket socket = new Socket(TOR_SOKCS_HOST, 9151);
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
		System.getProperties().remove(SOCKS_PROXY_HOST);
		System.getProperties().remove(SOCKS_PROXY_PORT);
	}

	public static void useTorAsProxy() {
		System.getProperties().setProperty(SOCKS_PROXY_HOST, TOR_SOKCS_HOST);
		System.getProperties().setProperty(SOCKS_PROXY_PORT, TOR_SOKCS_PORT);
	}

	public static String getTorProxyVMArguments() {
		StringBuilder vmArgs = new StringBuilder();
		vmArgs.append("-D").append(SOCKS_PROXY_HOST).append("=").append(TOR_SOKCS_HOST);
		vmArgs.append(" ");
		vmArgs.append("-D").append(SOCKS_PROXY_PORT).append("=").append(TOR_SOKCS_PORT);
		return vmArgs.toString();
	}

	public static void startTor() {
		try {
			if (torProcess == null || processTerminated())
				torProcess = Runtime.getRuntime().exec(torBundleLocation);
			logger.debug("Started Tor");
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
			logger.debug("Stopped Tor");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
