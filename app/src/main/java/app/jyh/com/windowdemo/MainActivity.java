package app.jyh.com.windowdemo;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

	private WindowManager.LayoutParams param;
	private WindowManager mWindowManager;
	Vibrator vibrator;

	private View v;
	private ProgressBar progressBar;
	private TextView textView;
	private TextView resetView;
	private TextView timeView;
	private TextView exitView;

	DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.progress, null, false);
		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
		progressBar.setProgress(0);
		resetView = (TextView) v.findViewById(R.id.reset);
		textView = (TextView) v.findViewById(R.id.tv);
		textView.setText(status.getDesc());
		timeView = (TextView) v.findViewById(R.id.time);
		exitView = (TextView) findViewById(R.id.exit);
		exitView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		textView.setOnClickListener(new TimerClickListener());
		resetView.setOnClickListener(new ResetClickListener());

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
		mWindowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		param = new WindowManager.LayoutParams();
		param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		param.format = 1;
		param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		param.flags = param.flags | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
		param.flags = param.flags | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

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

	private STATUS status = STATUS.READY;
	private long mCurrentTimeStamp = 0L;
	private Disposable disposable;
	private static long MAX_TIME_STAMP = 30 * 1000;
	private static final int interval = 10;
	private DecimalFormat format = new DecimalFormat("#.00");

	private class TimerClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (mCurrentTimeStamp >= MAX_TIME_STAMP) {
				Toast.makeText(MainActivity.this, "计时已经完成", Toast.LENGTH_SHORT).show();
			}
			if (status == STATUS.READY) {
				//就绪状态，点击后开始计时
				mCurrentTimeStamp = 0L;
				progressBar.setProgress(0);
				status = STATUS.RUNNING;
				textView.setText(status.getDesc());
				disposable = Observable.interval(interval, TimeUnit.MILLISECONDS)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Consumer<Long>() {
							@Override
							public void accept(Long aLong) throws Exception {
								mCurrentTimeStamp += interval;
								long v = mCurrentTimeStamp * 100 / MAX_TIME_STAMP;
								progressBar.setProgress(v > 100 ? 100 : (int) v);
								timeView.setText(format.format(((float) mCurrentTimeStamp) / 1000));
								if (mCurrentTimeStamp >= MAX_TIME_STAMP) {
									if (disposable != null && !disposable.isDisposed()) {
										disposable.dispose();
									}
									status = STATUS.READY;
									textView.setText(status.getDesc());
									mCurrentTimeStamp = 0L;
									progressBar.setProgress(0);
									vibrator.vibrate(500);
								}
							}
						}, new Consumer<Throwable>() {
							@Override
							public void accept(Throwable throwable) throws Exception {
								throwable.printStackTrace();
							}
						});
			} else if (status == STATUS.PAUSE) {
				//暂停状态，点击后继续计时
				status = STATUS.RUNNING;
				textView.setText(status.getDesc());
				disposable = Observable.interval(interval, TimeUnit.MILLISECONDS)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(new Consumer<Long>() {
							@Override
							public void accept(Long aLong) throws Exception {
								mCurrentTimeStamp += interval;
								long v = mCurrentTimeStamp * 100 / MAX_TIME_STAMP;
								progressBar.setProgress(v > 100 ? 100 : (int) v);
								timeView.setText(format.format(((float) mCurrentTimeStamp) / 1000));
								if (mCurrentTimeStamp >= MAX_TIME_STAMP) {
									if (disposable != null && !disposable.isDisposed()) {
										disposable.dispose();
									}
									status = STATUS.READY;
									textView.setText(status.getDesc());
									mCurrentTimeStamp = 0L;
									progressBar.setProgress(0);
									vibrator.vibrate(500);
								}
							}
						}, new Consumer<Throwable>() {
							@Override
							public void accept(Throwable throwable) throws Exception {
								throwable.printStackTrace();
							}
						});
			} else if (status == STATUS.RUNNING) {
				//正在运行状态，点击后暂停计时
				status = STATUS.PAUSE;
				textView.setText(status.getDesc());
				if (disposable != null && !disposable.isDisposed()) {
					disposable.dispose();
				}
			}
		}
	}

	@Override
	public void finish() {
		try {
			if (mWindowManager != null && v != null) {
				mWindowManager.removeView(v);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.finish();
	}

	private class ResetClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			//点击后直接重置
			status = STATUS.READY;
			textView.setText(status.getDesc());
			mCurrentTimeStamp = 0L;
			progressBar.setProgress(0);
			timeView.setText(format.format(((float) mCurrentTimeStamp) / 1000));
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
	}

	private enum STATUS {

		READY("开始"), PAUSE("继续"), RUNNING("暂停");

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
