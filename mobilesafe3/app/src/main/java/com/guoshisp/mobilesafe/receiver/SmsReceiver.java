package com.guoshisp.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.guoshisp.mobilesafe.R;
import com.guoshisp.mobilesafe.db.dao.BlackNumberDao;
import com.guoshisp.mobilesafe.engine.GPSInfoProvider;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";
	private SharedPreferences sp;
	private BlackNumberDao dao;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"短信到来了");
		dao = new BlackNumberDao(context);
		sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		String safenumber = sp.getString("safemuber", "");
		//获取短信中的内容。系统接收到一个信息广播时，会将接收到的信息存放到pdus数组中
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		//获取手机设备管理器
		DevicePolicyManager dm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//创建一个与MyAdmin相关联的组件
		ComponentName mAdminName = new ComponentName(context, MyAdmin.class);
		//遍历出信息中的所有内容
		for(Object obj : objs){
			SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj);
			//获取发件人的号码
			String sender = smsMessage.getOriginatingAddress();
			//判断短信号码是否是黑名单号码&短信拦截
			int result = dao.findNumberMode(sender);
			if(result==1||result==2){//判断该黑名单号码是否需要拦截短信
				Log.i(TAG,"拦截黑名单短信");
				abortBroadcast();
			}
			//获取短信信息内容
			String body = smsMessage.getMessageBody();

			if("#*location*#".equals(body)){
				Log.i(TAG,"发送位置信息");
				//获取上次的位置
				String lastlocation = GPSInfoProvider.getInstance(context).getLocation();
				if(!TextUtils.isEmpty(lastlocation)){
					//得到信息管理器
					SmsManager smsManager = SmsManager.getDefault();
					//向安全号码发送当前的位置信息
					smsManager.sendTextMessage(safenumber, null, lastlocation, null, null);
				}
				abortBroadcast();
			}else if("#*alarm*#".equals(body)){
				Log.i(TAG,"播放报警音乐");
				//得到音频播放器
				MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);//res\raw\ylzs.mp3
				//即使手机是静音模式也有音乐的声音
				player.setVolume(1.0f, 1.0f);
				//开始播放音乐
				player.start();
				//终止掉发送过来的信息，在本地查看不到该信息
				abortBroadcast();
			}else if("#*wipedata*#".equals(body)){
				Log.i(TAG,"清除数据");
				//判断设备的管理员权限是否被激活。只有被激活后，才可以执行锁频、清除数据恢复至出场设置（模拟器不支持该操作）等操作
				if(dm.isAdminActive(mAdminName)){
					dm.wipeData(0);//清除数据恢复至出场设置。手机会自动重启
				}
				abortBroadcast();
			}else if("#*lockscreen*#".equals(body)){
				Log.i(TAG,"远程锁屏");
				if(dm.isAdminActive(mAdminName)){
					dm.resetPassword("123", 0);//屏幕解锁时需要的解锁密码123
					dm.lockNow();
				}
				abortBroadcast();
			}
		}
	}
}