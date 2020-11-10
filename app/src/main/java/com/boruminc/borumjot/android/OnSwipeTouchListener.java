package com.boruminc.borumjot.android;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Detects left and right swipes across a view.
 * Taken from StackOverflow answer by <a href="https://stackoverflow.com/users/145173/edward-brey">Edward Brey</a>
 * Source: <a href="https://stackoverflow.com/a/19506010/9860982">Android: How to handle right to left swipe gestures</a>
 */
public class OnSwipeTouchListener implements View.OnTouchListener {
    private int windowWidth;
    private final GestureDetector gestureDetector;

    OnSwipeTouchListener(Context context, int ww) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        windowWidth = ww;
    }

    public void onSwipeLeft() {}

    public void onSwipeRight() {}

    public boolean onTouch(View v, MotionEvent event) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            // Change the view horizontally with the user's touch x coordinate
            layoutParams.leftMargin = Math.min((int) event.getRawX(), windowWidth);

            v.setLayoutParams(layoutParams);
        }

        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }
    }
}
