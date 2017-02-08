package com.guoshisp.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.guoshisp.mobilesafe.receiver.MyAdmin;

public class Setup4Activity extends Activity {
	private CheckBox cb_setup4_protect;
	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setup4);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		cb_setup4_protect = (CheckBox)findViewById(R.id.cb_setup4_protect);
		//用于数据的回显。判断手机防盗是否开启，默认情况下没有开启
		boolean protecting = sp.getBoolean("protecting", false);
		cb_setup4_protect.setChecked(protecting);

		cb_setup4_protect.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor editor = sp.edit();
				if(isChecked){
					editor.putBoolean("protecting", true);
					cb_setup4_protect.setText("防盗保护已经开启");
				}else{
					cb_setup4_protect.setText("防盗保护没有开启");
					editor.putBoolean("protecting", false);
				}
				editor.commit();
			}
		});
	}
	/**
	 * 当点击设置向导中的第四个界面中的“点击激活deviceadmin...”时所执行的方法。
	 * 激活手机的设备管理员权限。激活后，可以执行远程锁屏、清除数据恢复至出场设置
	 * @param view
	 */
	public void activeDeviceAdmin(View view){
		//创建出一个与MyAdmin相关联的组件
		ComponentName mAdminName = new ComponentName(this, MyAdmin.class);
		///获取手机设备管理器
		DevicePolicyManager dm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		//判断组件是否已经获取超级管理员的权限
		if (!dm.isAdminActive(mAdminName)) {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			//将组件的超级管理员权限激活
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
			startActivity(intent);
		}
	}



	/**
	 * 点击设置向导的第四个界面中的“设置完成”时所执行的方法，当执行该方法时，说明设置向导已经完成
	 * @param view
	 */
	public void next(View view){
		if(!cb_setup4_protect.isChecked()){//如果防盗保护没有开启，弹出一个对话框提示开启保护
			Builder builder = new Builder(this);
			builder.setTitle("温馨提示");
			builder.setMessage("手机防盗极大的保护了你的手机安全,强烈建议开启!");
			builder.setPositiveButton("开启", new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					//将防盗保护开启
					cb_setup4_protect.setChecked(true);
					//设置向导已经完成，在用户下次进入时判断时，值为true，说明已经进行过设置向导
					Editor editor = sp.edit();
					editor.putBoolean("issetup", true);
					editor.commit();
				}
			});
			builder.setNegativeButton("取消", new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					finish();
					//设置向导已经完成，在用户下次进入时判断时，值为true，说明已经进行过设置向导
					Editor editor = sp.edit();
					editor.putBoolean("issetup", true);
					editor.commit();
				}
			});
			builder.create().show();


			return ;
		}
		//设置向导已经完成，在用户下次进入时判断时，值为true，说明已经进行过设置向导
		Editor editor = sp.edit();
		editor.putBoolean("issetup", true);
		editor.commit();

		Intent intent = new Intent(this,LostProtectedActivity.class);
		startActivity(intent);
		finish();
		//自定义一个平移的动画效果。参数一：界面进入时的动画效果 ， 参数二：界面出去时的动画效果
		overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
	}
	public void pre(View view){
		Intent intent = new Intent(this,Setup3Activity.class);
		startActivity(intent);
		finish();
		//自定义一个透明度变化的动画效果。参数一：界面进入时的动画效果 ， 参数二：界面出去时的动画效果
		overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
	}
}