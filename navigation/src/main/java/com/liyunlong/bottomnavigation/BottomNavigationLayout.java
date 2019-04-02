package com.liyunlong.bottomnavigation;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.PointerIconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * 底部导航栏
 *
 * @author LiYunlong
 * @date 2019/4/1 11:03
 */
public class BottomNavigationLayout extends LinearLayout {

    private float navigationTextSize;
    private int navigationPaddingStart;
    private int navigationPaddingTop;
    private int navigationPaddingEnd;
    private int navigationPaddingBottom;
    private int navigationTextAppearance;
    private int navigationIconSize;
    private PorterDuff.Mode navigationIconTintMode;
    private ColorStateList navigationTextColor;
    private ColorStateList navigationIconTint;
    private int navigationItemBackgroundResId;
    private boolean unboundedRipple;
    private ColorStateList navigationRippleColorStateList;

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private DataSetObserver pagerAdapterObserver;
    private AdapterChangeListener adapterChangeListener;
    private OnNavigationSelectedListener currentVpSelectedListener;
    private BottomNavigationLayoutOnPageChangeListener pageChangeListener;
    private boolean setupViewPagerImplicitly;

    private Navigation selectedNavigation;
    private final ArrayList<Navigation> navigations = new ArrayList<>();
    private final ArrayList<OnNavigationSelectedListener> selectedListeners = new ArrayList<>();

    public BottomNavigationLayout(Context context) {
        this(context, null);
    }

    public BottomNavigationLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_BottomNavigationLayout);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomNavigationLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        int defaultIconSize = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_icon_size);
        int defaultTextSize = getResources().getDimensionPixelSize(R.dimen.bottom_navigation_active_text_size);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BottomNavigationLayout, defStyleAttr, defStyleRes);

        int padding = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationPadding, 10);
        navigationPaddingStart = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationPaddingStart, padding);
        navigationPaddingTop = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationPaddingTop, padding);
        navigationPaddingEnd = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationPaddingEnd, padding);
        navigationPaddingBottom = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationPaddingBottom, padding);

        if (a.hasValue(R.styleable.BottomNavigationLayout_navigationTextAppearance)) {
            navigationTextAppearance = a.getResourceId(R.styleable.BottomNavigationLayout_navigationTextAppearance, 0);
            // Text colors/sizes come from the text appearance first
            final TypedArray ta = context.obtainStyledAttributes(navigationTextAppearance, androidx.appcompat.R.styleable.TextAppearance);
            try {
                navigationTextSize = ta.getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, defaultTextSize);
                navigationTextColor = ta.getColorStateList(androidx.appcompat.R.styleable.TextAppearance_android_textColor);
            } finally {
                ta.recycle();
            }
        }

        if (a.hasValue(R.styleable.BottomNavigationLayout_navigationTextColor)) {
            navigationTextColor = a.getColorStateList(R.styleable.BottomNavigationLayout_navigationTextColor);
        }
        if (a.hasValue(R.styleable.BottomNavigationLayout_navigationSelectedTextColor)) {
            final int selected = a.getColor(R.styleable.BottomNavigationLayout_navigationSelectedTextColor, 0);
            navigationTextColor = createColorStateList(navigationTextColor.getDefaultColor(), selected);
        }
        navigationTextSize = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationTextSize, defaultTextSize);

        navigationIconSize = a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_navigationIconSize, defaultIconSize);

        if (a.hasValue(R.styleable.BottomNavigationLayout_elevation)) {
            ViewCompat.setElevation(this, a.getDimensionPixelSize(R.styleable.BottomNavigationLayout_elevation, 0));
        }

        if (a.hasValue(R.styleable.BottomNavigationLayout_navigationIconTint)) {
            int resourceId = a.getResourceId(R.styleable.BottomNavigationLayout_navigationIconTint, 0);
            if (resourceId != 0) {
                navigationIconTint = AppCompatResources.getColorStateList(context, resourceId);
            }
        }
        navigationIconTintMode = parseTintMode(a.getInt(R.styleable.BottomNavigationLayout_navigationIconTintMode, -1), null);

        if (a.hasValue(R.styleable.BottomNavigationLayout_navigationRippleColor)) {
            int resourceId = a.getResourceId(R.styleable.BottomNavigationLayout_navigationRippleColor, 0);
            if (resourceId != 0) {
                navigationRippleColorStateList = AppCompatResources.getColorStateList(context, resourceId);
            }
        }

        navigationItemBackgroundResId = a.getResourceId(R.styleable.BottomNavigationLayout_navigationBackground, 0);

        unboundedRipple = a.getBoolean(R.styleable.BottomNavigationLayout_navigationUnboundedRipple, false);
        a.recycle();

        if (getBackground() == null) {
            ViewCompat.setBackground(this, new MaterialShapeDrawable());
        }
    }

    /**
     * 在布局中添加一个导航(导航将被添加到列表的末尾，如果这是要添加的第一个导航，它将成为选中的导航)
     *
     * @param navigation 要添加的导航
     */
    public void addNavigation(@NonNull Navigation navigation) {
        addNavigation(navigation, navigations.isEmpty());
    }

    /**
     * 在布局中指定位置添加一个导航(如果这是要添加的第一个导航，它将成为选中的导航)
     *
     * @param navigation 要添加的导航
     * @param position   要添加的导航的位置索引
     */
    public void addNavigation(@NonNull Navigation navigation, int position) {
        addNavigation(navigation, position, navigations.isEmpty());
    }

    /**
     * 在布局中添加一个导航(导航将被添加到列表的末尾)
     *
     * @param navigation  要添加的导航
     * @param setSelected 是否为选中的导航
     */
    public void addNavigation(@NonNull Navigation navigation, boolean setSelected) {
        addNavigation(navigation, navigations.size(), setSelected);
    }

    /**
     * 在布局中指定位置添加一个导航
     *
     * @param navigation  要添加的导航
     * @param position    要添加的导航的位置索引
     * @param setSelected 是否为选中的导航
     */
    public void addNavigation(@NonNull Navigation navigation, int position, boolean setSelected) {
        if (navigation.parent != this) {
            throw new IllegalArgumentException("Navigation belongs to a different.");
        }
        configureNavigation(navigation, position);
        addNavigationView(navigation);

        if (setSelected) {
            navigation.select();
        }
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    @Override
    public void addView(View child) {
        addViewInternal(child);
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    @Override
    public void addView(View child, int index) {
        addViewInternal(child);
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    @Override
    public void addView(View child, int width, int height) {
        addViewInternal(child);
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        addViewInternal(child);
    }

    /**
     * 添加View(仅支持{@link NavigationItem})
     */
    private void addViewInternal(final View child) {
        if (child instanceof NavigationItem) {
            addTabFromItemView((NavigationItem) child);
        } else {
            throw new IllegalArgumentException("Only NavigationItem instances can be added to BottomNavigationLayout");
        }
    }

    private void addTabFromItemView(@NonNull NavigationItem item) {
        final Navigation navigation = newNavigation();
        if (item.text != null) {
            navigation.setText(item.text);
        }
        if (item.icon != null) {
            navigation.setIcon(item.icon);
        }
        if (item.customLayout != 0) {
            navigation.setCustomView(item.customLayout);
        }
        if (!TextUtils.isEmpty(item.getContentDescription())) {
            navigation.setContentDescription(item.getContentDescription());
        }
        addNavigation(navigation);
    }

    /**
     * 更新所有的导航
     */
    private void updateAllNavigations() {
        for (int i = 0, z = navigations.size(); i < z; i++) {
            navigations.get(i).updateView();
        }
    }

    /**
     * 创建一个NavigationView
     */
    private NavigationView createNavigationView(@NonNull final Navigation navigation) {
        NavigationView navigationView = new NavigationView(getContext());
        navigationView.setNavigation(navigation);
        navigationView.setClickable(true);
        navigationView.setFocusable(true);
        if (TextUtils.isEmpty(navigation.contentDesc)) {
            navigationView.setContentDescription(navigation.text);
        } else {
            navigationView.setContentDescription(navigation.contentDesc);
        }
        return navigationView;
    }

    /**
     * 将导航添加到指定位置并修改后面导航的索引
     *
     * @param navigation 要添加的导航
     * @param position   要添加的导航的索引
     */
    private void configureNavigation(Navigation navigation, int position) {
        navigation.setPosition(position);
        navigations.add(position, navigation);

        final int count = navigations.size();
        for (int i = position + 1; i < count; i++) {
            navigations.get(i).setPosition(i);
        }
    }

    /**
     * 将指定导航添加到布局中
     */
    private void addNavigationView(Navigation navigation) {
        final NavigationView navigationView = navigation.view;
        navigationView.setSelected(false);
        navigationView.setActivated(false);
        super.addView(navigationView, navigation.getPosition(), generateLayoutParams());
    }

    private LayoutParams generateLayoutParams() {
        return new LayoutParams(0, FrameLayout.LayoutParams.MATCH_PARENT, 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw navigation background layer for each navigation item
        for (int i = 0; i < getChildCount(); i++) {
            View navigationView = getChildAt(i);
            if (navigationView instanceof NavigationView) {
                ((NavigationView) navigationView).drawBackground(canvas);
            }
        }
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (viewPager == null) {
            // If we don't have a ViewPager already, check if our parent is a ViewPager to
            // setup with it automatically
            final ViewParent parent = getParent();
            if (parent instanceof ViewPager) {
                // If we have a ViewPager parent and we've been added as part of its decor, let's
                // assume that we should automatically setup to display any titles
                setupWithViewPager((ViewPager) parent, true, true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (setupViewPagerImplicitly) {
            // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
            setupWithViewPager(null);
            setupViewPagerImplicitly = false;
        }
    }

    /**
     * 添加导航栏选择状态改变监听
     *
     * @param listener 要添加的导航栏选择状态改变监听
     */
    public void addOnNavigationSelectedListener(@NonNull OnNavigationSelectedListener listener) {
        if (!selectedListeners.contains(listener)) {
            selectedListeners.add(listener);
        }
    }

    /**
     * 移除导航栏选择状态改变监听
     *
     * @param listener 要移除的导航栏选择状态改变监听
     * @see #addOnNavigationSelectedListener(OnNavigationSelectedListener)
     */
    public void removeOnNavigationSelectedListener(@NonNull OnNavigationSelectedListener listener) {
        selectedListeners.remove(listener);
    }

    /**
     * 移除所有的导航栏选择状态改变监听
     */
    public void clearOnNavigationSelectedListeners() {
        selectedListeners.clear();
    }

    /**
     * 创建一个新的{@link Navigation}(需要使用{@link #addNavigation(Navigation)}或相关方法手动添加)
     *
     * @see #addNavigation(Navigation)
     */
    @NonNull
    public Navigation newNavigation() {
        Navigation navigation = new Navigation();
        navigation.parent = this;
        navigation.view = createNavigationView(navigation);
        return navigation;
    }

    /**
     * 返回当前导航栏中的导航数量
     */
    public int getNavigationCount() {
        return navigations.size();
    }

    /**
     * 返回指定索引位置的导航
     */
    @Nullable
    public Navigation getNavigationAt(int index) {
        return (index < 0 || index >= getNavigationCount()) ? null : navigations.get(index);
    }

    /**
     * 返回当前选中导航的索引(如果没有选中的导航，则返回-1)
     */
    public int getSelectedPosition() {
        return selectedNavigation != null ? selectedNavigation.getPosition() : -1;
    }

    /**
     * 从布局中移除指定导航(如果被移除的是选中的导航，则另一个存在的导航将被选中)
     *
     * @param navigation 将被移除的导航
     */
    public void removeNavigation(Navigation navigation) {
        if (navigation.parent != this) {
            throw new IllegalArgumentException("Navigation does not belong to this BottomNavigationLayout.");
        }
        removeNavigationAt(navigation.getPosition());
    }

    /**
     * 从布局中移除指定导航(如果被移除的是选中的导航，则另一个存在的导航将被选中)
     *
     * @param position 将被移除的导航的索引
     */
    public void removeNavigationAt(int position) {
        final int selectedPosition = selectedNavigation != null ? selectedNavigation.getPosition() : 0;
        removeNavigationViewAt(position);

        final Navigation removed = navigations.remove(position);
        if (removed != null) {
            removed.reset();
        }

        final int newCount = navigations.size();
        for (int i = position; i < newCount; i++) {
            navigations.get(i).setPosition(i);
        }

        if (selectedPosition == position) {
            selectNavigation(navigations.isEmpty() ? null : navigations.get(Math.max(0, position - 1)));
        }
    }

    /**
     * 从导航栏中删除所有导航并取消选中导航
     */
    public void removeAllNavigations() {
        // Remove all the views
        for (int i = getChildCount() - 1; i >= 0; i--) {
            removeNavigationViewAt(i);
        }

        for (final Iterator<Navigation> i = navigations.iterator(); i.hasNext(); ) {
            final Navigation navigation = i.next();
            i.remove();
            navigation.reset();
        }

        selectedNavigation = null;
    }

    /**
     * Set whether this {@link BottomNavigationLayout} will have an unbounded ripple effect or if ripple will be
     * bound to the navigation item size.
     *
     * <p>Defaults to false.
     *
     * @attr ref R.styleable#BottomNavigationLayout_navigationUnboundedRipple
     * @see #hasUnboundedRipple()
     */
    public void setUnboundedRipple(boolean unboundedRipple) {
        if (this.unboundedRipple != unboundedRipple) {
            this.unboundedRipple = unboundedRipple;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof NavigationView) {
                    ((NavigationView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    /**
     * Returns whether this {@link BottomNavigationLayout} has an unbounded ripple effect, or if ripple is bound to
     * the navigation item size.
     *
     * @attr ref R.styleable#BottomNavigationLayout_navigationUnboundedRipple
     * @see #setUnboundedRipple(boolean)
     */
    public boolean hasUnboundedRipple() {
        return unboundedRipple;
    }

    /**
     * 返回用于导航的不同状态(正常/选中)的文本颜色
     */
    @Nullable
    public ColorStateList getNavigationTextColor() {
        return navigationTextColor;
    }

    /**
     * 设置用于导航的不同状态(正常/选中)的文本颜色
     *
     * @see #getNavigationTextColor()
     */
    public void setNavigationTextColor(@Nullable ColorStateList textColor) {
        if (navigationTextColor != textColor) {
            navigationTextColor = textColor;
            updateAllNavigations();
        }
    }

    /**
     * 设置用于导航的不同状态(正常/选中)的文本颜色
     *
     * @see #getNavigationTextColor()
     */
    public void setNavigationTextColor(@ColorRes int textColorResId) {
        setNavigationTextColor(AppCompatResources.getColorStateList(getContext(), textColorResId));
    }

    /**
     * 设置用于导航的不同状态(正常/选中)的文本颜色
     *
     * @attr ref R.styleable#BottomNavigationLayout_navigationTextColor
     * @attr ref R.styleable#BottomNavigationLayout_navigationSelectedTextColor
     */
    public void setNavigationTextColors(int normalColor, int selectedColor) {
        setNavigationTextColor(createColorStateList(normalColor, selectedColor));
    }

    /**
     * 返回用于导航的不同状态(正常/选中)的图标颜色
     */
    @Nullable
    public ColorStateList getNavigationIconTint() {
        return navigationIconTint;
    }

    /**
     * 设置用于导航的不同状态(正常/选中)的图标颜色
     *
     * @see #getNavigationIconTint()
     */
    public void setNavigationIconTint(@Nullable ColorStateList iconTint) {
        if (navigationIconTint != iconTint) {
            navigationIconTint = iconTint;
            updateAllNavigations();
        }
    }

    /**
     * 设置用于导航的不同状态(正常/选中)的图标颜色
     *
     * @see #getNavigationIconTint()
     */
    public void setNavigationIconTintResource(@ColorRes int iconTintResId) {
        setNavigationIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResId));
    }

    /**
     * 返回导航栏的水纹波颜色
     *
     * @see #setNavigationRippleColor(ColorStateList)
     */
    @Nullable
    public ColorStateList getNavigationRippleColor() {
        return navigationRippleColorStateList;
    }

    /**
     * 设置导航栏的水纹波颜色
     *
     * <p>当在KitKat及以下设备上运行时，会将此颜色绘制为填充的覆盖层，而不是波纹
     *
     * @attr ref R.styleable#BottomNavigationLayout_navigationRippleColor
     * @see #getNavigationRippleColor()
     */
    public void setNavigationRippleColor(@Nullable ColorStateList color) {
        if (navigationRippleColorStateList != color) {
            navigationRippleColorStateList = color;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof NavigationView) {
                    ((NavigationView) child).updateBackgroundDrawable(getContext());
                }
            }
        }
    }

    /**
     * 设置导航栏的水纹波颜色
     *
     * <p>当在KitKat及以下设备上运行时，会将此颜色绘制为填充的覆盖层，而不是波纹
     *
     * @param rippleColorResId A color resource to use as ripple color.
     * @see #getNavigationRippleColor()
     */
    public void setNavigationRippleColorResource(@ColorRes int rippleColorResId) {
        setNavigationRippleColor(AppCompatResources.getColorStateList(getContext(), rippleColorResId));
    }

    /**
     * 关联{@link ViewPager}到{@link BottomNavigationLayout}
     *
     * @param viewPager 要关联到的ViewPager或null以清除之前的关联
     * @see #setupWithViewPager(ViewPager, boolean)
     */
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        setupWithViewPager(viewPager, true);
    }

    /**
     * 关联{@link ViewPager}到{@link BottomNavigationLayout}
     *
     * <p>此方法将把给定的{@link ViewPager}和{@link BottomNavigationLayout}关联在一起，以便其中一个中的更改自动反映到另一个中，包括滚动状态更改和单击。
     * <p>如果{@code autoRefresh}为{@code true}，则{@link PagerAdapter}中的任何更改都将触发此布局，以便重新填充来自适配器的标题
     * <p>如果给定的ViewPager是非空的，则需要已经设置了{@link PagerAdapter}
     * <p>此布局中显示的导航将填充来自{@link PagerAdapter}的标题
     *
     * @param viewPager   要关联到的ViewPager或null以清除之前的关联
     * @param autoRefresh 如果给定ViewPager的内容发生更改，此布局是否应刷新其内容
     */
    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
        setupWithViewPager(viewPager, autoRefresh, false);
    }

    private void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
        if (this.viewPager != null) {
            // If we've already been setup with a ViewPager, remove us from it
            if (pageChangeListener != null) {
                this.viewPager.removeOnPageChangeListener(pageChangeListener);
            }
            if (adapterChangeListener != null) {
                this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
            }
        }

        if (currentVpSelectedListener != null) {
            // If we already have a navigation selected listener for the ViewPager, remove it
            removeOnNavigationSelectedListener(currentVpSelectedListener);
            currentVpSelectedListener = null;
        }

        if (viewPager != null) {
            this.viewPager = viewPager;

            // Add our custom OnPageChangeListener to the ViewPager
            if (pageChangeListener == null) {
                pageChangeListener = new BottomNavigationLayoutOnPageChangeListener(this);
            }
            viewPager.addOnPageChangeListener(pageChangeListener);

            // Now we'll add a navigation selected listener to set ViewPager's current item
            currentVpSelectedListener = new ViewPagerOnNavigationSelectedListener(viewPager);
            addOnNavigationSelectedListener(currentVpSelectedListener);

            final PagerAdapter adapter = viewPager.getAdapter();
            if (adapter != null) {
                // Now we'll populate ourselves from the pager adapter, adding an observer if
                // autoRefresh is enabled
                setPagerAdapter(adapter, autoRefresh);
            }

            // Add a listener so that we're notified of any adapter changes
            if (adapterChangeListener == null) {
                adapterChangeListener = new AdapterChangeListener();
            }
            adapterChangeListener.setAutoRefresh(autoRefresh);
            viewPager.addOnAdapterChangeListener(adapterChangeListener);

            // Now update the scroll position to match the ViewPager's current item
            setSelectedNavigationView(viewPager.getCurrentItem());
        } else {
            // We've been given a null ViewPager so we need to clear out the internal state,
            // listeners and observers
            this.viewPager = null;
            setPagerAdapter(null, false);
        }

        setupViewPagerImplicitly = implicitSetup;
    }

    /**
     * 选中指定索引位置的的导航
     *
     * @param position 要选中的导航的索引
     */
    public void selectNavigation(int position) {
        selectNavigation(getNavigationAt(position));
    }

    /**
     * 选中指定导航
     *
     * @param navigation 要选中的导航，为{@code null}则不选中任一导航
     */
    public void selectNavigation(@Nullable Navigation navigation) {
        if (navigation == selectedNavigation) {
            return;
        }
        final int newPosition = navigation != null ? navigation.getPosition() : Navigation.INVALID_POSITION;
        if (newPosition != Navigation.INVALID_POSITION) {
            setSelectedNavigationView(newPosition);
        }
        final Navigation unselected = selectedNavigation;
        this.selectedNavigation = navigation;
        if (navigation != null) {
            dispatchNavigationSelectedChanged(navigation, unselected);
        }
    }

    /**
     * 分发导航栏选择状态改变的回调
     *
     * @param selected   选中的导航
     * @param unselected 未选中的导航
     */
    private void dispatchNavigationSelectedChanged(@NonNull final Navigation selected, Navigation unselected) {
        for (OnNavigationSelectedListener listener : selectedListeners) {
            listener.onNavigationSelectedChanged(selected, unselected);
        }
    }

    /**
     * 设置选中的NavigationView并取消选中其他所有的NavigationView
     *
     * @param position 要选中的导航的索引
     */
    private void setSelectedNavigationView(int position) {
        final int childCount = getChildCount();
        if (position < childCount) {
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                child.setSelected(i == position);
                child.setActivated(i == position);
            }
        }
    }

    /**
     * 移除指定索引位置的NavigationView
     *
     * @param position 要移除的导航的索引
     */
    private void removeNavigationViewAt(int position) {
        final NavigationView view = (NavigationView) getChildAt(position);
        if (view == null) {
            return;
        }
        removeViewAt(position);
        view.reset();
        requestLayout();
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        final int[][] states = new int[2][];
        final int[] colors = new int[2];
        int i = 0;

        states[i] = SELECTED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i++;

        return new ColorStateList(states, colors);
    }

    void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
        if (pagerAdapter != null && pagerAdapterObserver != null) {
            // If we already have a PagerAdapter, unregister our observer
            pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
        }

        pagerAdapter = adapter;

        if (addObserver && adapter != null) {
            // Register our observer on the new adapter
            if (pagerAdapterObserver == null) {
                pagerAdapterObserver = new PagerAdapterObserver();
            }
            adapter.registerDataSetObserver(pagerAdapterObserver);
        }

        // Finally make sure we reflect the new adapter
        populateFromPagerAdapter();
    }

    void populateFromPagerAdapter() {
        removeAllNavigations();
        if (pagerAdapter != null) {
            Navigation navigation;
            final int adapterCount = pagerAdapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                navigation = newNavigation();
                navigation.setText(pagerAdapter.getPageTitle(i));
                if (pagerAdapter instanceof INavigationIconProvider) {
                    navigation.setIcon(((INavigationIconProvider) pagerAdapter).getNavigationIcon(i));
                }
                addNavigation(navigation, false);
            }

            // Make sure we reflect the currently set ViewPager item
            if (viewPager != null && adapterCount > 0) {
                final int curItem = viewPager.getCurrentItem();
                if (curItem != getSelectedPosition() && curItem < getNavigationCount()) {
                    selectNavigation(getNavigationAt(curItem));
                }
            }
        }
    }

    private PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.ADD;
            default:
                return defaultMode;
        }
    }

    /**
     * 导航栏选择状态改变的回调接口
     */
    public interface OnNavigationSelectedListener {
        /**
         * 当导航栏选择状态改变时回调该方法
         *
         * @param selected   选中的导航
         * @param unselected 未选中的导航
         */
        void onNavigationSelectedChanged(Navigation selected, Navigation unselected);

    }

    /**
     * 导航栏图标提供者接口
     */
    public interface INavigationIconProvider {
        /**
         * 返回导航栏图标资源ID
         */
        @DrawableRes
        int getNavigationIcon(int position);
    }

    /**
     * A {@link ViewPager.OnPageChangeListener} class which contains the necessary calls back to the
     * provided {@link BottomNavigationLayout} so that the navigation position is kept in sync.
     *
     * <p>This class stores the provided BottomNavigationLayout weakly, meaning that you can use {@link
     * ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
     * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and not cause a
     * leak.
     */
    private static class BottomNavigationLayoutOnPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

        private final WeakReference<BottomNavigationLayout> navigationLayoutRef;

        BottomNavigationLayoutOnPageChangeListener(BottomNavigationLayout navigationLayout) {
            navigationLayoutRef = new WeakReference<>(navigationLayout);
        }

        @Override
        public void onPageSelected(final int position) {
            final BottomNavigationLayout navigationLayout = navigationLayoutRef.get();
            if (navigationLayout != null
                    && navigationLayout.getSelectedPosition() != position
                    && position < navigationLayout.getNavigationCount()) {
                navigationLayout.selectNavigation(navigationLayout.getNavigationAt(position));
            }
        }

    }

    /**
     * A {@link OnNavigationSelectedListener} class which contains the necessary calls back to the
     * provided {@link ViewPager} so that the navigation position is kept in sync.
     */
    private static class ViewPagerOnNavigationSelectedListener implements OnNavigationSelectedListener {

        private final ViewPager viewPager;

        ViewPagerOnNavigationSelectedListener(ViewPager viewPager) {
            this.viewPager = viewPager;
        }

        @Override
        public void onNavigationSelectedChanged(Navigation selected, Navigation unselected) {
            viewPager.setCurrentItem(selected.getPosition());
        }
    }

    private class PagerAdapterObserver extends DataSetObserver {
        PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            populateFromPagerAdapter();
        }
    }

    private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {

        private boolean autoRefresh;

        AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(@NonNull ViewPager pager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
            if (viewPager == pager) {
                setPagerAdapter(newAdapter, autoRefresh);
            }
        }

        void setAutoRefresh(boolean autoRefresh) {
            this.autoRefresh = autoRefresh;
        }
    }

    public static final class NavigationItem extends View {

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

    /**
     * 布局中的导航(可以通过{@link #newNavigation()}创建实例)
     */
    public static final class Navigation {

        /**
         * 导航的无效位置
         */
        public static final int INVALID_POSITION = -1;

        private Object tag;
        private Drawable icon;
        private CharSequence text;
        private CharSequence contentDesc;
        private View customView;
        private int position = INVALID_POSITION;

        public NavigationView view;
        public BottomNavigationLayout parent;

        private Navigation() {
            // Private constructor
        }

        /**
         * 返回{@link Navigation}的Tag
         */
        @Nullable
        public Object getTag() {
            return tag;
        }

        /**
         * 设置{@link Navigation}的Tag
         */
        @NonNull
        public Navigation setTag(@Nullable Object tag) {
            this.tag = tag;
            return this;
        }

        /**
         * 返回{@link Navigation}的自定义视图
         *
         * @see #setCustomView(int)
         * @see #setCustomView(View)
         */
        @Nullable
        public View getCustomView() {
            return customView;
        }

        /**
         * 设置{@link Navigation}的自定义视图
         *
         * <p>如果提供的视图包含{@link TextView}，其ID为{@link android.R.id#text1}，那么将使用给予{@link #setText(CharSequence)}的值更新该视图。同样，如果此布局包含带有ID {@link android.R.id#icon}的{@link ImageView}，那么它将使用给予{@link #setIcon(Drawable)}的值进行更新。
         *
         * @see #setCustomView(View)
         */
        @NonNull
        public Navigation setCustomView(@LayoutRes int resId) {
            return setCustomView(resId == 0 ? null : LayoutInflater.from(view.getContext()).inflate(resId, view, false));
        }

        /**
         * 设置{@link Navigation}的自定义视图
         *
         * <p>如果提供的视图包含{@link TextView}，其ID为{@link android.R.id#text1}，那么将使用给予{@link #setText(CharSequence)}的值更新该视图。同样，如果此布局包含带有ID {@link android.R.id#icon}的{@link ImageView}，那么它将使用给予{@link #setIcon(Drawable)}的值进行更新。
         *
         * @see #setCustomView(int)
         */
        @NonNull
        public Navigation setCustomView(@Nullable View view) {
            customView = view;
            updateView();
            return this;
        }

        /**
         * 返回{@link Navigation}的索引(如果此导航当前不在导航栏中，则返回{@link #INVALID_POSITION})
         */
        public int getPosition() {
            return position;
        }

        /**
         * 设置{@link Navigation}的索引
         */
        void setPosition(int position) {
            this.position = position;
        }

        /**
         * 返回{@link Navigation}的图标
         *
         * @see #setIcon(int)
         * @see #setIcon(Drawable)
         */
        @Nullable
        public Drawable getIcon() {
            return icon;
        }

        /**
         * 设置{@link Navigation}的图标
         *
         * @see #setIcon(Drawable)
         */
        @NonNull
        public Navigation setIcon(@DrawableRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Navigation not attached to a BottomNavigationLayout");
            }
            return setIcon(resId == 0 ? null : AppCompatResources.getDrawable(parent.getContext(), resId));
        }

        /**
         * 设置{@link Navigation}的图标
         *
         * @see #setIcon(int)
         */
        @NonNull
        public Navigation setIcon(@Nullable Drawable icon) {
            this.icon = icon;
            updateView();
            return this;
        }

        /**
         * 返回{@link Navigation}的文本
         *
         * @see #setText(int)
         * @see #setText(CharSequence)
         */
        @Nullable
        public CharSequence getText() {
            return text;
        }

        /**
         * 设置{@link Navigation}的文本
         *
         * @see #setText(CharSequence)
         */
        @NonNull
        public Navigation setText(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Navigation not attached to a BottomNavigationLayout");
            }
            return setText(resId == 0 ? null : parent.getResources().getText(resId));
        }

        /**
         * 设置{@link Navigation}的文本
         *
         * @see #setText(int)
         */
        @NonNull
        public Navigation setText(@Nullable CharSequence text) {
            if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
                view.setContentDescription(text);
            }
            this.text = text;
            updateView();
            return this;
        }

        /**
         * 返回{@link Navigation}的内容说明(用于在辅助功能支持中使用)
         *
         * @see #setContentDescription(int)
         * @see #setContentDescription(CharSequence)
         */
        @Nullable
        public CharSequence getContentDescription() {
            // This returns the view's content description instead of contentDesc because if the title
            // is used as a replacement for the content description, contentDesc will be empty.
            return (view == null) ? null : view.getContentDescription();
        }

        /**
         * 设置{@link Navigation}的内容说明(用于在辅助功能支持中使用，如果未提供内容描述，则将使用标题)
         *
         * @see #setContentDescription(CharSequence)
         * @see #getContentDescription()
         */
        @NonNull
        public Navigation setContentDescription(@StringRes int resId) {
            if (parent == null) {
                throw new IllegalArgumentException("Navigation not attached to a BottomNavigationLayout");
            }
            return setContentDescription(resId == 0 ? null : parent.getResources().getText(resId));
        }

        /**
         * 设置{@link Navigation}的内容说明(用于在辅助功能支持中使用，如果未提供内容描述，则将使用标题)
         *
         * @see #setContentDescription(int)
         * @see #getContentDescription()
         */
        @NonNull
        public Navigation setContentDescription(@Nullable CharSequence contentDesc) {
            this.contentDesc = contentDesc;
            updateView();
            return this;
        }

        /**
         * 选择当前{@link Navigation}(仅在导航已添加到操作栏时有效)
         */
        public void select() {
            if (parent == null) {
                throw new IllegalArgumentException("Navigation not attached to a BottomNavigationLayout.");
            }
            parent.selectNavigation(this);
        }

        /**
         * 返回当前{@link Navigation}是否被选中
         */
        public boolean isSelected() {
            if (parent == null) {
                throw new IllegalArgumentException("Navigation not attached to a BottomNavigationLayout.");
            }
            return parent.getSelectedPosition() == position;
        }

        /**
         * 更新{@link NavigationView}
         */
        void updateView() {
            if (view != null) {
                view.update();
            }
        }

        /**
         * 重置{@link Navigation}
         */
        void reset() {
            parent = null;
            view = null;
            tag = null;
            icon = null;
            text = null;
            contentDesc = null;
            position = INVALID_POSITION;
            customView = null;
        }
    }

    class NavigationView extends LinearLayout {

        private Navigation navigation;
        private TextView textView;
        private ImageView iconView;
        private View customView;
        private TextView customTextView;
        private ImageView customIconView;
        private Drawable wrappedIcon;
        private Drawable backgroundDrawable;

        public NavigationView(Context context) {
            super(context);
            updateBackgroundDrawable(context);
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            setClickable(true);
            ViewCompat.setPaddingRelative(
                    this, navigationPaddingStart, navigationPaddingTop, navigationPaddingEnd, navigationPaddingBottom);
            ViewCompat.setPointerIcon(
                    this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
        }

        private void updateBackgroundDrawable(Context context) {
            if (navigationItemBackgroundResId != 0) {
                backgroundDrawable = AppCompatResources.getDrawable(context, navigationItemBackgroundResId);
                if (backgroundDrawable != null && backgroundDrawable.isStateful()) {
                    backgroundDrawable.setState(getDrawableState());
                }
            } else {
                backgroundDrawable = null;
            }

            Drawable background;
            GradientDrawable contentDrawable = new GradientDrawable();
            contentDrawable.setColor(Color.TRANSPARENT);

            if (navigationRippleColorStateList != null) {
                GradientDrawable maskDrawable = new GradientDrawable();
                // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
                // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
                maskDrawable.setCornerRadius(0.00001F);
                maskDrawable.setColor(Color.WHITE);

                @SuppressLint("RestrictedApi")
                ColorStateList rippleColor = RippleUtils.convertToRippleDrawableColor(navigationRippleColorStateList);

                // TODO: Add support to RippleUtils.compositeRippleColorStateList for different ripple color
                // for selected items vs non-selected items
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    background =
                            new RippleDrawable(
                                    rippleColor,
                                    unboundedRipple ? null : contentDrawable,
                                    unboundedRipple ? null : maskDrawable);
                } else {
                    Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
                    DrawableCompat.setTintList(rippleDrawable, rippleColor);
                    background = new LayerDrawable(new Drawable[]{contentDrawable, rippleDrawable});
                }
            } else {
                background = contentDrawable;
            }
            ViewCompat.setBackground(this, background);
            this.invalidate();
        }

        /**
         * 将navigationBackground属性指定的背景drawable绘制到提供的画布上
         */
        private void drawBackground(Canvas canvas) {
            if (backgroundDrawable != null) {
                backgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
                backgroundDrawable.draw(canvas);
            }
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            boolean changed = false;
            int[] state = getDrawableState();
            if (backgroundDrawable != null && backgroundDrawable.isStateful()) {
                changed |= backgroundDrawable.setState(state);
            }

            if (changed) {
                invalidate();
                BottomNavigationLayout.this.invalidate();
            }
        }

        /**
         * 执行导航的点击事件
         */
        @Override
        public boolean performClick() {
            final boolean handled = super.performClick();
            if (navigation != null) {
                if (!handled) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                }
                navigation.select();
                return true;
            } else {
                return handled;
            }
        }

        @Override
        public void setSelected(final boolean selected) {
            final boolean changed = isSelected() != selected;
            super.setSelected(selected);

            if (changed && selected && Build.VERSION.SDK_INT < 16) {
                // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            }

            // Always dispatch this to the child views, regardless of whether the value has
            // changed
            if (textView != null) {
                textView.setSelected(selected);
            }
            if (iconView != null) {
                iconView.setSelected(selected);
            }
        }

        /**
         * 设置Navigation到NavigationView并更新NavigationView
         */
        void setNavigation(@Nullable final Navigation navigation) {
            if (this.navigation != navigation) {
                this.navigation = navigation;
                update();
            }
        }

        /**
         * 重置NavigationView(将Navigation置为null并取消选中状态)
         */
        void reset() {
            setNavigation(null);
            setSelected(false);
        }

        /**
         * 更新NavigationView
         */
        final void update() {
            final Navigation navigation = getNavigation();
            final View custom = navigation != null ? navigation.getCustomView() : null;
            if (custom != null) {
                final ViewParent customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        ((ViewGroup) customParent).removeView(custom);
                    }
                    addView(custom);
                }
                customView = custom;
                if (this.textView != null) {
                    this.textView.setVisibility(GONE);
                }
                if (this.iconView != null) {
                    this.iconView.setVisibility(GONE);
                    this.iconView.setImageDrawable(null);
                }

                customTextView = custom.findViewById(android.R.id.text1);
                if (customTextView != null) {
                    customTextView.setMaxLines(1);
                }
                customIconView = custom.findViewById(android.R.id.icon);
            } else {
                // We do not have a custom view. Remove one if it already exists
                if (customView != null) {
                    removeView(customView);
                    customView = null;
                }
                customTextView = null;
                customIconView = null;
            }

            if (customView == null) {
                // If there isn't a custom view, we'll us our own in-built layouts
                if (this.iconView == null) {
                    iconView = new ImageView(getContext());
                    iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    addView(iconView, 0, new ViewGroup.LayoutParams(navigationIconSize, navigationIconSize));
                }

                final Drawable icon = navigation != null ? navigation.getIcon() : null;
                this.wrappedIcon = icon;
                if (icon != null) {
                    Drawable.ConstantState state = icon.getConstantState();
                    this.wrappedIcon = DrawableCompat.wrap(state == null ? icon : state.newDrawable()).mutate();
                    DrawableCompat.setTintList(wrappedIcon, navigationIconTint);
                    if (navigationIconTintMode != null) {
                        DrawableCompat.setTintMode(wrappedIcon, navigationIconTintMode);
                    }
                }

                if (this.textView == null) {
                    textView = new TextView(getContext());
                    textView.setGravity(Gravity.CENTER);
                    textView.setMaxLines(1);
                    addView(textView, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                }
                TextViewCompat.setTextAppearance(this.textView, navigationTextAppearance);
                if (navigationTextColor != null) {
                    this.textView.setTextColor(navigationTextColor);
                    this.textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, navigationTextSize);
                }
                updateTextAndIcon(this.textView, this.iconView);
            } else {
                // Else, we'll see if there is a TextView or ImageView present and update them
                if (customTextView != null || customIconView != null) {
                    updateTextAndIcon(customTextView, customIconView);
                }
            }

            if (navigation != null && !TextUtils.isEmpty(navigation.contentDesc)) {
                // Only update the NavigationView's content description from Navigation if the Navigation's content description
                // has been explicitly set.
                setContentDescription(navigation.contentDesc);
            }
            // Finally update our selected state
            setSelected(navigation != null && navigation.isSelected());
        }

        private void updateTextAndIcon(@Nullable final TextView textView, @Nullable final ImageView iconView) {
            final CharSequence text = navigation != null ? navigation.getText() : null;
            final Drawable icon = wrappedIcon != null ? wrappedIcon : (navigation != null ? navigation.getIcon() : null);

            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    iconView.setVisibility(GONE);
                    iconView.setImageDrawable(null);
                }
            }

            final boolean hasText = !TextUtils.isEmpty(text);
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    textView.setVisibility(VISIBLE);
                    setVisibility(VISIBLE);
                } else {
                    textView.setVisibility(GONE);
                    textView.setText(null);
                }
            }

            final CharSequence contentDesc = navigation != null ? navigation.contentDesc : null;
            TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
        }

        public Navigation getNavigation() {
            return navigation;
        }

    }

}
