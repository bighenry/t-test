package com.henryclout.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClientConnectionManager {

	@Autowired
	private MessageBroadcaster messageBroadcaster;

	private ExecutorService executor;

	// FIXME: make configurable
	private int activeConnectionLimit = 100;
	private int activeConnectionCount = 0;

	@PostConstruct
	public void init() {
		executor = Executors.newFixedThreadPool(activeConnectionLimit);
	}

	public synchronized void connectClient(Socket clientSocket) throws IOException, InsufficientResourceException {
		if (activeConnectionCount < activeConnectionLimit) {
			activeConnectionCount++;

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
						messageBroadcaster.broadcastMessage(message, clientPrintWriter);
					}
					LOG.info("Client disconnected normally.");
				} catch (Throwable t) {
					LOG.warn("Client disconnected.", t);
				} finally {
					try {
						scanner.close();
					} catch (Throwable t) {
						LOG.warn("Failed to close socket reader.", t);
					}
				}

				// Tidy up.
				updateStateForDisconnectedClient(clientPrintWriter);

			});
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
