package com.ibm.di.test.utils;

import java.util.ArrayList;
import java.util.List;

import com.ibm.di.connector.ConnectorInterface;

/*
 * Test driver for a Server mode Connector.
 */
public class ServerModeConnectorDriver {

	/**
	 * Handle the communication with clients. The implementation must be thread
	 * safe. If you throw an exception it will not stop the whole server.
	 */
	public interface ClientHandler {
		void handleClient(ConnectorInterface iteratorConnector) throws Exception;
	}

	private final ConnectorInterface serverConnector;
	private final ClientHandler clientHandler;
	private volatile boolean goOn = true;
	private final Thread acceptorThread;
	private final List<Servant> servants = new ArrayList<Servant>();

	/**
	 * Start the server. Will initialize the server Connector, so don't initialize it yourself.
	 */
	public ServerModeConnectorDriver(ConnectorInterface serverConnector, ClientHandler clientHandler) throws Exception {
		serverConnector.initialize(null);
		this.serverConnector = serverConnector;
		this.clientHandler = clientHandler;
		this.acceptorThread = new Thread(new Acceptor());
		acceptorThread.start();
	}

	private class Acceptor implements Runnable {
		public void run() {
			while (goOn) {
				ConnectorInterface iteratorConnector;
				try {
					iteratorConnector = serverConnector.getNextClient();
					if (iteratorConnector != null) {
						Servant s = new Servant(iteratorConnector, clientHandler);
						servants.add(s);
					}
				} catch (InterruptedException ignore) {
					break;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void close() {
		goOn = false;
		if (acceptorThread.isAlive()) {
			acceptorThread.interrupt();
		}
		try {
			serverConnector.terminateServer();
		} catch (Exception ignore) {
		}
		try {
			serverConnector.terminate();
		} catch (Exception ignore) {
		}
		try {
			acceptorThread.join();
		} catch (InterruptedException ie) {
		}
		for (Servant s : servants) {
			s.close();
		}
	}

	private static class Servant implements Runnable {

		private final ConnectorInterface iteratorConnector;
		private final ClientHandler clientHandler;
		private final Thread servantThread;

		public Servant(ConnectorInterface iteratorConnector, ClientHandler clientHandler) throws Exception {
			this.iteratorConnector = iteratorConnector;
			this.clientHandler = clientHandler;
			this.servantThread = new Thread(this);
			this.servantThread.start();
		}

		public void run() {
			try {
				clientHandler.handleClient(iteratorConnector);
			} catch (InterruptedException ie) {
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void close() {
			if (servantThread.isAlive()) {
				servantThread.interrupt();
			}
			try {
				iteratorConnector.terminate();
			} catch (Exception ignore) {
			}
			try {
				servantThread.join();
			} catch (InterruptedException ie) {
			}
		}

	}

}
