package org.easydarwin.video.render.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.easydarwin.video.common.LoadCallbackListener;
import org.easydarwin.video.common.SimpleListener;
import org.easydarwin.video.render.model.RenderDisplyer;
import org.easydarwin.video.render.util.Closer;
import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Xml;

public class RenderResHelper {
	public static final String RES_THEME = "theme";
	public static final String RES_FILTER = "filter";
	public static final String RES_FRAME = "frame";
	public static final String RES_MUSIC = "music";
	public static final String RES_COMMON = "common";
	public static final int RENDER_ACTION_MORE = 1;

	public static Context context;
	private static RenderResHelper renderResHelper;
	private static final String RES_COMMON_END_LOGO = "end_logo.png";
	private Handler handler = new Handler(Looper.getMainLooper());

	public static RenderResHelper getInstance() {
		if (renderResHelper == null) {
			renderResHelper = new RenderResHelper();
		}
		return renderResHelper;
	}

	private RenderResHelper() {
		super();
	}

	public void initWithContext(final Context context) {
		RenderResHelper.context = context;
		init();
	}

	public void init() {
		File base = context.getExternalFilesDir(null);
		newCopyAssetsResTask(context, RES_THEME + "/", base.getAbsolutePath());
		newCopyAssetsResTask(context, RES_FILTER + "/", base.getAbsolutePath());
		newCopyAssetsResTask(context, RES_COMMON + "/", base.getAbsolutePath());
		newCopyAssetsResTask(context, RES_FRAME + "/", base.getAbsolutePath());
		newCopyAssetsResTask(context, RES_MUSIC + "/", base.getAbsolutePath());
	}

	public String getResLocation(String type) {
		File root = context.getExternalFilesDir(null);
		return new File(root, type).getAbsolutePath();
	}

	public String getCommonEndLogo() {
		File root = context.getExternalFilesDir(null);
		return new File(new File(root, RES_COMMON), RES_COMMON_END_LOGO).getAbsolutePath();
	}

	public String getResLocation(String type, String id) {
		File root = context.getExternalFilesDir(null);
		return new File(new File(root, type), id).getAbsolutePath();
	}

	public void newCopyAssetsResTask(final Context context, final String folder, final String target) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String assetsVer = readAssetsVer(context, folder);
				String lastVer = readVerTag(new File(target, folder));
				if (assetsVer == null) {
					throw new RuntimeException("error ver");
				}
				if (lastVer == null || assetsVer.compareTo(lastVer) < 0) {
					long start = System.currentTimeMillis();
					copyFolderFromAssets(context, folder, target);
					realseResZip(new File(target, folder).getAbsolutePath());
					createVerTag(new File(target, folder), assetsVer);
					System.out.println("done! time use=" + (System.currentTimeMillis() - start));
				} else {
					System.out.println("same ver :" + assetsVer);
				}
			}
		}).start();
	}

	public void newCopyResTask(final String source, final String target, final String folder, final String ver) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String lastVer = readVerTag(new File(target, folder));
				if (lastVer == null || lastVer.compareTo(ver) < 0) {
					long start = System.currentTimeMillis();
					copyFolder(new File(source, folder).getAbsolutePath(), target);
					realseResZip(new File(target, folder).getAbsolutePath());
					createVerTag(new File(target, folder), ver);
					System.out.println("done! time use=" + (System.currentTimeMillis() - start));
				} else {
					System.out.println("same ver :" + ver);
				}
			}
		}).start();
	}

	public void newAddResTask(final String resZipFile, final String type, SimpleListener listener) {
		File root = context.getExternalFilesDir(null);
		newAddResTask(new File(resZipFile), new File(root, type), listener);
	}

	public void newAddResTask(final File resZipFile, final File targetDir, final SimpleListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				unZip(resZipFile, targetDir, false);
				if (listener != null) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							listener.onResult(null);
						}
					});
				}
			}
		}).start();
	}

	public static void createVerTag(File folder, String ver) {
		try {
			File file = new File(folder, ".meta");
			file.createNewFile();
			FileWriter writer = new FileWriter(file);
			writer.write(ver);
			writer.flush();
			writer.close();
		} catch (Exception e) {
		}
	}

	public static String readAssetsVer(Context context, String folder) {
		AssetManager assetManager = context.getAssets();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(folder + "ver.txt")));
			String ver = reader.readLine();
			reader.close();
			return ver;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readVerTag(File folder) {
		try {
			File file = new File(folder, ".meta");
			if (!file.exists()) {
				return null;
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String ver = reader.readLine();
			reader.close();
			return ver;
		} catch (Exception e) {
		}
		return null;
	}

	public static void realseResZip(final String folder) {
		File folderFile = new File(folder);
		if (!folderFile.exists() || !folderFile.isDirectory()) {
			return;
		}
		String[] zips = folderFile.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		});
		if (zips == null) {
			return;
		}
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < zips.length; i++) {
			final String z = zips[i];
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					unZip(new File(folder, z), null, true);
				}
			});
			thread.start();
			threads.add(thread);
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public static void copyFolderFromAssets(Context context, String assetSrc, String desDir) {
		AssetManager assetManager = context.getAssets();
		try {
			if (TextUtils.isEmpty(assetSrc)) {
				return;
			}
			if (!desDir.endsWith("/")) {
				desDir += "/";
			}
			if (TextUtils.isEmpty(assetSrc) || assetSrc.equals("/")) {
				assetSrc = "";
			} else if (assetSrc.endsWith("/")) {
				assetSrc = assetSrc.substring(0, assetSrc.length() - 1);
			}
			String assets[] = assetManager.list(assetSrc);
			if (assets.length > 0) {
				for (String name : assets) {
					if (!TextUtils.isEmpty(name)) {
						name = assetSrc + "/" + name;
					}
					String[] childNames = assetManager.list(name);
					if (!TextUtils.isEmpty(name) && childNames.length > 0) {
						copyFolderFromAssets(context, name, desDir);
					} else {
						copyFileFromAssets(context, name, desDir);
					}
				}
			} else {
				copyFileFromAssets(context, assetSrc, desDir);
			}
		} catch (Exception ex) {
		}
	}

	public static void copyFileFromAssets(final Context context, final String filename, final String desDir) {
		AssetManager assetManager = context.getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(filename);
			String newFileName = desDir + filename;
			File file = new File(newFileName);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
			}
			out = new FileOutputStream(newFileName);
			byte[] buffer = new byte[1024 * 2];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (Exception e) {
		}
		return;
	}

	public static void copyFolder(String oldPath, String newPath) {
		try {
			new File(newPath).mkdirs();
			File dir = new File(oldPath);
			newPath += File.separator + dir.getName();
			File moveDir = new File(newPath);
			if (dir.isDirectory()) {
				if (!moveDir.exists()) {
					moveDir.mkdirs();
				}
			}
			String[] file = dir.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}
				if (temp.isFile()) {
					copyFile(temp, new File(newPath + File.separator + temp.getName()));
				}
				if (temp.isDirectory()) {
					copyFolder(oldPath + File.separator + file[i], newPath);
				}
			}
		} catch (Exception e) {
		}
	}

	public static void copyFile(File source, File target) {
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		Closer closer = new Closer();
		try {
			inStream = closer.register(new FileInputStream(source));
			outStream = closer.register(new FileOutputStream(target));
			in = closer.register(inStream.getChannel());
			out = closer.register(outStream.getChannel());
			in.transferTo(0, in.size(), out);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closer.close();
		}
	}

	public static void unZip(File zipfile, File targetDir, boolean delete) {
		FileOutputStream fileOut;
		if (targetDir == null) {
			targetDir = zipfile.getParentFile();
		}
		byte[] buffer = new byte[1024];
		ZipInputStream zipIn = null;
		BufferedInputStream bin = null;
		try {
			zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipfile)));
			bin = new BufferedInputStream(zipIn);
			ZipEntry zipEntry = null;
			String curFileName;
			while ((zipEntry = zipIn.getNextEntry()) != null) {
				curFileName = zipEntry.getName();
				if (zipEntry.isDirectory()) {
					new File(targetDir, zipEntry.getName()).mkdirs();
				} else {
					File file = new File(targetDir, curFileName);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					fileOut = new FileOutputStream(file);
					int b = 0;
					while ((b = bin.read(buffer)) > 0) {
						fileOut.write(buffer, 0, b);
					}
					fileOut.close();
					zipIn.closeEntry();
				}
			}
			zipIn.close();
			if (delete) {
				zipfile.delete();
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	public static interface LoadRenderDisplyerListener {
	}

	public void loadRenderDisplyer(final String type, final LoadCallbackListener<List<RenderDisplyer>> listener) {
		loadRenderDisplyer(type, false, listener);
	}

	public void loadRenderDisplyer(final String type, final boolean loadActionMore, final LoadCallbackListener<List<RenderDisplyer>> listener) {
		if (listener == null) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<RenderDisplyer> list = new ArrayList<RenderDisplyer>();
				File resFolder = new File(context.getExternalFilesDir(null), type);
				int count = 0;
				while (!(resFolder.exists() && readVerTag(resFolder) != null)) {// not ready
					count++;
					SystemClock.sleep(10);
					if (count == (1000 * 3) / 10) {
						break;
					}
				}
				if (!(resFolder.exists() && readVerTag(resFolder) != null)) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							listener.onCallback(null);
						}
					});
				}
				String[] displayers = resFolder.list(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						return new File(new File(dir, filename), "config.xml").exists();
					}
				});
				if (displayers != null) {
					for (String folder : displayers) {
						RenderDisplyer displayer = parseRenderDisplyer(type, new File(new File(resFolder, folder), "config.xml"));
						if (displayer != null) {
							displayer.setId(folder);
							displayer.setIcon(new File(new File(resFolder, folder), "icon.png").getAbsolutePath());
							displayer.setLocation(new File(resFolder, folder).getAbsolutePath());
							if (!displayer.isEnable()) {
								break;
							}
							if (displayer.getAction() == RENDER_ACTION_MORE) {
								if (loadActionMore) {
									list.add(displayer);
								}
							} else {
								list.add(displayer);
							}
						}
					}
				}
				Collections.sort(list, new Comparator<RenderDisplyer>() {

					@Override
					public int compare(RenderDisplyer lhs, RenderDisplyer rhs) {
						return lhs.getOrder() - rhs.getOrder();
					}
				});
				handler.post(new Runnable() {
					@Override
					public void run() {
						listener.onCallback(list);
					}
				});
			}
		}).start();

	}

	public static RenderDisplyer parseRenderDisplyer(String type, File xmlConfig) {
		try {
			XmlPullParser parser = Xml.newPullParser();
			RenderDisplyer renderDisplyer = null;
			parser.setInput(new FileInputStream(xmlConfig), "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						renderDisplyer = new RenderDisplyer().setType(type);
						break;
					case XmlPullParser.START_TAG:
						if (parser.getName().equals("item")) {
						} else if (parser.getName().equals("order")) {
							eventType = parser.next();
							renderDisplyer.setOrder(Integer.valueOf(parser.getText()));
						} else if (parser.getName().equals("name")) {
							eventType = parser.next();
							renderDisplyer.setName(parser.getText());
						} else if (parser.getName().equals("enable")) {
							eventType = parser.next();
							renderDisplyer.setEnable(parser.getText().equals("1"));
						} else if (parser.getName().equals("action")) {
							eventType = parser.next();
							renderDisplyer.setAction(Integer.valueOf(parser.getText()));
						}
						break;
					case XmlPullParser.END_TAG:
						if (parser.getName().equals("item")) {
						}
						break;
				}
				eventType = parser.next();
			}
			return renderDisplyer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isValidRenderRes(String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}
		if (!url.endsWith(".zip")) {
			return false;
		}
		if (!new File(url).exists()) {
			return false;
		}
		return true;
	}
}
