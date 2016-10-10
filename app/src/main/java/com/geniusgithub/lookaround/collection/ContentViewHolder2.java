package com.geniusgithub.lookaround.collection;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.geniusgithub.lookaround.R;
import com.geniusgithub.lookaround.cache.ImageLoaderEx;
import com.geniusgithub.lookaround.component.ImageLoader;
import com.geniusgithub.lookaround.model.BaseType;

public class ContentViewHolder2 extends  RecyclerView.ViewHolder implements View.OnClickListener{
    private Context mContext;
    public CardView mCardView;
    public TextView tvTitle;
    public ImageView ivContent1;
    public ImageView ivContent2;
    public ImageView ivContent3;
    public TextView tvImageCount;
    public TextView tvArtist;
    public BaseType.InfoItemEx mItem;
    public IContentItemClick mListener;


    public ContentViewHolder2(Context context, View itemView, IContentItemClick listener) {
        super(itemView);
        mContext = context;
        tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
        tvImageCount = (TextView) itemView.findViewById(R.id.tv_imagecount);
        tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
        ivContent1 = (ImageView) itemView.findViewById(R.id.iv_content1);
        ivContent2 = (ImageView) itemView.findViewById(R.id.iv_content2);
        ivContent3 = (ImageView) itemView.findViewById(R.id.iv_content3);
        mCardView = (CardView) itemView.findViewById(R.id.cardview);
        mCardView.setOnClickListener(this);
        mListener = listener;
    }

    public void bindInfo(BaseType.InfoItemEx item, Drawable placeHodler){
        mItem = item;
        tvTitle.setText(item.mTitle);
        tvArtist.setText(item.mUserName);

        int thumailImageCount = item.getThumnaiImageCount();
        tvImageCount.setText(String.valueOf(thumailImageCount));

        if (thumailImageCount == 1){
            ivContent1.setVisibility(View.VISIBLE);
            ivContent2.setVisibility(View.INVISIBLE);
            ivContent3.setVisibility(View.INVISIBLE);
            ivContent1.setImageResource(R.drawable.load_img);
        }else if (thumailImageCount == 2){
            ivContent1.setVisibility(View.VISIBLE);
            ivContent2.setVisibility(View.VISIBLE);
            ivContent3.setVisibility(View.INVISIBLE);
            ivContent1.setImageResource(R.drawable.load_img);
            ivContent2.setImageResource(R.drawable.load_img);
        }else{
            ivContent1.setVisibility(View.VISIBLE);
            ivContent2.setVisibility(View.VISIBLE);
            ivContent3.setVisibility(View.VISIBLE);
            ivContent1.setImageResource(R.drawable.load_img);
            ivContent2.setImageResource(R.drawable.load_img);
            ivContent3.setImageResource(R.drawable.load_img);
        }


        ImageLoader.loadThumail(mContext, item.getThumnaiImageURL(0), ivContent1, placeHodler);
        ImageLoader.loadThumail(mContext, item.getThumnaiImageURL(1), ivContent2, placeHodler);
        ImageLoader.loadThumail(mContext, item.getThumnaiImageURL(2), ivContent3, placeHodler);
    }

    @Override
    public void onClick(View v) {
        if (mListener != null){
            mListener.onItemClick(mItem);
        }
    }
}
