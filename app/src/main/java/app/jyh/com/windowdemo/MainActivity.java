package app.jyh.com.windowdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	private WindowManager.LayoutParams param;
	private WindowManager mWindowManager;

	private View v;
	private ProgressBar progressBar;
	private TextView textView;
	private STATUS status = STATUS.READY;

	DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.progress, null, false);
		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		progressBar.setProgress(0);
		textView = (TextView) v.findViewById(R.id.tv);
		textView.setText(status.getDesc());

		dm = new DisplayMetrics();

		getWindowManager().getDefaultDisplay().getMetrics(dm);

		mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		param = new WindowManager.LayoutParams();
		param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;     // 系统提示类型,重要
		param.format = 1;
		param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; // 不能抢占聚焦点
		param.flags = param.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		param.flags = param.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS; // 排版不受限制

		param.alpha = 1.0f;

		param.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;   //调整悬浮窗口至左上角
		//以屏幕左上角为原点，设置x、y初始值
		param.x = 0;
		param.y = 0;

		//设置悬浮窗口长宽数据
		param.width = dm.widthPixels;
		param.height = dip2px(this, 30);

		//显示myFloatView图像
		mWindowManager.addView(v, param);
	}

	private enum STATUS {

		READY("开始"), PAUSE("继续"), RUNNING("暂停"), COMPLETE("重置");

		private String desc;

		STATUS(String desc) {
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}


}
