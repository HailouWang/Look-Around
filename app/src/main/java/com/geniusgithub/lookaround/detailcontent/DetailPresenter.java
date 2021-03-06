package com.geniusgithub.lookaround.detailcontent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.geniusgithub.common.util.AlwaysLog;
import com.geniusgithub.common.util.FileHelper;
import com.geniusgithub.lookaround.LAroundApplication;
import com.geniusgithub.lookaround.R;
import com.geniusgithub.lookaround.component.DownloadImageCacheTask;
import com.geniusgithub.lookaround.component.FileManager;
import com.geniusgithub.lookaround.datastore.DaoMaster;
import com.geniusgithub.lookaround.datastore.DaoSession;
import com.geniusgithub.lookaround.datastore.InfoItemDao;
import com.geniusgithub.lookaround.detailcontent.browse.PhotoBrowerActivity;
import com.geniusgithub.lookaround.detailcontent.web.WebViewActivity;
import com.geniusgithub.lookaround.detailcontent.web.WebViewFragment;
import com.geniusgithub.lookaround.model.BaseType;
import com.geniusgithub.lookaround.share.ShareActivity;
import com.geniusgithub.lookaround.share.ShareItem;
import com.geniusgithub.lookaround.util.CommonUtil;

import java.util.HashMap;

import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class DetailPresenter implements  DetailContract.IPresenter {

    private final static String TAG = DetailPresenter.class.getSimpleName();
    private Context mContext;
    private boolean loginStatus;
    private DetailContract.IView mView;

    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private InfoItemDao infoItemDao;
    private SQLiteDatabase db;

    private DownloadImageCacheTask mDownLoadImageTask;
    private String mSavePath;

    private BaseType.ListItem mTypeItem = new BaseType.ListItem();
    private BaseType.InfoItemEx mInfoItem = new BaseType.InfoItemEx();
    private boolean isCollect = false;


    ///////////////////////////////////////     presenter callback begin
    @Override
    public void bindView(DetailContract.IView view) {
        mView = view;
        mView.bindPresenter(this);
    }

    @Override
    public void unBindView() {

    }

    @Override
    public void shareToSina(){
        LAroundApplication.getInstance().onEvent("UMID0008");
        ShareItem.clear();

        String imageURL = mInfoItem.getImageURL(0);

        ShareItem.setText(mInfoItem.mContent);
        if (imageURL != null){
            ShareItem.setImageUrl(imageURL);
            ShareItem.setShareImagePath(mSavePath);
        }
        ShareItem.setPlatform(SinaWeibo.NAME);
        goShareActivity();
    }

    @Override
    public void shareToTencent(){
        ShareItem.clear();

        String imageURL = mInfoItem.getImageURL(0);

        ShareItem.setTitle(mInfoItem.mTitle);
        //	ShareItem.setTitleUrl("http://blog.csdn.net/lancees");
        ShareItem.setText(mInfoItem.mContent);
        if (imageURL != null){
            ShareItem.setImagePath(imageURL);
            ShareItem.setShareImagePath(mSavePath);

        }

        ShareItem.setPlatform(TencentWeibo.NAME);

        goShareActivity();
    }

    @Override
    public void shareToQZone(){
        ShareItem.clear();

        String imageURL = mInfoItem.getImageURL(0);

        ShareItem.setTitle(mInfoItem.mTitle);
        ShareItem.setTitleUrl(mInfoItem.mSourceUrl);
        ShareItem.setText(mInfoItem.mContent);
        if (imageURL != null){
            ShareItem.setImageUrl(imageURL);
            ShareItem.setShareImagePath(mSavePath);
        }
        ShareItem.setPlatform(QZone.NAME);

        goShareActivity();
    }



    @Override
    public void shareToWChat(){
        ShareItem.clear();

        String imageURL = mInfoItem.getImageURL(0);

        ShareItem.setTitle(mInfoItem.mTitle);
        ShareItem.setText(mInfoItem.mContent);
        if (imageURL != null){
            ShareItem.setImageUrl(imageURL);
            ShareItem.setShareImagePath(mSavePath);
        }
        ShareItem.setUrl(mInfoItem.mSourceUrl);
        ShareItem.setPlatform(Wechat.NAME);

        goShareActivity();
    }

    @Override
    public void shareToWFriend(){

        ShareItem.clear();

        String imageURL = mInfoItem.getImageURL(0);

        ShareItem.setTitle(mInfoItem.mTitle);
        ShareItem.setText(mInfoItem.mContent);
        if (imageURL != null){
            ShareItem.setImageUrl(imageURL);
            ShareItem.setShareImagePath(mSavePath);
        }
        ShareItem.setUrl(mInfoItem.mSourceUrl);
        ShareItem.setPlatform(WechatMoments.NAME);

        goShareActivity();
    }

    @Override
    public void enterWebView() {

        LAroundApplication.getInstance().onEvent("UMID0009");
        Intent intent = new Intent();
        intent.setClass(mContext, WebViewActivity.class);
        intent.putExtra(WebViewFragment.INTENT_EXTRA_URL, mInfoItem.mSourceUrl);
        mContext.startActivity(intent);
    }

    @Override
    public void enterPhoneView() {

        LAroundApplication.getInstance().onEvent("UMID0003");
        Intent intent = new Intent();
        intent.setClass(mContext, PhotoBrowerActivity.class);
        mContext.startActivity(intent);
    }
    ///////////////////////////////////////     presenter callback end



    ///////////////////////////////////////     lifecycle or ui operator begin
    public void onUiCreate(Context context){
        mContext = context;
        loginStatus = LAroundApplication.getInstance().getLoginStatus();
        if (!loginStatus){

            LAroundApplication.getInstance().startToMainActivity();
            if (context instanceof Activity){
                ((Activity)context).finish();
                return ;
            }

        }

        initData();
    }

    public void onUiDestroy() {
        if (db != null){
            db.close();
        }
        mView.onDestroy();
    }
    ///////////////////////////////////////     lifecycle or ui operator end

    private void initData(){
        DetailCache mDetailCache = DetailCache.getInstance();
        mTypeItem = mDetailCache.getTypeItem();
        mInfoItem = mDetailCache.getInfoItem();
        AlwaysLog.i(TAG, "mTypeItem --> \n" + mTypeItem.getShowString());

        inidDataBase();

        mView.updateToolTitle(mTypeItem.mTitle);
        mView.updateInfoItemEx(mInfoItem);


        FileHelper.createDirectory(FileManager.getDownloadFileSaveDir());

        String url = mInfoItem.getImageURL(0);
        mSavePath = FileManager.getDownloadFileSavePath(url);
        mDownLoadImageTask = new DownloadImageCacheTask(mContext, mSavePath);
        mDownLoadImageTask.execute(url);
    }
    private void inidDataBase(){
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, "lookaround-db", null);
        db = helper.getWritableDatabase();

        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        infoItemDao = daoSession.getInfoItemDao();

        isCollect = infoItemDao.isCollect(mInfoItem);
    }

    public boolean isCollect(){
        return isCollect;
    }

    public void collect(){
        AlwaysLog.i(TAG, "collect");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(BaseType.ListItem.KEY_TYPEID, mTypeItem.mTypeID);
        map.put(BaseType.ListItem.KEY_TITLE, mInfoItem.mTitle);
        LAroundApplication.getInstance().onEvent("UM0011", map);

        infoItemDao.insert(mInfoItem);
        CommonUtil.showToast(R.string.toast_collect_success, mContext);
        isCollect = true;
        AlwaysLog.i(TAG, "insert mInfoItem = \n" + mInfoItem.toString());
        if (mContext instanceof  Activity){
           ((Activity) mContext).invalidateOptionsMenu();;
        }
    }

    private void goShareActivity(){
        Intent intent = new Intent();
        intent.setClass(mContext, ShareActivity.class);
        mContext.startActivity(intent);
    }

}
