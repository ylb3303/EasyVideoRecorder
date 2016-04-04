package org.easydarwin.video.recoder.core;

import java.util.ArrayList;
import java.util.List;

public class EventPoster {
	private static EventPoster eventPoster;

	private List<OnEventListener> listeners = new ArrayList<OnEventListener>();

	private EventPoster() {
		super();
	}

	public static EventPoster getInstance() {
		if (eventPoster == null) {
			eventPoster = new EventPoster();
		}
		return eventPoster;
	}

	public static interface OnEventListener {
		public abstract void onEvent(int type, Object ...msg);
	}

	public void regist(OnEventListener listener) {
		listeners.add(listener);
	}

	public void unregist(OnEventListener listener) {
		listeners.remove(listener);
	}

	public void postEvent(int type, Object... msg) {
		for (OnEventListener onEventListener : listeners) {
			onEventListener.onEvent(type, msg);
		}
	}
}
