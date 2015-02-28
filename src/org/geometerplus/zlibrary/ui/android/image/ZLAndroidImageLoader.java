/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.image;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.os.Handler;
import android.os.Message;

import org.geometerplus.zlibrary.core.image.ZLImageProxy;

class ZLAndroidImageLoader {
	void startImageLoading(final ZLImageProxy.Synchronizer synchronizer, final ZLImageProxy image, Runnable postAction) {
		synchronized (myOnImageSyncRunnables) {
			LinkedList<Runnable> runnables = myOnImageSyncRunnables.get(image.getId());
			if (runnables != null) {
				if (postAction != null && !runnables.contains(postAction)) {
					runnables.add(postAction);
				}
				return;
			}

			runnables = new LinkedList<Runnable>();
			if (postAction != null) {
				runnables.add(postAction);
			}
			myOnImageSyncRunnables.put(image.getId(), runnables);
		}

		final ExecutorService pool =
			image.sourceType() == ZLImageProxy.SourceType.FILE
				? mySinglePool : myPool;
		pool.execute(new Runnable() {
			public void run() {
				synchronizer.synchronize(image, new Runnable() {
					public void run() {
						myImageSynchronizedHandler.fireMessage(image.getId());
					}
				});
			}
		});
	}

	private static class MinPriorityThreadFactory implements ThreadFactory {
		private final ThreadFactory myDefaultThreadFactory = Executors.defaultThreadFactory();

		public Thread newThread(Runnable r) {
			final Thread th = myDefaultThreadFactory.newThread(r);
			th.setPriority(Thread.MIN_PRIORITY);
			return th;
		}
	}

	private static final int IMAGE_LOADING_THREADS_NUMBER = 3; // TODO: how many threads ???

	private final ExecutorService myPool = Executors.newFixedThreadPool(IMAGE_LOADING_THREADS_NUMBER, new MinPriorityThreadFactory());
	private final ExecutorService mySinglePool = Executors.newFixedThreadPool(1, new MinPriorityThreadFactory());

	private final HashMap<String,LinkedList<Runnable>> myOnImageSyncRunnables = new HashMap<String,LinkedList<Runnable>>();

	private class ImageSynchronizedHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			final String imageUrl = (String)message.obj;
			final LinkedList<Runnable> runables;
			synchronized (myOnImageSyncRunnables) {
				runables = myOnImageSyncRunnables.remove(imageUrl);
			}
			for (Runnable runnable : runables) {
				runnable.run();
			}
		}

		public void fireMessage(String imageUrl) {
			sendMessage(obtainMessage(0, imageUrl));
		}
	};

	private final ImageSynchronizedHandler myImageSynchronizedHandler = new ImageSynchronizedHandler();
}
