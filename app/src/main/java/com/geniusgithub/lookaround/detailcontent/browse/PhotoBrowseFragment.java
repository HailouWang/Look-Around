package com.geniusgithub.lookaround.detailcontent.browse;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.geniusgithub.lookaround.R;
import com.geniusgithub.lookaround.base.BaseFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PhotoBrowseFragment extends BaseFragment {


    public static final String TAG = PhotoBrowseFragment.class.getSimpleName();

    private View mRootView;
    private PhotoBrowsePresenter mPhotoBrowsePresenter;
    private PhotoBrowseContact.IView mPhotoBrowseView;

    private String mCurUrl = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_player_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onUIReady(view);
        setHasOptionsMenu(true);
    }



    @Override
    public void onDestroy(){
        mPhotoBrowsePresenter.onUiDestroy();
        super.onDestroy();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browse_options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                getActivity().finish();
                break;
            case R.id.menu_download:
                mPhotoBrowsePresenter.startDownLoad(mCurUrl);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void onUIReady(View view){
        mRootView = view.findViewById(R.id.rl_root);

        mPhotoBrowsePresenter = new PhotoBrowsePresenter(getActivity());
        mPhotoBrowseView = new PhotoBrowseView(getActivity());
        mPhotoBrowseView.setupView(mRootView);
        mPhotoBrowsePresenter.bindView(mPhotoBrowseView);
        mPhotoBrowsePresenter.onUiCreate(getActivity(), getActivity().getIntent());
    }

    public class PhotoBrowseView implements PhotoBrowseContact.IView, ViewPager.OnPageChangeListener{

        private Context mContext;
        private PhotoBrowseContact.IPresenter mPicturePlayerPresenter;

        @BindView(R.id.toolbar)
        public Toolbar mToolbar;

        @BindView(R.id.view_pager)
        public HackyViewPager mViewPager;

        public PhotoBrowsePagerAdapter mBrowseAdapter;
        public int mDataSize = 0;

        public PhotoBrowseView(Context context){
            mContext = context;
        }

        @Override
        public void bindPresenter(PhotoBrowseContact.IPresenter presenter) {
            mPicturePlayerPresenter = presenter;
        }

        @Override
        public void setupView(View rootView) {
            initView(rootView);
        }


        @Override
        public void initBrowseData( List<String> data, int curIndex) {
            mCurUrl = data.get(curIndex);
            mDataSize = data.size();
            mBrowseAdapter.updateData(data);
            mViewPager.setCurrentItem(curIndex + mBrowseAdapter.getMiddlePos());
        }


        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            mCurUrl = mBrowseAdapter.getItem(position);
            int curItem = mViewPager.getCurrentItem() % mDataSize;
            updateToolTitle(curItem, mDataSize);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        private void initView(View rootView) {
            ButterKnife.bind(this, rootView);

            mBrowseAdapter = new PhotoBrowsePagerAdapter(mContext);

            mViewPager.setAdapter(mBrowseAdapter);
            mViewPager.addOnPageChangeListener(this);

            initToolBar();
        }

        private void initToolBar() {
            mToolbar.setTitle("PICTURE");
            mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
            mToolbar.setBackgroundColor(Color.parseColor("#00ffffff"));

            Activity parentActivity = getActivity();
            if (parentActivity instanceof AppCompatActivity){
                ((AppCompatActivity)parentActivity).setSupportActionBar(mToolbar);
                final ActionBar ab = ((AppCompatActivity)parentActivity).getSupportActionBar();
                ab.setDisplayHomeAsUpEnabled(true);
            }

        }

        public void updateToolTitle(int curPos, int totalCount){
            if (mToolbar != null){
                String value = curPos + 1 + "/" + totalCount;
                mToolbar.setTitle(value);
            }
        }

    }

}
