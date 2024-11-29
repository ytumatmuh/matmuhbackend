package com.matmuh.matmuhsite.core.utilities.results;

import org.springframework.http.HttpStatus;

public class DataResult<T> extends Result {
	
	private T data;
	
	public T getData() {
		return data;
	}

	public DataResult(T data, boolean success, String message, HttpStatus httpStatus) {
		super(success, message, httpStatus);
		this.data = data;
		
	}
	
	public DataResult(T data, boolean success, HttpStatus httpStatus) {
		super(success, httpStatus);
		this.data = data;
	}

}
