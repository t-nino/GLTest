package com.example.gltest;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class MainActivity extends Activity implements OnScaleGestureListener {
 private MainSurface surface = null;
 private ScaleGestureDetector scaleGgesture= null;
 private float scaleFactor = 1;

 // タッチイベント処理
 private float touchedX = 0;
 private float touchedY = 0;

 @Override
 protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  surface = new MainSurface(this);
  surface.setRenderer(surface);
  surface.setFocusable(true);
  surface.setKeepScreenOn(true);
  setContentView(surface);

  //surface.setSurface(R.drawable.ic_launcher);
  surface.setSurface(R.drawable.goo);

  // スレッド起動
  	(new Thread(new Runnable() {
    @Override
	  public void run() {
    	int x = 0;
    	int y = 0;
    	while(true){
    		surface.setRoll(x, y);
    		x = x + 1;
    		y = y + 1;
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
    		if(x >= Float.MAX_VALUE-100){
        		x = 0;
        	}
    		if(y >= Float.MAX_VALUE-100){
        		y = 0;
        	}
    	}

      }

	  })).start();
 }

 @Override
 protected void onResume() {
  super.onResume();

  if (scaleGgesture==null){
   scaleGgesture = new ScaleGestureDetector(getApplicationContext(), this);
  }
 }

 @Override
 public boolean onTouchEvent(MotionEvent event) {
  scaleGgesture.onTouchEvent(event);

  //フリック操作ならカメラ移動
  if (event.getPointerCount()==1){
   switch (event.getAction()) {
   case MotionEvent.ACTION_MOVE:
    float x = event.getX();
    float y = event.getY();

    surface.setCameraPotision((touchedX - x), (touchedY - y));
    touchedX = x;
    touchedY = y;
    break;
   case MotionEvent.ACTION_DOWN:
    touchedX = event.getX();
    touchedY = event.getY();
    break;
   }
  }
  return true;
 }

 @Override
 public boolean onScale(ScaleGestureDetector detector) {
  scaleFactor *= detector.getScaleFactor();
  surface.setScale(scaleFactor);
  return true;
 }

 @Override
 public boolean onScaleBegin(ScaleGestureDetector detector) {
  return true;
 }

 @Override
 public void onScaleEnd(ScaleGestureDetector detector) {
  scaleFactor *= detector.getScaleFactor();
 }

}