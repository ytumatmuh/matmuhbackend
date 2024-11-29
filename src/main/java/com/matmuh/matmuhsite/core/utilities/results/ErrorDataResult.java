package com.matmuh.matmuhsite.core.utilities.results;

import org.springframework.http.HttpStatus;

public class ErrorDataResult<T> extends DataResult<T> {

	public ErrorDataResult(T data, String message, HttpStatus httpStatus) {
		super(data, false, message, httpStatus);
		
	}
	
	public ErrorDataResult(T data, HttpStatus httpStatus) {
		super(data, false, httpStatus);
	}
	
	public ErrorDataResult(String message, HttpStatus httpStatus) {
		super(null, false, message, httpStatus);
	}
	
	public ErrorDataResult(HttpStatus httpStatus) {
		super(null, false, httpStatus);
	}

	
}
