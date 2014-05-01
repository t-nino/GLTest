package com.example.gltest;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class GLViewer extends Activity implements OnScaleGestureListener {
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
/*
 // OpenGLのViewクラス
 private class MainSurface extends GLSurfaceView implements Renderer {
  //表示する画像の数
  private static final int MAX_IMAGE_NUM = 2;
  private int[] imageIDs = new int[MAX_IMAGE_NUM];

  //画像1辺のサイズ
  private static final int IMAGE_PIXCEL = 512;

  //視野関連
  private float fovy = 45f;
  private float aspect;

  // カメラの位置
  private float cameraX = 0;
  private float cameraY = 0;

  //カメラの移動量
  private float cameraDist;

  // 頂点座標
  private float apexs[] = new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f,
    1f, };
  // 頂点座標バッファ
  private FloatBuffer apexBuff;

  // 頂点テクスチャ
  private float coords[] = new float[] {
    // 上下反転
    0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
  // 頂点テクスチャバッファ
  private FloatBuffer coordsBuff;

  private int[] textures = new int[MAX_IMAGE_NUM];

  void setCameraPotision(float x, float y) {
   //移動量が極端に大きい場合は無視
   if (Math.abs(x)> 100 || Math.abs(y)> 100){
    return;
   }

   cameraX +=  x/cameraDist;
   cameraY += -y/cameraDist;

   //画像が画面の外に出ない様にブレーキ
   if (cameraX> 1)cameraX  = 1;
   if (cameraX<-1)cameraX = -1;
   if (cameraY> 2)cameraY  = 2;
   if (cameraY<-2)cameraY = -2;
  }

  void setScale (float scale){
   this.fovy = 45f / scale;
   //画像の極端な拡大縮小を防ぐ
   if(this.fovy > 150) this.fovy = 150;
   if(this.fovy <  10) this.fovy =  10;
  }

  public MainSurface(Context context) {
   super(context);
  }

  //修正後
  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
   imageIDs[0] = R.drawable.ic_launcher;
   imageIDs[1] = R.drawable.ic_launcher;

   // バッファの生成
   apexBuff = makeFloatBuffer(apexs);
   coordsBuff = makeFloatBuffer(coords);

   // テクスチャ管理番号割り当て
   gl.glDeleteTextures(MAX_IMAGE_NUM, textures, 0);
   gl.glGenTextures(MAX_IMAGE_NUM, textures, 0);

   for (int i=0; i<MAX_IMAGE_NUM; i++){
    //画像をロードし
    //画像が1辺のサイズに収まる大きさに縦横比率を保ったまま伸縮
    Bitmap srcImage = BitmapFactory.decodeResource(getResources(),imageIDs[i]);

    float width = (float) srcImage.getWidth();
    float height= (float) srcImage.getHeight();
    float scale;
    float left;
    float top;
    if (width > height){
     scale = IMAGE_PIXCEL / width;
     left = 0;
     top = ((width - height) / 2) * scale;
    }else{
     scale = IMAGE_PIXCEL / height;
     top = 0;
     left = ((height - width) / 2) * scale;
    }


    Matrix matrix = new Matrix();
    matrix.postScale(scale, scale);
    Bitmap stretchedImage = Bitmap.createBitmap(srcImage, 0, 0, (int)width, (int)height, matrix, true);
    srcImage.recycle();

    //画像を正方形に納める
    Bitmap dstImage = Bitmap.createBitmap(IMAGE_PIXCEL, IMAGE_PIXCEL, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(dstImage);
    canvas.drawBitmap(stretchedImage, left, top, null);
    stretchedImage.recycle();

    // テクスチャ管理番号バインド
    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);

    // 画像ファイルをテクスチャにバインド
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, dstImage, 0);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
    dstImage.recycle();
   }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
   cameraDist =(float)(width/2);

   gl.glViewport(0, 0, width, height);
   gl.glMatrixMode(GL10.GL_PROJECTION);
   gl.glLoadIdentity();
   aspect = (float) width / (float) height;

   // 背面塗り潰し色の指定
   gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);// 黒
  }

  @Override
  public void onDrawFrame(GL10 gl) {
   // 画面クリア
   gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

   gl.glPushMatrix();

   //視野
   GLU.gluPerspective(gl, this.fovy, aspect, 0.1f, 100f);

   // カメラ
   GLU.gluLookAt(gl, cameraX, cameraY, 3, cameraX, cameraY, 0, 0, 1, 0);

   //テクスチャ画像を全て表示
   for (int i=0; i<MAX_IMAGE_NUM; i++){
    gl.glPushMatrix();

    // テクスチャーの有効化
    gl.glEnable(GL10.GL_TEXTURE_2D);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    // 頂点配列の指定
    gl.glVertexPointer(2, GL10.GL_FLOAT, 0, apexBuff);
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

    // テクスチャ配列の指定
    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordsBuff);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);

    //位置
    gl.glTranslatef(0, -i*2, 0);

    // 描画
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

    // テクスチャーの無効化
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glDisable(GL10.GL_TEXTURE_2D);
    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

    gl.glPopMatrix();
   }

   gl.glPopMatrix();
  }
 }

 //頂点の配列をバッファーに変換するメソッド
 private static FloatBuffer makeFloatBuffer(float[] values) {
  ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 4);
  bb.order(ByteOrder.nativeOrder());
  FloatBuffer fb = bb.asFloatBuffer();
  fb.put(values);
  fb.position(0);
  return fb;
 }
 */
}