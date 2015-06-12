package ru.johnlife.lifetoolsmp3.ui.widget;

import ru.johnlife.lifetoolsmp3.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SwipeUndoView;

public class RippleView extends FrameLayout implements OnGestureListener {

	private static boolean click = false;
	
    private int width;
    private int height;
    private Integer rippleFrameRate = Integer.valueOf(10);
    private Integer rippleDuration = Integer.valueOf(400);
    private Integer rippleAlpha = Integer.valueOf(90);
    private int rippleColor = Color.WHITE;
    private Integer ripplePadding = Integer.valueOf(0);
    private Integer rippleType = Integer.valueOf(0);
    private Integer rippleZoomDuration = Integer.valueOf(200);
    private float rippleZoomScale = 1.03f;
    private boolean rippleClickable = true;
    private boolean rippleAlphaVisible = true;
    
    private Integer timer = Integer.valueOf(0);
    private Integer timerEmpty = Integer.valueOf(0);
    private Integer durationEmpty = Integer.valueOf(-1);
    private float x = Float.NEGATIVE_INFINITY;
    private float y = Float.NEGATIVE_INFINITY;
    private float radiusMax = Float.NaN;
    
    private boolean hasToZoom;
    private boolean isCentered;
    private boolean isLongClick;
    private boolean animationRunning;
    private boolean eventCanceled;
    private boolean singleTap;
    
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap originBitmap;
    private View childView;
    private GestureDetector gestureDetector;
//    private ScaleAnimation scaleAnimation;

    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, Color.TRANSPARENT);
        rippleType = typedArray.getInt(R.styleable.RippleView_rv_type, rippleType);
        rippleDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, rippleDuration);
        rippleFrameRate = typedArray.getInteger(R.styleable.RippleView_rv_framerate, rippleFrameRate);
        rippleAlpha = typedArray.getInteger(R.styleable.RippleView_rv_alpha, rippleAlpha);
        rippleAlphaVisible = typedArray.getBoolean(R.styleable.RippleView_rv_alphaVisible, rippleAlphaVisible);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, ripplePadding);
        rippleZoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, rippleZoomScale);
        rippleZoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, rippleZoomDuration);
        rippleClickable = typedArray.getBoolean(R.styleable.RippleView_rv_clickable, rippleClickable);
        hasToZoom = typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false);
        isCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false);
        typedArray.recycle();
        gestureDetector = new GestureDetector(context, this);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);
        setWillNotDraw(false);
        enableClipPathSupportIfNecessary();
	}
    
	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		if (!animationRunning) return;
		if (timer == 0)
			canvas.save(Canvas.CLIP_SAVE_FLAG);
		if (rippleDuration <= timer * rippleFrameRate) {
			if (!eventCanceled && singleTap) 
				sendClickEvent();
			animationRunning = false;
			timer = 0;
			durationEmpty = -1;
			timerEmpty = 0;
			canvas.restore();
			return;
		} else {
			invalidate();
		}
		canvas.drawCircle(x, y, (radiusMax * (((float) timer * rippleFrameRate) / rippleDuration)), paint);
		if (rippleType == 1 && originBitmap != null && (((float) timer * rippleFrameRate) / rippleDuration) > 0.4f) {
			if (durationEmpty == -1)
				durationEmpty = rippleDuration - timer * rippleFrameRate;
			timerEmpty++;
			final Bitmap tmpBitmap = getCircleBitmap((int) ((radiusMax) * (((float) timerEmpty * rippleFrameRate) / (durationEmpty))));
			canvas.drawBitmap(tmpBitmap, 0, 0, paint);
			tmpBitmap.recycle();
		}
		paint.setColor(rippleColor);
		if (rippleAlphaVisible)
			if (rippleType == 1)
				if ((((float) timer * rippleFrameRate) / rippleDuration) > 0.6f)
					paint.setAlpha((int) (rippleAlpha - ((rippleAlpha) * (((float) timerEmpty * rippleFrameRate) / (durationEmpty)))));
				else
					paint.setAlpha(rippleAlpha);
			else
				paint.setAlpha((int) (rippleAlpha - ((rippleAlpha) * (((float) timer * rippleFrameRate) / rippleDuration))));
		timer++;
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
//		scaleAnimation = new ScaleAnimation(1.0f, rippleZoomScale, 1.0f, rippleZoomScale, width / 2, height / 2);
//		scaleAnimation.setDuration(rippleZoomDuration);
//		scaleAnimation.setRepeatMode(Animation.REVERSE);
//		scaleAnimation.setRepeatCount(1);
    }

	@Override
	public boolean onDown(MotionEvent event) { return false; }

	@Override
	public void onShowPress(MotionEvent event) {}
	
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		if (!eventCanceled)
			singleTapOn(true);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) { return false; }

	@Override
	public void onLongPress(MotionEvent event) {
		if (!eventCanceled) {
			singleTapOn(false);
			sendClickEvent();
		}
	}
	
	private void singleTapOn(boolean on) {
		singleTap = on;
		isLongClick = !on;
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) { return false; }
    
    public void animateRipple(MotionEvent event) {
        createAnimation(event.getX(), event.getY());
    }

    public void animateRipple(final float x, final float y) {
        createAnimation(x, y);
    }

    private void createAnimation(final float x, final float y) {
        if (!animationRunning) {
            if (width <= 0 || height <= 0) return;
			if (hasToZoom) {
				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, rippleZoomScale, 1.0f, rippleZoomScale, width / 2, height / 2);
				scaleAnimation.setDuration(rippleZoomDuration);
				scaleAnimation.setRepeatMode(Animation.REVERSE);
				scaleAnimation.setRepeatCount(1);
				startAnimation(scaleAnimation);
			}
            radiusMax = Math.max(width, height);
            if (rippleType != 2)
                radiusMax /= 2;
            radiusMax -= ripplePadding;
            if (isCentered || rippleType == 1) {
                this.x = getMeasuredWidth() / 2;
                this.y = getMeasuredHeight() / 2;
            } else {
            	this.x = x;
                this.y = y;
            }
            animationRunning = true;
            if (rippleType == 1 && null == originBitmap)
                originBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            invalidate();
        }
    }

	@Override
	public final void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("RippleView can host only one child");
		}
		childView = child;
		super.addView(child, index, params);
	}

	@Override
	public void setOnClickListener(OnClickListener onClickListener) {
		if (childView == null) {
			throw new IllegalStateException("RippleView must have a child view to handle clicks");
		}
		childView.setOnClickListener(onClickListener);
	}
	
	@Override
	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		if (childView == null) {
			throw new IllegalStateException("RippleView must have a child view to handle clicks");
		}
		childView.setOnLongClickListener(onLongClickListener);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(@NonNull final MotionEvent event) {
		if (!isEnabled() || !childView.isEnabled()) return super.onTouchEvent(event);
		gestureDetector.onTouchEvent(event);
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
			childView.onTouchEvent(event);
			isLongClick = false;
			click = false;
			break;
		case MotionEvent.ACTION_DOWN:
			if (click) return true;
			childView.onTouchEvent(event);
			eventCanceled = false;
			singleTap = false;
			click = true;
			if (isInScrollingContainer()) {
				final float x = event.getX();
				final float y = event.getY();
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if (!eventCanceled && childView.isClickable() && rippleClickable) {
							animateRipple(x, y);
							eventCanceled = false;
						}
					}
				}, ViewConfiguration.getTapTimeout());
			} else if (childView.isClickable() && rippleClickable) {
				animateRipple(event);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			childView.onTouchEvent(event);
			eventCanceled = true;
			animationRunning = false;
			isLongClick = false;
			click = false;
			break;
		}
		return true;
	}
	
	private boolean isInScrollingContainer() {
		ViewParent parent = getParent();
		while (null != parent && parent instanceof ViewGroup) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				if (((ViewGroup) parent).shouldDelayChildPressedState()) return true;
			} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				if (((ViewGroup) parent).isScrollContainer()) return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		return !findClickableViewInChild(childView, (int) event.getX(), (int) event.getY());
	}
	
	private boolean findClickableViewInChild(View view, final int x, final int y) {
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				View child = viewGroup.getChildAt(i);
				final Rect rect = new Rect();
				child.getHitRect(rect);
				final boolean contains = rect.contains(x, y);
				if (contains) {
					return findClickableViewInChild(child, x - rect.left, y - rect.top);
				}
			}
		} else if (view != childView) {
			return (view.isEnabled() && (view.isClickable() || view.isLongClickable() || view.isFocusableInTouchMode()));
		}
		return view.isFocusableInTouchMode();
	}
	
	private void enableClipPathSupportIfNecessary() {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			setLayerType(LAYER_TYPE_HARDWARE, null);
		}
	}

	private void sendClickEvent() {
		if (getParent() instanceof AdapterView) {
			final int position = ((AdapterView<?>) getParent()).getPositionForView(this);
			final long id = ((AdapterView<?>) getParent()).getItemIdAtPosition(position);
			if (isLongClick) {
				if (((AdapterView<?>) getParent()).getOnItemLongClickListener() != null)
					((AdapterView<?>) getParent()).getOnItemLongClickListener().onItemLongClick(((ListView) getParent()), this, position, id);
			} else
				((AdapterView<?>) getParent()).performItemClick(this, position, id);			
		} else {
			ViewParent parent = getSwipeParent();
			if (null != parent) {
				final int position = ((AdapterView<?>) parent).getPositionForView(this);
				final long id = ((AdapterView<?>) parent).getItemIdAtPosition(position);
				if (isLongClick) {
					if (((AdapterView<?>) parent).getOnItemLongClickListener() != null)
						((AdapterView<?>) parent).getOnItemLongClickListener().onItemLongClick(((ListView) parent), this, position, id);
				} else
					((AdapterView<?>) parent).performItemClick(this, position, id);
			}
		}
	}
	
	private ViewParent getSwipeParent() {
		ViewParent parent = getParent();
		while (null != parent && parent instanceof ViewGroup) {
			if (parent instanceof SwipeUndoView) {
				return parent.getParent();
			} else {
				parent = parent.getParent();	
			}
		}
		return null;
	}

	private Bitmap getCircleBitmap(final int radius) {
        final Bitmap output = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect((int)(x - radius), (int)(y - radius), (int)(x + radius), (int)(y + radius));
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(x, y, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originBitmap, rect, rect, paint);
        return output;
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
    	super.setEnabled(enabled);
    	rippleClickable = enabled;
    }
    
    @Override
    public void setClickable(final boolean clickable) {
    	super.setClickable(clickable);
    	rippleClickable = clickable;
    }
    
    @Override
    public boolean isInEditMode() {
    	return true;
    }
	
	public void setRippleColor(final int rippleColor) {
		this.rippleColor = rippleColor;
	}

	public int getRippleColor() {
		return rippleColor;
	}
}
