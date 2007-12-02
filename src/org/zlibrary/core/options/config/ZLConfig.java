package org.zlibrary.core.options.config;

import java.util.Set;

public interface ZLConfig extends ZLSimpleConfig {
	public void applyDelta();
	public void clearDelta();
	public ZLDeltaConfig getDelta();
	public Set<String> getCategories();
	public void setCategory(String group, String name, String category);
}
