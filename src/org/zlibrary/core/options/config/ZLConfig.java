package org.zlibrary.core.options.config;

public interface ZLConfig extends ZLSimpleConfig {
	public void applyDelta();
	public void clearDelta();
	public ZLDeltaConfig getDelta();
	public void setCategory(String group, String name, String category);
}
