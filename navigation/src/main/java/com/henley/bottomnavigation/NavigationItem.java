package com.henley.bottomnavigation;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.TintTypedArray;

/**
 * 导航栏Item
 *
 * @author Henley
 * @date 2019/4/2 15:15
 */
public class NavigationItem extends View {

    public final CharSequence text;
    public final Drawable icon;
    public final int customLayout;

    public NavigationItem(Context context) {
        this(context, null);
    }

    public NavigationItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs, R.styleable.NavigationItem);
        text = a.getText(R.styleable.NavigationItem_android_text);
        icon = a.getDrawable(R.styleable.NavigationItem_android_icon);
        customLayout = a.getResourceId(R.styleable.NavigationItem_android_layout, 0);
        a.recycle();
    }

}
