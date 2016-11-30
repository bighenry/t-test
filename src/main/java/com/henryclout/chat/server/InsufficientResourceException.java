package com.henryclout.chat.server;

public class InsufficientResourceException extends Exception {

	private static final long serialVersionUID = 1L;

	public InsufficientResourceException(String msg) {
		super(msg);
	}
}
