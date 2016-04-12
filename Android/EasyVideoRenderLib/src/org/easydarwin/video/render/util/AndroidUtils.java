package org.easydarwin.video.render.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class AndroidUtils {
	public static boolean isProjectProcess(Context cxt) {
		try {
			PackageInfo info = cxt.getPackageManager().getPackageInfo(cxt.getPackageName(), 0);
			String processName = getProcessName(cxt, android.os.Process.myPid());
			if (info.packageName.equals(processName)) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Point getScreenSize(Context mContext) {
		Point size = new Point();
		if (Build.VERSION.SDK_INT > 13) {
			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			display.getSize(size);
		} else if (Build.VERSION.SDK_INT < 13 && Build.VERSION.SDK_INT > 8) {
			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();
			size.set(width, height);
		} else if (Build.VERSION.SDK_INT <= 8) {
			DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
			size.set(dm.widthPixels, dm.heightPixels);
		}
		return size;
	}
}
