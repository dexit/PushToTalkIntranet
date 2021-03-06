package com.sinobpo.godtalk;


import com.sinobpo.R;
import com.sinobpo.util.MyScrollLayout;
import com.sinobpo.util.OnViewChangeListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Copyright (C) 2012 移动应用研发组-神聊对讲机 版本： 日期： 描述：新手指引   操作：添加
 * 操作人：移动应用研发组-gxc
 */
public class GuiderActivity extends Activity implements OnViewChangeListener, OnClickListener{
    /** Called when the activity is first created. */
	

	private MyScrollLayout mScrollLayout;
	
	private ImageView[] mImageViews;
	
	private int mViewCount;
	
	private int mCurSel;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guider_activity);
        
        init();
    }
    
    

    private void init()
    {
    	mScrollLayout = (MyScrollLayout) findViewById(R.id.ScrollLayout);
    	
    	LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llayout);
    	
    	mViewCount = mScrollLayout.getChildCount();
    	mImageViews = new ImageView[mViewCount];
    	
    	for(int i = 0; i < mViewCount; i++)
    	{
    		mImageViews[i] = (ImageView) linearLayout.getChildAt(i);
    		mImageViews[i].setEnabled(true);
    		mImageViews[i].setOnClickListener(this);
    		mImageViews[i].setTag(i);
    	}
    	
    	mCurSel = 0;
    	mImageViews[mCurSel].setEnabled(false);
    	
    	mScrollLayout.SetOnViewChangeListener(this);
    }


    private void setCurPoint(int index)
    {
    	if (index < 0 || index > mViewCount - 1 || mCurSel == index)
    	{
    		return ;
    	}
    	
    	mImageViews[mCurSel].setEnabled(true);
    	mImageViews[index].setEnabled(false);
    	
    	mCurSel = index;
    }

    @Override
	public void OnViewChange(int view) {
		setCurPoint(view);
	}


	@Override
	public void onClick(View v) {
		int pos = (Integer)(v.getTag());
		setCurPoint(pos);
		mScrollLayout.snapToScreen(pos);
	}
}