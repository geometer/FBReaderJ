package org.zlibrary.core.resources;

final class ZLMissingResource extends ZLResource {
	static final String Value = "????????";
	static final ZLMissingResource Instance = new ZLMissingResource();

	private ZLMissingResource() {
		super(Value);
	}

	public ZLResource getResource(String key) {
		return this;
	}

	public boolean hasValue() {
		return false;
	}

	public String getValue() {
		return Value;
	}
}
