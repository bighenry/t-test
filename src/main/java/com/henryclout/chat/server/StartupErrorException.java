package com.henryclout.chat.server;

public class StartupErrorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StartupErrorException(String msg, Throwable t) {
		super(msg, t);
	}
}
