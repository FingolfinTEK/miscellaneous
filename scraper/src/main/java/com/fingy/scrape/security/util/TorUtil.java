package com.fingy.scrape.security.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TorUtil {

	private static final String TOR_LOCATION = "tor/start.exe";
	private static final String AUTHENTICATE_COMMAND = "AUTHENTICATE \"aprodhu123\"\r\n";
	private static final String NEW_IDENTITY_COMMAND = "SIGNAL NEWNYM\r\n";
	private static final String SHUTDOWN_IDENTITY_COMMAND = "SIGNAL SHUTDOWN\r\n";
	private static final String TERMINATE_IDENTITY_COMMAND = "SIGNAL TERM\r\n";

	private static final String TOR_SOKCS_HOST = "127.0.0.1";
	private static final String TOR_SOKCS_PORT = "9150";

	private static final String SOCKS_PROXY_HOST = "socksProxyHost";
	private static final String SOCKS_PROXY_PORT = "socksProxyPort";

	private static Logger logger = LoggerFactory.getLogger(TorUtil.class);

	private static Process torProcess;

	public static void executeTorCommands(String... commands) {
		Map<Object, Object> backup = copySystemProperties();
		disableSocksProxy();

		try {
			Socket socket = new Socket(TOR_SOKCS_HOST, 9151);

			for (String command : commands) {
				logger.trace("Seding command to Tor: " + command);
				socket.getOutputStream().write(command.getBytes());

				InputStreamReader torResponseReader = new InputStreamReader(socket.getInputStream());
				BufferedReader bufferedTorResponseReader = IOUtils.toBufferedReader(torResponseReader);
				String response = bufferedTorResponseReader.readLine();
				logger.trace("Received response from Tor: " + response);
			}
		} catch (IOException e) {
			logger.error("Tor not running or wrong control port");
		}

		restoreSystemProperties(backup);
	}

	public static void requestNewIdentity() {
		executeTorCommands(AUTHENTICATE_COMMAND, NEW_IDENTITY_COMMAND);
	}

	public static void shutdownTor() {
		executeTorCommands(AUTHENTICATE_COMMAND, SHUTDOWN_IDENTITY_COMMAND);
	}

	public static void terminateTor() {
		executeTorCommands(AUTHENTICATE_COMMAND, TERMINATE_IDENTITY_COMMAND);
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
				torProcess = Runtime.getRuntime().exec(TOR_LOCATION);
			logger.trace("Started Tor");
		} catch (IOException e) {
			logger.error("Exception occured", e);
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
			shutdownTor();
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("Exception occured", e);
		} finally {
			forciblyTerminateProcesses();
		}
	}

	private static void forciblyTerminateProcesses() {
		try {
			Runtime.getRuntime().exec("Taskkill /IM vidalia.exe /F").waitFor();
			Runtime.getRuntime().exec("Taskkill /IM tor.exe /F").waitFor();
			logger.trace("Stopped Tor");
		} catch (InterruptedException | IOException e) {
			logger.error("Exception occured", e);
		}
	}

}
