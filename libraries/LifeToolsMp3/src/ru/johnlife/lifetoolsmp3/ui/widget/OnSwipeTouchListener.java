package ru.johnlife.lifetoolsmp3.ui.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void onSwipeLeft(View v) {
    }

    public void onSwipeRight(View v) {
    }
    
    public void onClick() {
    	
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;
        private View view;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
//            android.util.Log.d("logd", "onFling: start-------------------------------------");
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//            	 android.util.Log.d("logd", "onFling: in");
            	if (distanceX > 0)
                    onSwipeRight(getView());
                else
                    onSwipeLeft(getView());
                return true;
            } else {
            	android.util.Log.d("logd", "onFling: ");
            	onClick();
            }
//            android.util.Log.d("logd", "onFling: end-------------------------------------");
            return false;
        }

		public View getView() {
			return view;
		}

		public void setView(View view) {
			this.view = view;
		}
    }
}