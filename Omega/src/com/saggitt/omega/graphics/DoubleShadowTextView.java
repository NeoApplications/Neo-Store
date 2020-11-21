package com.saggitt.omega.graphics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.launcher3.views.DoubleShadowBubbleTextView;

@SuppressLint("AppCompatCustomView")
public class DoubleShadowTextView extends TextView {
    private final DoubleShadowBubbleTextView.ShadowInfo mShadowInfo;

    public DoubleShadowTextView(Context context) {
        super(context, null);
        mShadowInfo = new DoubleShadowBubbleTextView.ShadowInfo(context, null, 0);
        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffset,
                mShadowInfo.ambientShadowBlur), 0f, 0f,
                mShadowInfo.keyShadowColor);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShadowInfo = new DoubleShadowBubbleTextView.ShadowInfo(context, attrs, 0);
        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffset,
                mShadowInfo.ambientShadowBlur), 0f, 0f,
                mShadowInfo.keyShadowColor);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShadowInfo = new DoubleShadowBubbleTextView.ShadowInfo(context, attrs, defStyleAttr);
        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur
                + mShadowInfo.keyShadowOffset, mShadowInfo.ambientShadowBlur), 0f, 0f, mShadowInfo.keyShadowColor);
    }

    protected void onDraw(Canvas canvas) {
        if (mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas);
            return;
        }
        getPaint().setShadowLayer(mShadowInfo.ambientShadowBlur, 0.0f, 0.0f, mShadowInfo.ambientShadowColor);
        super.onDraw(canvas);
        getPaint().setShadowLayer(mShadowInfo.keyShadowBlur, 0.0f, mShadowInfo.keyShadowOffset, mShadowInfo.keyShadowColor);
        super.onDraw(canvas);
    }
}
