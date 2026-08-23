package com.matmuh.matmuhsite.core.utilities.results;

import com.matmuh.matmuhsite.core.helpers.RequestResourceResolver;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ErrorResult extends Result {

	private final String errorCode;
	private final String resource;
	private final String resourceId;
	private List<ErrorDetail> details = List.of();

	public ErrorResult(String message, HttpStatus httpStatus) {
		this(message, httpStatus, ErrorCodes.UNEXPECTED);
	}

	public ErrorResult(String message, HttpStatus httpStatus, String errorCode) {
		super(false, message, httpStatus);
		this.errorCode = errorCode;

		var target = RequestResourceResolver.resolve();
		this.resource = target.resource();
		this.resourceId = target.id();
	}

	public ErrorResult withDetails(List<ErrorDetail> details) {
		this.details = details == null ? List.of() : List.copyOf(details);
		return this;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getResource() {
		return resource;
	}

	public String getResourceId() {
		return resourceId;
	}

	public List<ErrorDetail> getDetails() {
		return details;
	}
}
