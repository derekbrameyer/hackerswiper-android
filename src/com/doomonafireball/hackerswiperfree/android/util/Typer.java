package com.doomonafireball.hackerswiperfree.android.util;

import com.doomonafireball.hackerswiperfree.android.R;

import android.content.Context;
import android.content.res.Resources;

import java.io.InputStream;

/**
 * User: derek Date: 1/31/14 Time: 3:43 PM
 */
public class Typer {

    private String mText;
    private int mIndex = 0; // Current cursor position
    private int mSpeed = 3; // Speed of the typer

    private Context mContext;

    public Typer(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        try {
            Resources res = mContext.getResources();
            InputStream is = res.openRawResource(R.raw.kernel);
            byte[] b = new byte[is.available()];
            is.read(b);
            mText = new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            mText = "Error: can't show help.";
        }
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public String getTextPortion(int index) {
        return mText.substring(0, index);
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public String getTextPortion() {
        String textPortion;
        if (mIndex + mSpeed >= mText.length()) {
            // Index out of bounds
            textPortion = mText.substring(mIndex);
            mIndex = 0;
        } else {
            textPortion = mText.substring(mIndex, mIndex + mSpeed);
            mIndex += mSpeed;
        }
        return textPortion;
    }
}