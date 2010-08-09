package com.github.gikolipse.exceptions;

public class GikolipseException extends RuntimeException {

	private static final long serialVersionUID = 7157542916676011015L;

	public GikolipseException() {
		super();
	}

	public GikolipseException(Throwable t) {
		super(t);
	}

	public GikolipseException(String m) {
		super(m);
	}

	public GikolipseException(String s, Throwable t) {
		super(s, t);
	}
}
