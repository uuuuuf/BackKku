package com.soulstring94.backkku;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MyService extends AccessibilityService {

    WindowManager windowManager;
    View view;

    private float prevX;
    private float prevY;

    WindowManager.LayoutParams params;

    public ImageButton button;

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                300,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.LEFT | Gravity.TOP;
        view = inflater.inflate(R.layout.service_view, null);
        button = view.findViewById(R.id.button);

        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setVisibility(View.GONE);

        button.setOnClickListener(view -> {
            try {
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    prevX = event.getRawX();
                    prevY = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float rawX = event.getRawX();
                    float rawY = event.getRawY();

                    float x = rawX - prevX;
                    float y = rawY - prevY;

                    setCoordinateUpdate(x, y);

                    prevX = rawX;
                    prevY = rawY;
                    break;
            }
            return false;
        });

        try {
            windowManager.addView(view, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCoordinateUpdate(float x, float y) {
        if(params != null) {
            params.x += (int) x;
            params.y += (int) y;

            windowManager.updateViewLayout(view, params);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        button.setVisibility(View.GONE);
        if(windowManager != null) {
            if(view != null) {
                windowManager.removeView(view);
                view = null;
            }
            windowManager = null;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        button.setVisibility(View.VISIBLE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if(extras.getString("image_path") != null) {
            String imagePath = extras.getString("image_path");
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            button.setImageBitmap(bitmap);
        } else if(extras.getBoolean("toggleFlag")) {
            button.setVisibility(View.VISIBLE);
        } else if(!extras.getBoolean("toggleFlag")) {
            button.setVisibility(View.GONE);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}