package com.microsoft.activitytracker.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class FloatingActionMenu extends ViewGroup {

    private static final long ANIMATION_DURATION = 300;
    private static final String TAG = "FloatingActionMenu";

    private FloatingActionButton menuButton;
    private List<FloatingActionButton> menuItems;
    private List<Button> menuLabels;
    private List<ItemAnimator> itemAnimators;
    private View backgroundView;

    private AnimatorSet openSet = new AnimatorSet();
    private AnimatorSet closeSet = new AnimatorSet();
    private Animator openOverlay;
    private Animator closeOverlay;


    private boolean isOpen;
    private boolean isAnimating;
    private boolean isCloseOnTouchOutside = true;

    public interface OnMenuToggleListener {
        void onMenuToggle(boolean opened);
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(FloatingActionMenu fam, int index, FloatingActionButton item);


    }

    private OnMenuItemClickListener onMenuItemClickListener;
    private OnMenuToggleListener onMenuToggleListener;

    public FloatingActionMenu(Context context) {
        this(context, null, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        menuItems = new ArrayList<>(5);
        itemAnimators = new ArrayList<>(5);
        menuLabels = new ArrayList<>(5);
    }

    @Override
    protected void onFinishInflate() {
        bringChildToFront(menuButton);
        super.onFinishInflate();
    }

    @Override
    public void addView(@NonNull View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if (getChildCount() > 1) {
            if (child instanceof FloatingActionButton) {
                child.setLayoutParams(params);
                addMenuItem((FloatingActionButton) child);
            }
        } else {
            backgroundView = new View(getContext());
            backgroundView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    createDefaultIconAnimation();
                }

                @Override
                public void onViewDetachedFromWindow(View v) {

                }
            });
            menuButton = (FloatingActionButton) child;
            menuButton.setOnClickListener(v -> toggle());
            addView(backgroundView);
        }
    }

    public void toggle() {
        if (!isOpen) {
            open();
        } else {
            close();
        }
    }

    public void open() {
        startOpenAnimator();
        isOpen = true;
        if (onMenuToggleListener != null) {
            onMenuToggleListener.onMenuToggle(true);
        }
    }

    public void close() {
        startCloseAnimator();
        isOpen = false;
        if (onMenuToggleListener != null) {
            onMenuToggleListener.onMenuToggle(true);
        }
    }

    protected void startCloseAnimator() {
        if (closeOverlay == null) {
            createOverlayAnimations();
        }

        closeSet.start();
        closeOverlay.start();
        for (ItemAnimator anim : itemAnimators) {
            anim.startCloseAnimator();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createOverlayAnimations() {
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int radius = menuButton.getHeight() / 2;

        closeOverlay = ViewAnimationUtils.createCircularReveal(backgroundView,
                menuButton.getLeft() + radius, menuButton.getTop() + radius, Math.max(size.x, size.y),
                radius);
        closeOverlay.setDuration(500);
        closeOverlay.setInterpolator(new AccelerateDecelerateInterpolator());
        closeOverlay.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                backgroundView.setVisibility(GONE);
                animation.end();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        openOverlay = ViewAnimationUtils.createCircularReveal(backgroundView,
                menuButton.getLeft() + radius, menuButton.getTop() + radius, radius,
                Math.max(size.x, size.y));
        openOverlay.setDuration(500);
        openOverlay.setInterpolator(new AccelerateDecelerateInterpolator());
        openOverlay.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                backgroundView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.end();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    protected void startOpenAnimator() {
        createOverlayAnimations();

        openOverlay.start();
        openSet.start();
        for (ItemAnimator anim : itemAnimators) {
            anim.startOpenAnimator();
        }
    }

    public void addMenuItem(FloatingActionButton item) {
        menuItems.add(item);
        itemAnimators.add(new ItemAnimator(item));
        AppCompatButton button = new AppCompatButton(getContext());
        button.setText(item.getContentDescription());
        addView(button);
        menuLabels.add(button);
        item.setTag(button);
        item.setOnClickListener(onItemClickListener);
        button.setOnClickListener(onItemClickListener);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width;
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height;
        final int count = getChildCount();
        int maxChildWidth = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }

        for (int i = 0; i < menuItems.size(); i++) {
            FloatingActionButton fab = menuItems.get(i);
            Button label = menuLabels.get(i);
            maxChildWidth = Math.max(maxChildWidth, label.getMeasuredWidth() + fab.getMeasuredWidth()
                    + fab.getPaddingEnd() + fab.getPaddingStart());
        }

        maxChildWidth = Math.max(menuButton.getMeasuredWidth(), maxChildWidth);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }
        else {
            width = maxChildWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        }
        else {
            int heightSum = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                heightSum += child.getMeasuredHeight() + child.getPaddingBottom();
            }
            height = heightSum;
        }

        setMeasuredDimension(resolveSize(width, widthMeasureSpec),
                resolveSize(height, heightMeasureSpec));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (isCloseOnTouchOutside) {
            return mGestureDetector.onTouchEvent(event);
        }
        else {
            return super.onTouchEvent(event);
        }
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(),
        new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return isCloseOnTouchOutside && isOpened();
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                close();
                return true;
            }
        });

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {

            backgroundView.setBackgroundColor(Color.parseColor("#7F2a3441"));
            backgroundView.layout(l, 0, r, b);

            int right = this.getMeasuredWidth() - menuButton.getPaddingEnd();
            int bottom = this.getMeasuredHeight() - menuButton.getPaddingBottom();
            int dimension = this.getResources().getDimensionPixelSize(
                    android.support.design.R.dimen.design_fab_size_normal);

            int sdk = Build.VERSION.SDK_INT;
            if(sdk < 21) {
                int height = (menuButton.getMeasuredHeight() - dimension);
                int width = (menuButton.getMeasuredWidth() - dimension);

                right = right + width;
                bottom = bottom + height;
            }

            int top = bottom - menuButton.getMeasuredHeight();

            menuButton.layout(right - menuButton.getMeasuredWidth(), top, right, bottom);


            for (int i = 0; i < menuItems.size(); i++) {
                FloatingActionButton item = menuItems.get(i);
                int height = item.getMeasuredHeight();
                int width = item.getMeasuredWidth();
                int paddingBottom = item.getPaddingBottom();
                int paddingStart = item.getPaddingStart();

                if (sdk < 21) {
                    int shadowHeight = (item.getMeasuredHeight() - dimension) / 2;
                    int shadowWidth = (item.getMeasuredWidth() - dimension);

                    paddingBottom = paddingBottom - (shadowHeight * 3);
                    paddingStart = paddingStart - shadowWidth;
                }

                Button label = menuLabels.get(i);
                bottom = top - paddingBottom;
                top = bottom - height;

                item.layout(right - width, top, right, bottom);

                int center = (height - label.getMeasuredHeight()) / 2;
                label.layout(item.getLeft() - paddingStart - label.getMeasuredWidth(),
                        item.getTop() + center , item.getLeft() - paddingStart,
                        item.getBottom() - center);

                if (!isAnimating) {
                    if (!isOpen) {
                        item.setTranslationY(menuButton.getTop() - item.getTop());
                        item.setVisibility(GONE);
                        label.setVisibility(GONE);
                        backgroundView.setVisibility(GONE);
                    } else {
                        item.setTranslationY(0);
                        item.setVisibility(VISIBLE);
                        label.setVisibility(VISIBLE);
                        backgroundView.setVisibility(VISIBLE);
                    }
                }
            }

            if (!isAnimating && getBackground() != null) {
                if (!isOpen) {
                    getBackground().setAlpha(0);
                } else {
                    getBackground().setAlpha(0xff);
                }
            }
        }
    }

    private void createDefaultIconAnimation() {
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        ObjectAnimator collapseAnimator = ObjectAnimator.ofFloat(menuButton, "rotation", 135f, 0f);
        ObjectAnimator expandAnimator = ObjectAnimator.ofFloat(menuButton, "rotation", 0f, 135f);


        if (Build.VERSION.SDK_INT >= 21) {
            openSet.playTogether(expandAnimator);
            closeSet.playTogether(collapseAnimator);
        }
        else {

            ValueAnimator hideBackgroundAnimator = ObjectAnimator.ofInt(0xff, 0);
            hideBackgroundAnimator.addUpdateListener(animation -> {
                Integer alpha = (Integer) animation.getAnimatedValue();
                getBackground().setAlpha(alpha > 0xff ? 0xff : alpha);
            });
            ValueAnimator showBackgroundAnimator = ObjectAnimator.ofInt(0, 0xff);
            showBackgroundAnimator.addUpdateListener(animation -> {
                Integer alpha = (Integer) animation.getAnimatedValue();
                getBackground().setAlpha(alpha > 0xff ? 0xff : alpha);
            });

            openSet.playTogether(expandAnimator, showBackgroundAnimator);
            closeSet.playTogether(collapseAnimator, hideBackgroundAnimator);
        }

        openSet.setInterpolator(DEFAULT_OPEN_INTERPOLATOR);
        closeSet.setInterpolator(DEFAULT_CLOSE_INTERPOLATOR);

        openSet.setDuration(ANIMATION_DURATION);
        closeSet.setDuration(ANIMATION_DURATION);

        openSet.addListener(listener);
        closeSet.addListener(listener);
    }

    static final TimeInterpolator DEFAULT_OPEN_INTERPOLATOR = new OvershootInterpolator();
    static final TimeInterpolator DEFAULT_CLOSE_INTERPOLATOR = new AnticipateInterpolator();

    public boolean isOpened() {
        return isOpen;
    }

    private class ItemAnimator implements Animator.AnimatorListener {
        private View view;
        private boolean playingOpenAnimator;

        public ItemAnimator(View v) {
            v.animate()
                .setListener(this);
            view = v;
        }

        public void startOpenAnimator() {
            view.animate()
                .cancel();
            playingOpenAnimator = true;
            view.animate()
                .translationY(0)
                .setInterpolator(DEFAULT_OPEN_INTERPOLATOR).start();
        }

        public void startCloseAnimator() {
            view.animate()
                .cancel();
            playingOpenAnimator = false;
            view.animate()
                .translationY((menuButton.getTop() - view.getTop()))
                .setInterpolator(DEFAULT_CLOSE_INTERPOLATOR).start();
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (playingOpenAnimator) {
                view.setVisibility(VISIBLE);
            }
            else {
                ((Button) view.getTag()).setVisibility(GONE);
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!playingOpenAnimator) {
                view.setVisibility(GONE);
            }
            else {
                ((Button) view.getTag()).setVisibility(VISIBLE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putBoolean("mOpen", isOpen);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            isOpen = bundle.getBoolean("mOpen");
            state = bundle.getParcelable("instanceState");
        }

        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            super.setBackground(background);
        }
        else {
            throw new IllegalArgumentException("floating only support color background");
        }
    }

    private OnClickListener onItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            close();
            if (v instanceof FloatingActionButton) {
                int i = menuItems.indexOf(v);
                if (onMenuItemClickListener != null) {
                    onMenuItemClickListener.onMenuItemClick(FloatingActionMenu.this, i, (FloatingActionButton) v);
                }
            }
//            else if (v instanceof Button) {
//                int i = menuLabels.indexOf(v);
//                if (onMenuItemClickListener != null) {
//                    onMenuItemClickListener.onMenuItemClick(FloatingActionMenu.this, i, menuItems.get(i));
//                }
//            }
        }
    };

    public OnMenuToggleListener getOnMenuToggleListener() {
        return onMenuToggleListener;
    }

    public void setOnMenuToggleListener(OnMenuToggleListener onMenuToggleListener) {
        this.onMenuToggleListener = onMenuToggleListener;
    }

    public OnMenuItemClickListener getOnMenuItemClickListener() {
        return onMenuItemClickListener;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }
}