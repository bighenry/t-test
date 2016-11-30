package com.henryclout.chat;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MessageBroadcaster {

	private Set<PrintWriter> broadcastWriters = new HashSet<>();
	
	public synchronized void addBroadcastWriter(PrintWriter printWriter) {
		broadcastWriters.add(printWriter);
	}
	
	public synchronized void removeBroadcastWriter(PrintWriter printWriter) {
		broadcastWriters.remove(printWriter);
	}
	
	public void broadcastMessage(String message, PrintWriter messageSourceClientPrintWriter) {
		LOG.debug("Broadcasting message: " + message);
		broadcastWriters.parallelStream().forEach((broadcastWriter) -> {
			if (broadcastWriter != messageSourceClientPrintWriter) {
				// Only broadcast to 'other' clients. 
				broadcastWriter.println(message);
				broadcastWriter.flush();
			}
		}); 
	}
	
}
