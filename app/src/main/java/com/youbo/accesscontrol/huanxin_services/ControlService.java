package com.youbo.accesscontrol.huanxin_services;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;


import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import org.json.JSONObject;
import java.net.URLDecoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 环信的聊天服务 Created by lx on 2017/3/2.
 */
public class ControlService extends Service {

	public static final String TAG = "MyAccessControl";
	public static final String PACKAGENAME = "com.yb.accesscontrol";
	// ca1f355f6abb
	private String username = "";// "9bc08c829278";
	private String password = "11111111";
	private Context context;
	private SharedPreferences sharedPreferences;
	private SharedPreferences.Editor editor;
	private boolean cntStatus;
	public ExecutorService ESUPdata;// 数据处理的线程池

	@Override
	public int onStartCommand(Intent intent, int i, int i1) {

		return super.onStartCommand(intent, i, i1);
		
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context=this;
		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		WakeLock newWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK , "ControlService");
		newWakeLock.acquire();



		Log.e(TAG, "onCreate.........服务创建成功");

		new CheckNetworkThread(getApplicationContext()).start();
		cntStatus = true;

		initCreate();

		Message msg13 = new Message();
		msg13.obj = 13;
		myHandler.sendMessage(msg13);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		//Toast.makeText(context, "服务销毁成功", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		//Toast.makeText(context, "服务开始了", Toast.LENGTH_SHORT).show();
		
	}

	private void initCreate() {
		// 初始化版本号
		ESUPdata = Executors.newFixedThreadPool(10);
		sharedPreferences = getSharedPreferences("main", Activity.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		String versionName = getVersionName(PACKAGENAME);
		Log.e(TAG, versionName);
		editor.putString("versionName", versionName);
		editor.commit();


		EMClient.getInstance().chatManager().addMessageListener(msgListener);
		EMClient.getInstance().addConnectionListener(new MyConnectionListener());
		EMClient.getInstance().callManager()
				.addCallStateChangeListener(new EMCallStateChangeListener() {
					@Override
					public void onCallStateChanged(CallState callState,
							CallError error) {
						switch (callState) {
						case CONNECTING: // 正在连接对方

							break;
						case CONNECTED: // 双方已经建立连接

							

							break;

						case ACCEPTED: // 电话接通成功
							

							break;
						case DISCONNECTED: // 电话断了
							

							break;
						case NETWORK_UNSTABLE: // 网络不稳定

							break;
						case NETWORK_NORMAL: // 网络恢复正常
							break;
						default:
							break;
						}
					}
				});
	}


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 检查网络登录的线程
	 */
	private class CheckNetworkThread extends Thread {
		private Context context;

		public CheckNetworkThread(Context context) {
			this.context = context;
		}
		
		@Override
		public void run() {
			while (cntStatus) {
				try {
					// L.e(TAG,".线程开始了");
					// 判断是否有网络
					Thread.sleep(5000);
					 boolean isNet = isNetworkConnected(context);
					boolean connected=EMClient.getInstance().isConnected();;



					if (isNet&&!connected) {
						Log.e(TAG, "有网络");
						// 读取本地账号

						if (username.trim().equalsIgnoreCase("")
								|| username == null) {
							// 登入环信
							// 没有账号和密码
							username = "test";
							LoginHX(username);
							Log.e(TAG, "没有获取到环信账号");
						} else {
						
							LoginHX(username);
						}
						Message msg = new Message();
						msg.obj = 200;
						myHandler.sendMessage(msg);
						break;
					} else {
						Log.e(TAG, "没有网络");
					}
					Thread.sleep(5000);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	private void LoginHX(final String username) {
		// 登录
		EMClient.getInstance().login(username, "11111111", new EMCallBack() {// 回调
					@Override
					public void onSuccess() {
						EMClient.getInstance().groupManager().loadAllGroups();
						EMClient.getInstance().chatManager()
								.loadAllConversations();
						Log.e("main", "登陆聊天服务器成功！");

					}

					@Override
					public void onProgress(int progress, String status) {
							
					}

					@Override
					public void onError(int code, String message) {
						Log.e("main", "登陆聊天服务器失败！");
					}
				});
	}

	/**
	 * 有网络就关闭检查网络登录线程
	 */
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// cntStatus = (boolean) msg.obj;
			int msgobj = (Integer) msg.obj;
			switch (msgobj) {
			case 200:
				cntStatus = false;
				if (!cntStatus) {

				}
				break;
			case 201:

				break;
			case 6:
				break;
			case 9:
				// //Toast.makeText(context, "Success--09",
				// Toast.LENGTH_SHORT).show();
				break;
			default:

				break;
			}
		}
	};

	/**
	 * use:判断是否有网络
	 *
	 * @param context
	 *            上下文
	 * @return:是否有网络
	 */
	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	private String getVersionName(String packageName) {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		// PackageInfo packInfo =
		// packageManager.getPackageInfo(getPackageName(), 0);
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(packageName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packInfo == null) {
			return "";
		} else {
			return packInfo.versionName;
		}
	}
	
	EMMessageListener msgListener = new EMMessageListener() {
		@Override
		public void onMessageReceived(List<EMMessage> msg) {
			// 收到消息
			// L.e(TAG,"接到消息" + msg.get(0).getBody().toString());

			if ("TXT".equals(msg.get(0).getType().toString())) {
				String from = msg.get(0).getFrom();
				String str = msg.get(0).getBody().toString();
				
				String mess = str.substring(5, str.length() - 1);
				Log.e(TAG,"接收到："+mess);
				int option;
				String optionid = "";
				try {
					mess = URLDecoder.decode(mess, "UTF-8");
					Log.e(TAG, "发送过来的信息--->" + mess);
					JSONObject jsonObject = new JSONObject(mess);
					option = jsonObject.getInt("option");
					// L.e(TAG,"root.option--->" + option);
					optionid = jsonObject.getString("optionid");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onCmdMessageReceived(List<EMMessage> messages) {
			// 收到透传消息
		}

		@Override
		public void onMessageReadAckReceived(List<EMMessage> messages) {
			// 收到已读回执
		}

		@Override
		public void onMessageDeliveryAckReceived(List<EMMessage> message) {
			// 收到已送达回执
		}

		@Override
		public void onMessageChanged(EMMessage message, Object change) {
			// 消息状态变动
		}
	};
	
	
	private class MyConnectionListener implements EMConnectionListener {
	    @Override
	    public void onConnected() {

	    }
	    @Override
	    public void onDisconnected(final int error) {
	    	
	    	
	    }
	}
}
