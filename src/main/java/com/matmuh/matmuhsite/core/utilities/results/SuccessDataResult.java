package com.matmuh.matmuhsite.core.utilities.results;

import org.springframework.http.HttpStatus;

public class SuccessDataResult<T> extends DataResult<T> {

	public SuccessDataResult(T data, String message, HttpStatus httpStatus) {
		super(data, true, message, httpStatus);
	}
	
	public SuccessDataResult(T data, HttpStatus httpStatus) {
		super(data, true, httpStatus);
	}
	
	public SuccessDataResult(String message, HttpStatus httpStatus ) {
		super(null, true, message, httpStatus);
	}
	
	public SuccessDataResult(HttpStatus httpStatus) {
		super(null, true,httpStatus);
	}

}
