package org.zlibrary.core.resources;

final class ZLMissingResource extends ZLResource {
	static final String ourValue = "????????";
	private static ZLMissingResource ourInstance;

	private ZLMissingResource() {
		super(ourValue);
	}

	public ZLResource getResource(String key) {
		return this;
	}

	public boolean hasValue() {
		return false;
	}

	public String getValue() {
		return ourValue;
	}
	
	public static ZLMissingResource instance() {
		if (ourInstance == null) {
			ourInstance = new ZLMissingResource();
		}
		return ourInstance;
	}
}
