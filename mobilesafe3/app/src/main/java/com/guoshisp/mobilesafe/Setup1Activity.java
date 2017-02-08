package com.guoshisp.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Setup1Activity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//进入到设置向导的第一个界面
		setContentView(R.layout.setup1);
	}
	/**
	 * 当点击设置向导的第一个界面中右下角按钮-下一步时所要执行的方法，
	 * 因为在该Button中设置有属性android:onClick=next，在下面的代码中省去该解释
	 * @param view
	 */
	public void next(View view){
		Intent intent = new Intent(this,Setup2Activity.class);
		startActivity(intent);
		finish();
		//Activity切换时播放动画。自定义一个透明度变化的动画效果，且该方法必须写在startActivity(intent)或者finish()方法之后才会生效。
		//参数一：界面进入时的动画效果 ， 参数二：界面出去时的动画效果。
		overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
	}
}