package com.guoshisp.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.guoshisp.mobilesafe.LostProtectedActivity;

public class OutCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//获取到广播发送来的结果数据
		String outnumber = getResultData();
		//设定我们拨号进入手机防盗的号码
		String enterPhoneBakNumber = "110";
		//判断设定的号码是否与广播过来的数据相同
		if (enterPhoneBakNumber.equals(outnumber)) {
			//进入手机防盗界面
			Intent lostIntent = new Intent(context, LostProtectedActivity.class);
			//为手机防盗对应的Activity设置一个新的任务栈
			lostIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(lostIntent);
			// 拦截掉外拨的电话号码，在拨号记录中不会显示该号码
			setResultData(null);
		}
	}
}