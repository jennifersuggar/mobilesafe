package com.guoshisp.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Setup2Activity extends Activity implements OnClickListener {
	private RelativeLayout rl_setup2_bind;//“点击绑定sim卡”的父控件，该控件中存在两个子控件，获取该控件的目的在于为其设置点击事件，便于点击该控件中的任何一个控件都响应到点击事件
	private ImageView iv_setup2_bind_status;//rl_setup2_bind中的一个子控件，用于显示sim卡是否被绑定时的不同状态
	private SharedPreferences sp;//用于保存sim卡是否被绑定的信息，以便程序下次加载时使用
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup2);
		rl_setup2_bind = (RelativeLayout) findViewById(R.id.rl_setup2_bind);
		rl_setup2_bind.setOnClickListener(this);
		iv_setup2_bind_status = (ImageView) findViewById(R.id.iv_setup2_bind_status);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		//初始化的逻辑-判断sim卡是否被绑定
		String simseral = sp.getString("simserial", "");
		if(TextUtils.isEmpty(simseral)){
			iv_setup2_bind_status.setImageResource(R.drawable.switch_off_normal);
		}else{
			iv_setup2_bind_status.setImageResource(R.drawable.switch_on_normal);
		}
	}
	/**
	 * 在设置向导的第二个界面中点击”点击绑定sim卡“时执行的点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_setup2_bind:
				//判断当前sim卡的状态
				String simseral = sp.getString("simserial", "");
				if(TextUtils.isEmpty(simseral)){//sim卡未绑定
					Editor editor = sp.edit();
					editor.putString("simserial", getSimSerial());
					editor.commit();
					//因为sim卡的状态是未被绑定，所以，点击条目后应该设置为绑定的状态
					iv_setup2_bind_status.setImageResource(R.drawable.switch_on_normal);
				}else{
					Editor editor = sp.edit();
					editor.putString("simserial", "");
					editor.commit();
					iv_setup2_bind_status.setImageResource(R.drawable.switch_off_normal);
				}

				break;
		}
	}

	/**
	 * 获取手机的sim卡串号
	 */
	private String getSimSerial(){
		//sim卡是与电话相关的。需要在清单文件中配置权限：<uses-permission android:name="android.permission.READ_PHONE_STATE" />
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		//返回sim卡的串号
		return tm.getSimSerialNumber();
	}
	/**
	 * 点击界面的右下角的”下一步“按钮所要执行的方法
	 * @param view
	 */
	public void next(View view){

		Intent intent = new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		//自定义一个平移的动画效果。参数一：界面进入时的动画效果 ， 参数二：界面出去时的动画效果
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
	}
	/**
	 * 点击界面左下角的”上一步“所要执行的方法
	 * @param view
	 */
	public void pre(View view){
		Intent intent = new Intent(this,Setup1Activity.class);
		startActivity(intent);
		finish();
		//自定义一个透明度变化的动画效果。参数一：界面进入时的动画效果 ， 参数二：界面出去时的动画效果
		overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
	}
}