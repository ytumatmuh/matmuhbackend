package com.matmuh.matmuhsite.core.utilities.results;

import org.springframework.http.HttpStatus;

public class Result {
	private boolean success;
	private String message;
	private HttpStatus httpStatus;
	
	public boolean isSuccess() {
		return success;
	}
	
	public String getMessage() {
		return message;
	}

	public HttpStatus getHttpStatus(){
		return httpStatus;
	}
	
	public Result(boolean success, String message, HttpStatus httpStatus) {
		this.success = success;
		this.message = message;
		this.httpStatus = httpStatus;
	}
	
	public Result(boolean success, HttpStatus httpStatus) {
		this.success = success;
		this.httpStatus = httpStatus;
	}
}
