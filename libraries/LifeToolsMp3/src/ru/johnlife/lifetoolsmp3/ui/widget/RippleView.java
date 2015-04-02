package ru.johnlife.lifetoolsmp3.ui.widget;

import ru.johnlife.lifetoolsmp3.R;
import android.annotation.TargetApi;
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
import android.os.Handler;
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

public class RippleView extends FrameLayout implements OnGestureListener {

    private int width;
    private int height;
    private int rippleFrameRate = 10;
    private int rippleDuration = 400;
    private int rippleAlpha = 90;
    private int rippleColor = Color.WHITE;
    private int ripplePadding = 0;
    private int rippleType;
    private int rippleZoomDuration;
    private float rippleZoomScale;
    
    private int timer = 0;
    private int timerEmpty = 0;
    private int durationEmpty = -1;
    private float x = -1;
    private float y = -1;
    private float radiusMax = 0;
    
    private boolean hasToZoom;
    private boolean isCentered;
    private boolean isLongClick;
    private boolean animationRunning;
    private boolean eventCanceled;
    
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap originBitmap;
    private View childView;
    private GestureDetector gestureDetector;
    private ScaleAnimation scaleAnimation;
    private Handler canvasHandler;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

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
        if (isInEditMode())
        	return;
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, Color.TRANSPARENT);
        rippleType = typedArray.getInt(R.styleable.RippleView_rv_type, 0);
        rippleDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, rippleDuration);
        rippleFrameRate = typedArray.getInteger(R.styleable.RippleView_rv_framerate, rippleFrameRate);
        rippleAlpha = typedArray.getInteger(R.styleable.RippleView_rv_alpha, rippleAlpha);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0);
        rippleZoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, 1.03f);
        rippleZoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, 200);
        hasToZoom = typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false);
        isCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false);
        typedArray.recycle();
        canvasHandler = new Handler();
        gestureDetector = new GestureDetector(context, this);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);
        setWillNotDraw(false);
		setClickable(true);
	}

	@Override
	public void draw(@NonNull Canvas canvas) {
		super.draw(canvas);
		if (!animationRunning)
			return;
		if (rippleDuration <= timer * rippleFrameRate) {
			animationRunning = false;
			timer = 0;
			durationEmpty = -1;
			timerEmpty = 0;
			canvas.restore();
			invalidate();
			return;
		} else {
			canvasHandler.postDelayed(runnable, rippleFrameRate);
		}
		if (rippleDuration > timer * rippleFrameRate) {
			canvasHandler.postDelayed(runnable, rippleFrameRate);
		}
		if (timer == 0) {
			canvas.save();
		}
		canvas.drawCircle(x, y, (radiusMax * (((float) timer * rippleFrameRate) / rippleDuration)), paint);
		if (rippleType == 1 && originBitmap != null	&& (((float) timer * rippleFrameRate) / rippleDuration) > 0.4f) {
			if (durationEmpty == -1)
				durationEmpty = rippleDuration - timer * rippleFrameRate;
			timerEmpty++;
			final Bitmap tmpBitmap = getCircleBitmap((int) ((radiusMax) * (((float) timerEmpty * rippleFrameRate) / (durationEmpty))));
			canvas.drawBitmap(tmpBitmap, 0, 0, paint);
			tmpBitmap.recycle();
		}
		paint.setColor(rippleColor);
		if (rippleType == 1) {
			if ((((float) timer * rippleFrameRate) / rippleDuration) > 0.6f)
				paint.setAlpha((int) (rippleAlpha - ((rippleAlpha) * (((float) timerEmpty * rippleFrameRate) / (durationEmpty)))));
			else
				paint.setAlpha(rippleAlpha);
		} else
			paint.setAlpha((int) (rippleAlpha - ((rippleAlpha) * (((float) timer * rippleFrameRate) / rippleDuration))));
		timer++;
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        System.out.println("!!! widht="+width+" height="+height);
        scaleAnimation = new ScaleAnimation(1.0f, rippleZoomScale, 1.0f, rippleZoomScale, w / 2, h / 2);
        scaleAnimation.setDuration(rippleZoomDuration);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
    }

	@Override
	public boolean onDown(MotionEvent event) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent event) { }

	
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		isLongClick = true;
		sendClickEvent();
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
		return false;
	}
    
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
            if (rippleType == 1 && originBitmap == null)
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
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (!isEnabled() || !childView.isEnabled()) {
			return super.onTouchEvent(event);
		}
		gestureDetector.onTouchEvent(event);
		childView.onTouchEvent(event);
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_UP:
			if (!isLongClick) {
				sendClickEvent();
			}
			isLongClick = false;
			break;
		case MotionEvent.ACTION_DOWN:
			eventCanceled = false;
			removeCallbacks(runnable);
			if (isInScrollingContainer()) {
				final float x = event.getX();
				final float y = event.getY();
				postDelayed(new Runnable() {
					
					@Override
					public void run() {
						if (!eventCanceled) {
							animateRipple(x, y);
							eventCanceled = false;
						}
					}
				}, ViewConfiguration.getTapTimeout());
			} else {
				animateRipple(event);
			}
			childView.setPressed(true);
			break;
		case MotionEvent.ACTION_CANCEL:
			eventCanceled = true;
			animationRunning = false;
			removeCallbacks(runnable);
			childView.setPressed(false);
			break;
		}
		return true;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private boolean isInScrollingContainer() {
		ViewParent p = getParent();
		while (p != null && p instanceof ViewGroup) {
			if (((ViewGroup) p).shouldDelayChildPressedState()) {
				return true;
			}
			p = p.getParent();
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return !findClickableViewInChild(childView, (int) event.getX(), (int) event.getY());
	}
	
	private boolean findClickableViewInChild(View view, int x, int y) {
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

	private void sendClickEvent() {
		if (getParent() instanceof AdapterView) {
			final int position = ((AdapterView<?>) getParent()).getPositionForView(this);
			final long id = ((AdapterView<?>) getParent()).getItemIdAtPosition(position);
			if (isLongClick) {
				if (((AdapterView<?>) getParent()).getOnItemLongClickListener() != null)
					((AdapterView<?>) getParent()).getOnItemLongClickListener().onItemLongClick(((ListView) getParent()), this, position, id);
			} else {
				((AdapterView<?>) getParent()).performItemClick(this, position, id);
			}
		}
	}

    private Bitmap getCircleBitmap(final int radius) {
        final Bitmap output = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect((int)(x - radius), (int)(y - radius), (int)(x + radius), (int)(y + radius));
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(x, y, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(originBitmap, rect, rect, paint);
        return output;
    }
	
	public void setRippleColor(int rippleColor) {
		this.rippleColor = rippleColor;
	}

	public int getRippleColor() {
		return rippleColor;
	}
}
