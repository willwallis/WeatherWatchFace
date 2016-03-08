/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWeatherWatchFace extends CanvasWatchFaceService {
    public final String LOG_TAG = MyWeatherWatchFace.class.getSimpleName();

    float mXCenter;
    float mYCenter;
    private static final String KEY_ONE = "weatherId";
    private static final String KEY_TWO = "high";
    private static final String KEY_THREE = "low";
    int weatherId = 0;
    String mHighTemp = String.format("%s°", "--");
    String mLowTemp = String.format("%s°", "--");
    private final int LIGHTLIGHT = 0;
    private final int LIGHTDARK = 1;
    private final int DARKLIGHT = 2;
    private final int DARKDARK = 3;


    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWeatherWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWeatherWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWeatherWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;
        Bitmap mWeatherBitmap;
        Paint mTextPaint;
        Paint mTextHigh;
        Paint mTextLow;
        boolean mAmbient;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        float mXOffset;
        float mYOffset;
        float mYTemp;
        float mXPic;
        float mYPic;
        float mXHigh;
        float mXLow;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWeatherWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = MyWeatherWatchFace.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));

            Drawable backgroundDrawable = resources.getDrawable(R.drawable.cloudy_blue_sky);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
            Drawable weatherDrawable = resources.getDrawable(R.drawable.ic_clear);
            mWeatherBitmap = ((BitmapDrawable) weatherDrawable).getBitmap();

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.light_text));

            mTextHigh = new Paint();
            mTextHigh = createTextPaint(resources.getColor(R.color.light_text));

            mTextLow = new Paint();
            mTextLow = createTextPaint(resources.getColor(R.color.light_text));


            mTime = new Time();

            LocalBroadcastManager.getInstance(MyWeatherWatchFace.this).registerReceiver(
                    mMessageReceiver, new IntentFilter("update-watch-face"));
        }

        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                // Get extra data included in the Intent
                weatherId = intent.getIntExtra(KEY_ONE, 0);
                Log.v("receiver", "Got weather: " + weatherId);
                mHighTemp = String.format("%s°", (int)intent.getDoubleExtra(KEY_TWO, 2));
                mLowTemp = String.format("%s°", (int) intent.getDoubleExtra(KEY_THREE, 4));

                // Allows for manual override for testing
                int weatherTest = weatherId;

                Resources resources = MyWeatherWatchFace.this.getResources();
                Drawable weatherDrawable = resources.getDrawable(Utility.getIconResourceForWeatherCondition(weatherTest));
                mWeatherBitmap = ((BitmapDrawable) weatherDrawable).getBitmap();

                Drawable backgroundDrawable = resources.getDrawable(Utility.getImageUrlForWeatherCondition(weatherTest));
                mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
                mBackgroundScaledBitmap = null;

                // Sets text to be readable against background
                switch (Utility.getTextColor(weatherTest)){
                    case LIGHTLIGHT:
                        mTextPaint.setColor(getResources().getColor(R.color.light_text));
                        mTextHigh.setColor(getResources().getColor(R.color.light_text));
                        mTextLow.setColor(getResources().getColor(R.color.light_text));
                        break;
                    case LIGHTDARK:
                        mTextPaint.setColor(getResources().getColor(R.color.light_text));
                        mTextHigh.setColor(getResources().getColor(R.color.dark_text));
                        mTextLow.setColor(getResources().getColor(R.color.dark_text));
                        break;
                    case DARKLIGHT:
                        mTextPaint.setColor(getResources().getColor(R.color.dark_text));
                        mTextHigh.setColor(getResources().getColor(R.color.light_text));
                        mTextLow.setColor(getResources().getColor(R.color.light_text));
                        break;
                    case DARKDARK:
                        mTextPaint.setColor(getResources().getColor(R.color.dark_text));
                        mTextHigh.setColor(getResources().getColor(R.color.dark_text));
                        mTextLow.setColor(getResources().getColor(R.color.dark_text));
                        break;
                }

            }
        };

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            Log.v(LOG_TAG, "onSurfaceChanged");

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mXCenter = width / 2f;
            mYCenter = height / 2f;

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWeatherWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWeatherWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWeatherWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            mYOffset = resources.getDimension(isRound
                    ? R.dimen.digital_y_offset_round : R.dimen.digital_y_offset);
            mXPic = resources.getDimension(isRound
                    ? R.dimen.pic_x_offset_round : R.dimen.pic_x_offset);
            mYPic = resources.getDimension(isRound
                    ? R.dimen.pic_y_offset_round : R.dimen.pic_y_offset);
            mXHigh = resources.getDimension(isRound
                    ? R.dimen.high_x_offset_round : R.dimen.high_x_offset);
            mXLow = resources.getDimension(isRound
                    ? R.dimen.low_x_offset_round : R.dimen.low_x_offset);
            mYTemp = resources.getDimension(isRound
                    ? R.dimen.temp_y_offset_round : R.dimen.temp_y_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
            float textSizeHigh = resources.getDimension(isRound
                    ? R.dimen.high_text_size_round : R.dimen.high_text_size);
            float textSizeLow = resources.getDimension(isRound
                    ? R.dimen.low_text_size_round : R.dimen.low_text_size);

            mTextPaint.setTextSize(textSize);
            mTextHigh.setTextSize(textSizeHigh);
            mTextLow.setTextSize(textSizeLow);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null || mBackgroundScaledBitmap.getWidth() != bounds.width() || mBackgroundScaledBitmap.getHeight() != bounds.height())
            {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        bounds.width(), bounds.height(), true /* filter */);
            }

            canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            // Temp
            canvas.drawText(mHighTemp, mXHigh, mYTemp, mTextHigh);
            canvas.drawText(mLowTemp, mXLow, mYTemp, mTextLow);
            canvas.drawBitmap(mWeatherBitmap, mXPic, mYPic, null);




        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
