package com.henryclout.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientConnectionManager {

	private final static Logger LOG = Logger.getLogger(ClientConnectionManager.class.getName());
	
	@Autowired
	private MessageBroadcaster messageBroadcaster;

	private ExecutorService executor;

	// FIXME: make configurable
	private int activeConnectionLimit = 100;
	private int activeConnectionCount = 0;

	public synchronized int getActiveConnectionCount() {
		return activeConnectionCount;
	}

	@PostConstruct
	public void init() {
		executor = Executors.newFixedThreadPool(activeConnectionLimit);
	}

	public synchronized void connectClient(Socket clientSocket) throws IOException, InsufficientResourceException {
		if (activeConnectionCount < activeConnectionLimit) {
			LOG.info("Connection client.  New active connection count is: " + activeConnectionCount);
			
			// Add the writer to broadcast messages from this client.
			PrintWriter clientPrintWriter = new PrintWriter(clientSocket.getOutputStream());
			messageBroadcaster.addBroadcastWriter(clientPrintWriter);

			// Add a new thread to receive input from the client.
			Scanner scanner = new Scanner(clientSocket.getInputStream());
			executor.execute(() -> {
				try {
					while (scanner.hasNext()) {
						String message = scanner.nextLine();
						LOG.info("Received message: " + message);
						messageBroadcaster.broadcastMessage(message, clientPrintWriter);
						LOG.info("Waiting for next message: " + message);

					}
					LOG.info("Client disconnected normally.");
				} catch (Throwable t) {
					LOG.log(Level.WARNING, "Client disconnected.", t);
				} finally {
					try {
						scanner.close();
					} catch (Throwable t) {
						LOG.log(Level.WARNING, "Failed to close socket reader.", t);
					}
				}

				// Tidy up.
				updateStateForDisconnectedClient(clientPrintWriter);

			});
			activeConnectionCount++;
		} else {
			LOG.info("Client connection refused.  We've reached the capacity limit of:  client.  New active connection count is: " + activeConnectionCount);

			throw new InsufficientResourceException("Server is at connenction capactiy limit of: " + activeConnectionLimit + " connections.");
		}
	}

	private synchronized void updateStateForDisconnectedClient(PrintWriter clientPrintWriter) {
		messageBroadcaster.removeBroadcastWriter(clientPrintWriter);
		activeConnectionCount--;
		LOG.info("Disconnecting client.  New active connection count is: " + activeConnectionCount);

	}
}
