package com.matmuh.matmuhsite.core.utilities.results;

import org.springframework.http.HttpStatus;

public class ErrorResult extends Result {

	public ErrorResult(String message, HttpStatus httpStatus) {
		super(false, message, httpStatus);
		
	}
	
	public ErrorResult(HttpStatus httpStatus) {
		super(false, httpStatus);
	}

}
