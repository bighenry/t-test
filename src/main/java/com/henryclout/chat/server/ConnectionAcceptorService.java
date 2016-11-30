package com.henryclout.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectionAcceptorService {

	private final static Logger LOG = Logger.getLogger(ConnectionAcceptorService.class.getName());

	// FIXME: make configurable
	private int acceptPort = 8081;

	@Autowired
	private ClientConnectionManager clientConnectionManager;

	@PostConstruct
	public void acceptConnections() throws IOException {
		Thread acceptorThread = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(acceptPort);) {
				LOG.info("Started connecton acceptor on port: " + acceptPort);

				while (true) {
					try {
						LOG.fine("Waiting for client connection.");
						Socket clientSocket = serverSocket.accept();
						LOG.fine("Client opened socket connection from: " + clientSocket.getRemoteSocketAddress());

						clientConnectionManager.connectClient(clientSocket);
					} catch (Throwable t) {
						LOG.log(Level.WARNING, "Failed to establish socket connection.", t);
					}
				}
			} catch (Throwable t) {
				LOG.log(Level.SEVERE, "Failed to start server.", t);
				throw new StartupErrorException("Failed to start server.", t);
			}
		});

		acceptorThread.start();
	}
}
