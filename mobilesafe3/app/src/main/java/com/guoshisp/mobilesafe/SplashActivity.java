package com.guoshisp.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.domain.UpdateInfo;
import com.guoshisp.mobilesafe.engine.UpdateInfoParser;
import com.guoshisp.mobilesafe.utils.AssetCopyUtil;
import com.guoshisp.mobilesafe.utils.DownLoadUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SplashActivity extends Activity {
	private TextView tv_splash_version;
	private UpdateInfo info;

	private static final int GET_INFO_SUCCESS = 10;
	private static final int SERVER_ERROR = 11;
	private static final int SERVER_URL_ERROR = 12;
	private static final int PROTOCOL_ERROR = 13;
	private static final int IO_ERROR = 14;
	private static final int XML_PARSE_ERROR = 15;
	private static final int DOWNLOAD_SUCCESS = 16;
	private static final int DOWNLOAD_ERROR = 17;
	protected static final String TAG = "SplashActivity";
	private long startTime;
	private RelativeLayout rl_splash;
	private long endTime;
	private ProgressDialog pd;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case XML_PARSE_ERROR:
					Toast.makeText(getApplicationContext(), "xml解析错误", 1).show();
					loadMainUI();
					break;
				case IO_ERROR:
					Toast.makeText(getApplicationContext(), "I/O错误", 1).show();
					loadMainUI();
					break;
				case PROTOCOL_ERROR:
					Toast.makeText(getApplicationContext(), "协议不支持", 1).show();
					loadMainUI();
					break;
				case SERVER_URL_ERROR:
					Toast.makeText(getApplicationContext(), "服务器路径不正确", 1).show();
					loadMainUI();
					break;
				case SERVER_ERROR:
					Toast.makeText(getApplicationContext(), "服务器内部异常", 1).show();
					loadMainUI();
					break;
				case GET_INFO_SUCCESS:
					String serverversion = info.getVersion();
					String currentversion = getVersion();
					if (currentversion.equals(serverversion)) {
						Log.i(TAG, "版本号相同进入主界面");
						loadMainUI();
					} else {
						Log.i(TAG, "版本号不相同,升级对话框");
						showUpdateDialog();
					}
					break;

				case DOWNLOAD_SUCCESS:
					Log.i(TAG, "文件下载成功");
					File file = (File) msg.obj;
					installApk(file);
					break;
				case DOWNLOAD_ERROR:
					Toast.makeText(getApplicationContext(), "下载数据异常", 1).show();
					loadMainUI();
					break;
			}
		};
	};

	/**
	 * 加载主界面
	 */
	private void loadMainUI() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();// 把当前的Activity从任务栈里面移除
	}

	/**
	 * 安装一个apk文件
	 *
	 * @param file
	 *            要安装的完整文件名
	 */
	protected void installApk(File file) {
		// 隐式意图
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");// 设置意图的动作
		intent.addCategory("android.intent.category.DEFAULT");// 为意图添加额外的数据
		// intent.setType("application/vnd.android.package-archive");
		// intent.setData(Uri.fromFile(file));
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");// 设置意图的数据与类型
		startActivity(intent);// 激活该意图
	}

	/**
	 * 显示升级提示的对话框
	 */
	protected void showUpdateDialog() {
		// 创建了对话框的构造器
		Builder builder = new Builder(this);
		// 设置对话框的提示内容
		builder.setIcon(getResources().getDrawable(R.drawable.notification));
		// 设置升级标题
		builder.setTitle("升级提示");
		// 设置升级提示内容
		builder.setMessage(info.getDescription());
		// 创建下载进度条
		pd = new ProgressDialog(SplashActivity.this);
		// 设置进度条在显示时的提示消息
		pd.setMessage("正在下载");
		// 指定显示下载进度条为水平形状
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 设置升级按钮
		builder.setPositiveButton("升级", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "升级,下载" + info.getApkurl());
				// 判断Sdcard是否可用
				if (Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					pd.show();// 显示下载进度条
					// 开启子线程下载apk
					new Thread() {

						public void run() {
							// 获取服务端新版本apk的下载地址
							String path = info.getApkurl();
							// 获取最新apk的文件名
							String filename = DownLoadUtil.getFilename(path);
							// 在Sdcard的根目录上创建一个文件。第一个参数是用于获取Sdcard的根目录，第二个参数是在Sdcard的根目录上创建的文件的文件名
							File file = new File(Environment
									.getExternalStorageDirectory(), filename);
							// 得到下载后的apk的完整名称
							file = DownLoadUtil.getFile(path,
									file.getAbsolutePath(), pd);
							if (file != null) {
								// 向主线程发送消息下载成功的消息
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_SUCCESS;
								msg.obj = file;
								handler.sendMessage(msg);
							} else {
								// 向主线程发送消息下载失败的消息
								Message msg = Message.obtain();
								msg.what = DOWNLOAD_ERROR;
								handler.sendMessage(msg);
							}
							pd.dismiss();// 下载结束后，将下载的进度条关闭掉
						};
					}.start();
				} else {
					Toast.makeText(getApplicationContext(), "sd卡不可用", 1).show();
					loadMainUI();// 进入程序主界面
				}
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				loadMainUI();
			}
		});
		builder.create().show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置为无标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置为全屏模式
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_splash);
		rl_splash = (RelativeLayout) findViewById(R.id.rl_splash);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号:" + getVersion());

		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(2000);
		rl_splash.startAnimation(aa);

		// 1.连接服务器获取服务器上的配置信息.
		new Thread(new CheckVersionTask()) {
		}.start();
		//拷贝病毒库的数据库文件
		new Thread() {
			public void run() {
				File file = new File(getFilesDir(), "antivirus.db");
				if (file.exists() && file.length() > 0) {//数据库文件已经拷贝成功

				} else {
					AssetCopyUtil.copy1(getApplicationContext(),
							"antivirus.db", file.getAbsolutePath(), null);
				}
			};

		}.start();
	}

	/**
	 * 连网检查应用的版本号与服务端上的版本号是否相同
	 *
	 * @author Administrator
	 *
	 */
	private class CheckVersionTask implements Runnable {

		public void run() {
			// 获取Sdcard下的config.xml文件，如果该文件不存在，那么将会自动创建该文件
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			// 由sp对象来获取autoupdate所对应的boolean值，如果该键不存在，默认返回true
			boolean autoupdate = sp.getBoolean("autoupdate", true);
			// 自动更新没有开启
			if (!autoupdate) {
				try {
					// 睡眠2秒钟的是为了播放动画
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 睡眠2秒钟播放动画完毕后进入程序主界面
				loadMainUI();
			}
			startTime = System.currentTimeMillis();
			Message msg = Message.obtain();
			try {
				// 获取服务端的配置信息的连接地址
				String serverurl = getResources().getString(R.string.serverurl);
				URL url = new URL(serverurl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");// 设置请求方式
				conn.setConnectTimeout(5000);
				int code = conn.getResponseCode();// 获取响应码
				if (code == 200) {// 响应码为200时，表示与服务端连接成功
					InputStream is = conn.getInputStream();
					info = UpdateInfoParser.getUpdateInfo(is);
					endTime = System.currentTimeMillis();
					long resulttime = endTime - startTime;
					if (resulttime < 2000) {
						try {
							Thread.sleep(2000 - resulttime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					msg.what = GET_INFO_SUCCESS;
					handler.sendMessage(msg);
				} else {
					// 服务器状态错误.
					msg.what = SERVER_ERROR;
					handler.sendMessage(msg);
					endTime = System.currentTimeMillis();
					long resulttime = endTime - startTime;
					if (resulttime < 2000) {
						try {
							Thread.sleep(2000 - resulttime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
				msg.what = SERVER_URL_ERROR;
				handler.sendMessage(msg);
			} catch (ProtocolException e) {
				msg.what = PROTOCOL_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			} catch (IOException e) {
				msg.what = IO_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				msg.what = XML_PARSE_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取当前应用程序的版本号。 版本号存在于我们的APK中对应的清单文件中（直接解压APK后，即可看到对应的清单文件），
	 * 版本号是manifest节点中的android:versionName="1.0" 当一个应用程序被装到手机后
	 * ，该apk拷贝到手机的data/app目录下（也就是系统中），如图6。所以想得到版本号，我们需要拿到与系统相关的服务，就可以得到apk中的信息了
	 *
	 * @return
	 */
	private String getVersion() {
		// 得到系统的包管理器。已经得到了apk的面向对象的包装
		PackageManager pm = this.getPackageManager();
		try {
			// 参数一：当前应用程序的包名 参数二：可选的附加消息，这里我们用不到 ，可以定义为0
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			// 返回当前应用程序的版本号
			return info.versionName;
		} catch (Exception e) {// 包名未找到的异常，理论上， 该异常不可能会发生
			e.printStackTrace();
			return "";
		}
	}
}