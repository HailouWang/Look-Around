package com.geniusgithub.lookaround.weibo.sdk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geniusgithub.lookaround.R;
import com.geniusgithub.lookaround.base.BaseActivityEx;
import com.geniusgithub.lookaround.util.CommonLog;
import com.geniusgithub.lookaround.util.CommonUtil;
import com.geniusgithub.lookaround.util.LogFactory;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class ShareActivity extends BaseActivityEx implements Callback , TextWatcher,
												OnClickListener, PlatformActionListener{

	private static final CommonLog log = LogFactory.createLog();
	
	private static final int MSG_TOAST = 1;
	private static final int MSG_ACTION_CCALLBACK = 2;
	private static final int MSG_CANCEL_NOTIFY = 3;
	
	private static final int MAX_TEXT_LENGTH = 140;


	private Toolbar toolbar;
	private Button mBtnCancelImage;
	private ImageView mIVShareImage;
	private EditText mETContent;
	private TextView mTVTarget;
	private TextView mTVLive;
	private View phoneFrameView;
	

	private int notifyIcon;
	private String notifyTitle;
	private String sharePath;
	
	
	private Platform mPlatform;
	private HashMap<String, Object> reqMap;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_layout);
		setupViews();
		initData();
	}

	@Override
	protected void onDestroy() {
		
		
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.share_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		return  super.onPrepareOptionsMenu(menu);

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				finish();
				break;
			case R.id.menu_share:
				share(mPlatform, reqMap);
				break;
		}

		return super.onOptionsItemSelected(item);
	}




	private void setupViews(){	
		setNotification(R.drawable.logo_icon,"Look Around");
		initToolBar();

		mBtnCancelImage = (Button)findViewById(R.id.btn_cancelimage);
		mIVShareImage = (ImageView) findViewById(R.id.iv_pic);
		mETContent = (EditText) findViewById(R.id.et_content);
		mTVTarget = (TextView) findViewById(R.id.tv_target);
		mTVLive = (TextView) findViewById(R.id.tv_live);
		phoneFrameView = findViewById(R.id.fl_phoneframe);


		mBtnCancelImage.setOnClickListener(this);
		mETContent.addTextChangedListener(this);
	}



	private void initToolBar() {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle("");
		setSupportActionBar(toolbar);

		final ActionBar ab = getSupportActionBar();
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
	}


	private void initData(){
	
		reqMap = ShareItem.reqMap;
		mPlatform = ShareSDK.getPlatform(this, (String) reqMap.get("platform"));
		Object object = reqMap.get("text");
		if (object != null){
			String value = (String) object;
			mETContent.setText(value);
			mETContent.setSelection(value.length());
		}
		updateTVLive();
		
		sharePath = ShareItem.getShareImagePath();
		log.e("sharePath = " + sharePath);
		if (sharePath == null){
			showShareImage(false);
		}else{
			Bitmap bitmap = BitmapFactory.decodeFile(sharePath);
			if (bitmap != null){
				mIVShareImage.setImageBitmap(bitmap);
			}else{
				showShareImage(false);
				sharePath = null;
				reqMap.remove("imagePath");
				reqMap.remove("imageUrl");
			}
			
		}
	
		PlatformDb db = mPlatform.getDb();
		String nickname = db.get("nickname");
		if (nickname != null){
			mTVTarget.setText(nickname);
		}
		
		String name = mPlatform.getName();
		log.e("mPlatform name = " + name);
		String value = "分享至";
		if (name.equals(QZone.NAME)){
			value += "QQ空间";
		}else if (name.equals(TencentWeibo.NAME)){
			value += "腾讯微博";
		}else if (name.equals(SinaWeibo.NAME)){
			value += "新浪微博";
		}else if (name.equals(Wechat.NAME)){
			value += "微信好友";
		}else if (name.equals(WechatMoments.NAME)){
			value += "微信朋友圈";
		}

		log.i("share title = " + value);
		toolbar.setTitle(value);
		
	}
	

	public void showShareImage(boolean flag){
		if (!flag){
			phoneFrameView.setVisibility(View.GONE);
			mBtnCancelImage.setVisibility(View.GONE);
		}else{
			phoneFrameView.setVisibility(View.VISIBLE);
			mBtnCancelImage.setVisibility(View.VISIBLE);
		}
	}
	
	/** 分享时Notification的图标和文字 */
	public void setNotification(int icon, String title) {
		notifyIcon = icon;
		notifyTitle = title;
	}

	
	/** 执行分享 */
	public void share(Platform plat, HashMap<String, Object> data) {
			boolean started = false;
		
			int relen = MAX_TEXT_LENGTH -  mETContent.length();
			if (relen < 0){
				CommonUtil.showToast(R.string.toast_too_txtcount, this);
				return ;
			}

				Toast.makeText(this, "功能暂时屏蔽，敬请谅解", Toast.LENGTH_SHORT).show();
			
		/*	String value = mETContent.getText().toString();
			reqMap.put("text", value);
			
			String name = plat.getName();
			boolean isWechat = "WechatMoments".equals(name) || "Wechat".equals(name);
			if (isWechat && !plat.isValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				msg.obj = getString(R.string.wechat_client_inavailable);
				UIHandler.sendMessage(msg, this);
				return ;
			}

			boolean isQQ = "QQ".equals(name);
			if (isQQ && !plat.isValid()) {
				Message msg = new Message();
				msg.what = MSG_TOAST;
				msg.obj = getString(R.string.qq_client_inavailable);
				UIHandler.sendMessage(msg, this);
				return ;
			}

			int shareType = Platform.SHARE_TEXT;
			if (sharePath != null){
				String imagePath = String.valueOf(data.get("imagePath"));
				if (imagePath != null && (new File(imagePath)).exists()) {
					shareType = Platform.SHARE_IMAGE;
					if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
						shareType = Platform.SHARE_WEBPAGE;
					}
				}
				else {
					String imageUrl = String.valueOf(data.get("imageUrl"));
					if (imageUrl != null) {
						shareType = Platform.SHARE_IMAGE;
						if (data.containsKey("url") && !TextUtils.isEmpty(data.get("url").toString())) {
							shareType = Platform.SHARE_WEBPAGE;
						}
					}
				}
			}	
			data.put("shareType", shareType);

			if (!started) {
				started = true;
				showNotification(2000, getString(R.string.sharing));
				finish();
			}
			mPlatform.setPlatformActionListener(this);
			ShareCore shareCore = new ShareCore();
			shareCore.share(plat, data);*/
	
	}
	
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_cancelimage:
			showShareImage(false);
			sharePath = null;
			reqMap.remove("imagePath");
			reqMap.remove("imageUrl");
			break;
		}
	}

	
	@Override
	public boolean handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_TOAST: {
				String text = String.valueOf(msg.obj);
				Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
			}
			break;
			case MSG_ACTION_CCALLBACK: {
				switch (msg.arg1) {
					case 1: { // 成功
						showNotification(2000, getString(R.string.share_completed));
					}
					break;
					case 2: { // 失败
						String expName = msg.obj.getClass().getSimpleName();
						if ("WechatClientNotExistException".equals(expName)
								|| "WechatTimelineNotSupportedException".equals(expName)) {
							showNotification(2000, getString(R.string.wechat_client_inavailable));
						}
						else if ("GooglePlusClientNotExistException".equals(expName)) {
							showNotification(2000, getString(R.string.google_plus_client_inavailable));
						}
						else if ("QQClientNotExistException".equals(expName)) {
							showNotification(2000, getString(R.string.qq_client_inavailable));
						}
						else {
							showNotification(2000, getString(R.string.share_failed));
						}
					}
					break;
					case 3: { // 取消
						showNotification(2000, getString(R.string.share_canceled));
					}
					break;
				}
			}
			break;
			case MSG_CANCEL_NOTIFY: {
				NotificationManager nm = (NotificationManager) msg.obj;
				if (nm != null) {
					nm.cancel(msg.arg1);
				}
			}
			break;
		}
		return false;
	}
		
	// 在状态栏提示分享操作
	private void showNotification(long cancelTime, String text) {
				try {
					NotificationManager nm = (NotificationManager) 
							getSystemService(Context.NOTIFICATION_SERVICE);
					final int id = Integer.MAX_VALUE / 13 + 1;
					nm.cancel(id);

					long when = System.currentTimeMillis();
					PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(), 0);
					Notification.Builder builder = new Notification.Builder(this);
					builder.setSmallIcon(notifyIcon);
					builder.setContentText(text);
					builder.setWhen(when);
					builder.setContentTitle(notifyTitle);
					builder.setContentIntent(pi);
					builder.setAutoCancel(true);


					nm.notify(id, builder.build());

					if (cancelTime > 0) {
						Message msg = new Message();
						msg.what = MSG_CANCEL_NOTIFY;
						msg.obj = nm;
						msg.arg1 = id;
						UIHandler.sendMessageDelayed(msg, cancelTime, this);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

	public void onComplete(Platform platform, int action,
			HashMap<String, Object> res) {
		log.e("onComplete Platform = " + platform.getName() + ", action = " + action);
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 1;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	public void onError(Platform platform, int action, Throwable t) {
		t.printStackTrace();
		log.e("onError Platform = " + platform.getName() + ", action = " + action);
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 2;
		msg.arg2 = action;
		msg.obj = t;
		UIHandler.sendMessage(msg, this);

		// 分享失败的统计
		ShareSDK.logDemoEvent(4, platform);
	}

	public void onCancel(Platform platform, int action) {
		log.e("onCancel Platform = " + platform.getName() + ", action = " + action);
		Message msg = new Message();
		msg.what = MSG_ACTION_CCALLBACK;
		msg.arg1 = 3;
		msg.arg2 = action;
		msg.obj = platform;
		UIHandler.sendMessage(msg, this);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		updateTVLive();
	}

	
	
	private void updateTVLive(){
		int remain = MAX_TEXT_LENGTH - mETContent.length();
		mTVLive.setText("您还可以输入" + String.valueOf(remain) + "字");
		mTVLive.setTextColor(remain > 0 ? 0xffcfcfcf : 0xffff0000);
	}

}
