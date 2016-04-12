package org.easydarwin.video.render.util;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;

public class Closer implements Closeable {

	private final Deque<Closeable> stack = new ArrayDeque<Closeable>(4);

	public <C extends Closeable> C register(C closeable) {
		if (closeable != null) {
			this.stack.addFirst(closeable);
		}
		return closeable;
	}

	@Override
	public void close() {
		while (!(this.stack.isEmpty())) {
			Closeable closeable = this.stack.removeFirst();
			try {
				closeable.close();
			} catch (Throwable e) {
			}
		}
	}

}
