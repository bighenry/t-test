package com.henryclout.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConnectionAcceptorService {

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
						LOG.debug("Waiting for client connection.");
						Socket clientSocket = serverSocket.accept();
						LOG.debug("Client opened socket connection from: " + clientSocket.getRemoteSocketAddress());

						clientConnectionManager.connectClient(clientSocket);
					} catch (Throwable t) {
						LOG.warn("Failed to establish socket connection.", t);
					}
				}
			} catch (Throwable t) {
				LOG.error("Failed to start server.", t);
				throw new StartupErrorException("Failed to start server.", t);
			}
		});

		acceptorThread.start();
	}
}
