package com.henryclout.chat.server;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

@Component
public class MessageBroadcaster {

	private final static Logger LOG = Logger.getLogger(MessageBroadcaster.class.getName());

	private Set<PrintWriter> broadcastWriters = new HashSet<>();
	
	public synchronized void addBroadcastWriter(PrintWriter printWriter) {
		broadcastWriters.add(printWriter);
	}
	
	public synchronized void removeBroadcastWriter(PrintWriter printWriter) {
		broadcastWriters.remove(printWriter);
	}
	
	public void broadcastMessage(String message, PrintWriter messageSourceClientPrintWriter) {
		LOG.fine("Broadcasting message: " + message);
		broadcastWriters.parallelStream().forEach((broadcastWriter) -> {
			if (broadcastWriter != messageSourceClientPrintWriter) {
				// Only broadcast to 'other' clients. 
				broadcastWriter.println(message);
				broadcastWriter.flush();
			}
		}); 
	}
	
}
