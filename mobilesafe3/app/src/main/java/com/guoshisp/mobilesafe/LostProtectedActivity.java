package com.guoshisp.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guoshisp.mobilesafe.utils.Md5Encoder;

public class LostProtectedActivity extends Activity implements OnClickListener {
	private static final String TAG = "LostProtectedActivity";
	//偏好设置存储对象
	private SharedPreferences sp;
	//第一次进入”手机防盗“界面时的界面控件对象
	private EditText et_first_dialog_pwd;
	private EditText et_first_dialog_pwd_confirm;
	private Button bt_first_dialog_ok;
	private Button bt_first_dialog_cancle;
	//第二次进入”手机防盗“界面时的界面控件对象
	private EditText et_normal_dialog_pwd;
	private Button bt_normal_dialog_ok;
	private Button bt_normal_dialog_cancle;
	//设置向导结束后的结果界面中的控件
	private TextView tv_lost_protect_number;//绑定的安全号码
	private RelativeLayout rl_lost_protect_setting;//防盗保护设置是否开启所在的父控件，获取该控件是要为该控件设置点击事件（点击该控件中的任意一个控件都会响应点击事件）
	private CheckBox cb_lost_protect_setting;//防盗保护是否开启
	private TextView tv_lost_protect_reentry_setup;//该控件的点击事件执行：重新进入设置向导界面
	//对话框对象
	private AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//获取Sdcard下的config.xml文件，如果该文件不存在，那么将会自动创建该文件
		sp = getSharedPreferences("config", MODE_PRIVATE);
		// 判断用户是否设置过密码
		if (isSetupPwd()) {
			//进入非第一次进入“手机防盗”时要显示的对话框
			showNormalEntryDialog();
		} else {
			//进入第一次进入“手机防盗”时要显示的对话框
			showFirstEntryDialog();
		}
	}

	/**
	 * 第一次进入“手机防盗”时要显示的对话框
	 */
	private void showFirstEntryDialog() {
		//得到对话框的构造器
		Builder builder = new Builder(this);
		//通过View对象的inflate(Context context, int resource, ViewGroup root)对象将第一次进入“手机防盗”要弹出的窗体对话框的布局文件转换为一个View对象
		View view = View.inflate(this, R.layout.first_entry_dialog, null);
		//查找view对象中的各个控件
		et_first_dialog_pwd = (EditText) view
				.findViewById(R.id.et_first_dialog_pwd);
		et_first_dialog_pwd_confirm = (EditText) view
				.findViewById(R.id.et_first_dialog_pwd_confirm);
		bt_first_dialog_ok = (Button) view
				.findViewById(R.id.bt_first_dialog_ok);
		bt_first_dialog_cancle = (Button) view
				.findViewById(R.id.bt_first_dialog_cancle);
		//分别为“取消”、“确定”按钮设置一个监听器
		bt_first_dialog_cancle.setOnClickListener(this);
		bt_first_dialog_ok.setOnClickListener(this);
		//将上面的View对象添加到对话框上
		builder.setView(view);
		//获取到对话框对象
		dialog = builder.create();
		//显示出对话框
		dialog.show();
	}

	/**
	 * 当设置过密码后，正常进入“手机防盗”时要显示的对话框
	 */
	private void showNormalEntryDialog() {
		Builder builder = new Builder(this);
		builder.setOnCancelListener(new OnCancelListener() {
			//当点击“取消”按钮时，直接结束掉当前的LostProtectedActivity，程序会进入到主界面
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});
		//通过View对象的inflate(Context context, int resource, ViewGroup root)对象将非第一次进入“手机防盗”要弹出的窗体对话框的布局文件转换为一个View对象
		View view = View.inflate(this, R.layout.normal_entry_dialog, null);
		//查找view对象中的各个控件
		et_normal_dialog_pwd = (EditText) view
				.findViewById(R.id.et_normal_dialog_pwd);
		bt_normal_dialog_ok = (Button) view
				.findViewById(R.id.bt_normal_dialog_ok);
		bt_normal_dialog_cancle = (Button) view
				.findViewById(R.id.bt_normal_dialog_cancle);
		//分别为“取消”、“确定”按钮设置一个监听器
		bt_normal_dialog_cancle.setOnClickListener(this);
		bt_normal_dialog_ok.setOnClickListener(this);
		//将上面的View对象添加到对话框上
		builder.setView(view);
		//获取到对话框对象
		dialog = builder.create();
		//显示出对话框
		dialog.show();
	}

	/**
	 * 判断用户是否设置过密码
	 *
	 * @return
	 */
	private boolean isSetupPwd() {
		String savedpwd = sp.getString("password", "");
		if (TextUtils.isEmpty(savedpwd)) {//通过一个文本工具类来判断String是否为空
			return false;
		} else {
			return true;
		}
		// return (!TextUtils.isEmpty(savedpwd));
	}
	/**
	 * 为两个对话框中的“确定”和“取消”按钮设置的监听器
	 */
	public void onClick(View v) {

		switch (v.getId()) {
			//第一次进入“手机防盗”时弹出的对话框中，对“取消”按钮事件的处理
			case R.id.bt_first_dialog_cancle:
				dialog.cancel();//取消对话框
				finish();//结束当前的Activity后会进入程序的主界面
				break;
			//第一次进入“手机防盗”时弹出的对话框中，对“确定”按钮事件的处理
			case R.id.bt_first_dialog_ok:
				//获取到两个EditText中的输入的密码，并将EditText前后的空格给去除掉
				String pwd = et_first_dialog_pwd.getText().toString().trim();
				String pwd_confirm = et_first_dialog_pwd_confirm.getText()
						.toString().trim();
				//判断两个EditText中的内容是否为空
				if (TextUtils.isEmpty(pwd_confirm) || TextUtils.isEmpty(pwd)) {
					Toast.makeText(this, "密码不能为空", 1).show();
					return;
				}
				//判断两个EditText中的内容是否相同
				if (pwd.equals(pwd_confirm)) {
					//获取到一个编辑器对象，此处用于像sp中编辑数据
					Editor editor = sp.edit();
					//将加密后的密码存入到sp所对应的文件中
					editor.putString("password", Md5Encoder.encode(pwd));
					//将编辑的数据提交后才能真正的存入进sp中
					editor.commit();
					//销毁当前的对话框
					dialog.dismiss();
					//结束掉当前的Activity后，跳转至主界面
					finish();
				} else {
					Toast.makeText(this, "两次密码不相同", 1).show();
					return;
				}

				break;
			//非第一次进入“手机防盗”时弹出的对话框中，对“取消”按钮事件的处理
			case R.id.bt_normal_dialog_cancle:
				dialog.cancel();
				finish();
				break;
			//非第一次进入“手机防盗”时弹出的对话框中，对“确定”按钮事件的处理
			case R.id.bt_normal_dialog_ok:
				String userentrypwd = et_normal_dialog_pwd.getText().toString()
						.trim();
				if (TextUtils.isEmpty(userentrypwd)) {
					Toast.makeText(this, "密码不能为空", 1).show();
					return;
				}
				String savedpwd = sp.getString("password", "");
				//因为我们在设置密码后，存入的是加密后的密码，所以当我们将输入的密码与设置的密码比较时需要将输入的密码先加密
				if (savedpwd.equals(Md5Encoder.encode(userentrypwd))) {
					Toast.makeText(this, "密码正确进入界面", 1).show();
					dialog.dismiss();
					// 判断用户是否进行过设置向导.
					if(isSetupAlready()){
						//进入到完成设置向导后的界面
						Log.i(TAG,"进入到完成设置向导后的界面");
						setContentView(R.layout.lost_protected);
						//绑定的安全号码
						tv_lost_protect_number = (TextView) findViewById(R.id.tv_lost_protect_number);
						String safemuber = sp.getString("safemuber", "");
						tv_lost_protect_number.setText(safemuber);
						//防盗保护设置是否开启所在的父控件，获取该控件是要为该控件设置点击事件（点击该控件中的任意一个控件都会响应点击事件）
						rl_lost_protect_setting = (RelativeLayout)findViewById(R.id.rl_lost_protect_setting);
						//防盗保护是否开启
						cb_lost_protect_setting = (CheckBox)findViewById(R.id.cb_lost_protect_setting);
						boolean protecting = sp.getBoolean("protecting", false);
						cb_lost_protect_setting.setChecked(protecting);
						if(protecting){
							cb_lost_protect_setting.setText("防盗保护已经开启");
						}else{
							cb_lost_protect_setting.setText("防盗保护没有开启");
						}
						//该控件的点击事件执行：重新进入设置向导界面
						tv_lost_protect_reentry_setup = (TextView)findViewById(R.id.tv_lost_protect_reentry_setup);

						rl_lost_protect_setting.setOnClickListener(this);
						tv_lost_protect_reentry_setup.setOnClickListener(this);

					}else{
						//进入设置向导界面
						Log.i(TAG,"进入到设置向导界面");
						Intent intent = new Intent(this,Setup1Activity.class);
						//执行该方法的原因在于：当用户完成设置向导后按back键时，避免出现之前的界面，增强用户体验效果
						finish();
						startActivity(intent);
					}
					return;
				} else {
					Toast.makeText(this, "密码不正确", 1).show();
					return;
				}
			case R.id.tv_lost_protect_reentry_setup://重新进入设置向导
				Intent reentryIntent = new Intent(this,Setup1Activity.class);
				startActivity(reentryIntent);
				finish();
				break;
			case R.id.rl_lost_protect_setting://是否开启防盗保护
				Editor editor =	sp.edit();
				if(cb_lost_protect_setting.isChecked()){
					cb_lost_protect_setting.setChecked(false);
					cb_lost_protect_setting.setText("防盗保护没有开启");
					editor.putBoolean("protecting", false);

				}else{
					cb_lost_protect_setting.setChecked(true);
					cb_lost_protect_setting.setText("防盗保护已经开启");
					editor.putBoolean("protecting", true);
				}
				editor.commit();
				break;
		}
	}
	/**
	 * 判断用户是否完成过设置向导
	 *
	 * @return
	 */
	private boolean isSetupAlready() {
		//默认情况下返回false，表示用户没有进行过设置向导
		return sp.getBoolean("issetup", false);
	}
	/**
	 * 当长按Menu键时会打开一个菜单，当彩蛋第一次被打开时，框架回调该方法
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//参数一：当前Item所在的组，参数二：当前Item的id号，在Switch中要用到
		//参数三：当出现多个Item时，该数字可以决定Item在菜单中的前后位置，参数四：当前Item在菜单中的标题
		menu.add(1, 1, 1, "更改标题名称");
		return true;
	}
	/**
	 * 当一个菜单中的Item被选中时，框架回调该方法，并将所单击的Item传入
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//上面定义的id为1
		if (item.getItemId() == 1) {
			//获取一个窗体构造器
			Builder builder = new Builder(this);
			//创建一个文本输入框
			final EditText et = new EditText(this);
			//设置文本输入框中提示的内容，当点击文本输入框时，该内容会自动消失
			et.setHint("请输入新的标题名称,可以留空");
			//将文本输入框添加到窗体对话框上
			builder.setView(et);
			//为窗体对话框添加一个“确定”按钮
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						//当点击“确定”按钮时要执行的回调方法
						public void onClick(DialogInterface dialog, int which) {
							//获取文本输入框中的内容，并将文本前后的空格去除掉
							String newname = et.getText().toString().trim();
							//获取sp对应的编辑器
							Editor editor = sp.edit();
							//将修改后的名称保存到sp中，此时数据还只在缓存中
							editor.putString("newname", newname);
							//数据真正的被保存到sp对应的文件中
							editor.commit();
						}
					});
			//创建并显示出窗体对话框
			builder.create().show();
		}
		return super.onOptionsItemSelected(item);
	}
}