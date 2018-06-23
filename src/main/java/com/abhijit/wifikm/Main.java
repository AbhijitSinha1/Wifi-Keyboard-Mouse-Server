package com.abhijit.wifikm;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

import org.json.JSONObject;

public class Main {

	private static Robot robot;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException, AWTException {
		ServerSocket server = new ServerSocket(9999);
		robot = new Robot();

		Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces();
		while (ni.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) ni.nextElement();
			Enumeration<InetAddress> en = n.getInetAddresses();
			while (en.hasMoreElements()) {
				InetAddress i = (InetAddress) en.nextElement();
				String address = i.getHostName();
				System.out.println(String.format("server started at %s:%s", address, server.getLocalPort()));
			}
		}
		while (true) {
			Socket socket = server.accept();
			System.out.println("Connection establised");
			InputStream inputStream = socket.getInputStream();
			handleSocketConnection(inputStream);
		}
	}

	private static void handleSocketConnection(InputStream inputStream) throws IOException {
		new Thread(() -> {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String text = "";
			try {
				while ((text = br.readLine()) != null) {
					JSONObject json = new JSONObject(text);
					process(json);
				}
			} catch (IOException e) {
			}
		}).start();
	}

	private static void process(JSONObject json) {
		new Thread(() -> {
			String type = json.getString("type");
			switch (type) {
			case "MOUSE_SCROLL": {
				JSONObject value = json.getJSONObject("value");
				float y = value.getFloat("y");
				robot.mouseWheel((int) y);
			}
				break;
			case "MOUSE_LEFT_CLICK": {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
				break;
			case "MOUSE_DOUBLE_CLICK": {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
				break;
			case "MOUSE_RIGHT_CLICK": {
				robot.mousePress(InputEvent.BUTTON3_MASK);
				robot.mouseRelease(InputEvent.BUTTON3_MASK);
			}
				break;
			case "MOUSE_MOVE": {
				JSONObject value = json.getJSONObject("value");

				float dx = value.getFloat("x");
				float dy = value.getFloat("y");

				Point location = MouseInfo.getPointerInfo()
					.getLocation();

				double x = location.getX() - dx;
				double y = location.getY() - dy;

				robot.mouseMove((int) x, (int) y);
			}
				break;
			case "KEYBOARD_INPUT": {
				JSONObject value = json.getJSONObject("value");
//				System.out.println(value);
				int keyCode = value.getInt("keyCode");
				if(keyCode == 0) {
					break;
				}
				boolean isShiftPressed = value.getBoolean("shift");
				int event = KeyEvent.getExtendedKeyCodeForChar(keyCode);

				if(isShiftPressed) {
					robot.keyPress(KeyEvent.VK_SHIFT);
				}
				robot.keyPress(event);
				robot.keyRelease(event);
				if(isShiftPressed) {
					robot.keyRelease(KeyEvent.VK_SHIFT);
				}
			}
				break;
			}
		}).start();
	}
}