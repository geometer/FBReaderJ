package org.geometerplus.android.fbreader.plugin.metainfoservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MetaInfoService extends Service {
	private final MetaInfoReader.Stub binder=new MetaInfoReader.Stub() {
		public String readMetaInfo(String path) {
			return MetaInfoService.this.readMetaInfo(path);
		}
	};
	@Override
	public void onCreate() {
		super.onCreate();
	}
	  
	@Override
	public IBinder onBind(Intent intent) {
		return binder ;
	}
		  
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
		  
	private String readMetaInfo(String path) {
		return "testtesttest";
	}
};

