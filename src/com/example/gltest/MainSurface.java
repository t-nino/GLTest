package com.example.gltest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class MainSurface extends GLSurfaceView implements Renderer{

	//デフォルトで表示する画像の数

	private int surface;

	  private static int horizon = 4;
	  private static int vertical = 4;

	  private static int vertical_max = 4;

	  private static final int MAX_IMAGE_NUM = horizon * vertical;


	  private int[] imageIDs = new int[MAX_IMAGE_NUM];

	  //画像1辺のデフォルトサイズ
	  private static int IMAGE_PIXCEL = 256;

	  //視野関連
	  private float fovy = 90f;
	  private float aspect;

	  // カメラの位置
	  private float cameraX = 0;
	  private float cameraY = 0;

	  //カメラの移動量
	  private float cameraDist;

	  //デフォルトのカメラの移動量
	  private float cameraDistDef;

	  // 頂点座標
//	  private float apexs[] = new float[] { -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f, };
	  private float apexs[] = new float[] { -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, };

	  // 頂点座標バッファ
	  private FloatBuffer apexBuff;

	  // 頂点テクスチャ
	  private float coords[] = new float[] {
	    // 上下反転
	    0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };
	  // 頂点テクスチャバッファ
	  private FloatBuffer coordsBuff;

	  private int[] textures = new int[MAX_IMAGE_NUM];

	  Bitmap[] images;
	  float scale = 1.0f;
	  int deviceWidth = 0;
	  int deviceHeight = 0;

	   float roll_x = 0f;
	   float roll_y = 0f;


	  void setCameraPotision(float x, float y) {
	   //移動量が極端に大きい場合は無視
	   if (Math.abs(x)> 100 || Math.abs(y)> 100){
	    return;
	   }

	   cameraX +=  x/cameraDist;
	   cameraY += -y/cameraDist;


/*
	   //画像が画面の外に出ない様にブレーキ
	   if (cameraX> 1)cameraX  = 1;
	   if (cameraX<-1)cameraX = -1;
	   if (cameraY> 2)cameraY  = 2;
	   if (cameraY<-2)cameraY = -2;
*/
	  }

	  void setSurface(int surface){
		  this.surface = surface;
	  }

	  void setRoll(float x,float y){
		  roll_x = x;
		  roll_y = y;
	  }

	  void setScale (float scale){
	   this.scale = scale;
System.out.println(scale);
	   this.fovy = 30f / scale;
	   //画像の極端な拡大縮小を防ぐ
	   if(this.fovy > 150) this.fovy = 150;
	   if(this.fovy <  10) this.fovy =  10;

	   //カメラの移動量をスケールにあわせる必要がある
	   cameraDist = cameraDistDef * scale;

	   //動的に横のマス数を変更する。
	  // vertical = Math.min(Math.round(deviceWidth / (IMAGE_PIXCEL*scale)),vertical_max);
//System.out.println("v:" + vertical);

	  }

	  public MainSurface(Context context) {
		  super(context);
	  }

	  public void setImage(Bitmap[] images){
		  this.images = images;
	  }


	  @Override
	  public void onSurfaceCreated(GL10 gl, EGLConfig config) {

System.out.println("surfaceCreated");
System.out.println("surface_width " + deviceWidth);
System.out.println("surface_height " + deviceHeight);


imageIDs[0] = surface;
imageIDs[1] = surface;
imageIDs[2] = surface;
imageIDs[3] = surface;
imageIDs[4] = surface;
imageIDs[5] = surface;
imageIDs[6] = surface;
imageIDs[7] = surface;
imageIDs[8] = surface;
imageIDs[9] = surface;
imageIDs[10] = surface;
imageIDs[11] = surface;
imageIDs[12] = surface;
imageIDs[13] = surface;
imageIDs[14] = surface;
imageIDs[15] = surface;


/*
	   imageIDs[0] = R.drawable.ic_launcher;
	   imageIDs[1] = R.drawable.ic_launcher;
	   imageIDs[2] = R.drawable.ic_launcher;
	   imageIDs[3] = R.drawable.ic_launcher;
	   imageIDs[4] = R.drawable.ic_launcher;
	   imageIDs[5] = R.drawable.ic_launcher;
	   imageIDs[6] = R.drawable.ic_launcher;
	   imageIDs[7] = R.drawable.ic_launcher;
	   imageIDs[8] = R.drawable.ic_launcher;
	   imageIDs[9] = R.drawable.ic_launcher;
	   imageIDs[10] = R.drawable.ic_launcher;
	   imageIDs[11] = R.drawable.ic_launcher;
	   imageIDs[12] = R.drawable.ic_launcher;
	   imageIDs[13] = R.drawable.ic_launcher;
	   imageIDs[14] = R.drawable.ic_launcher;
	   imageIDs[15] = R.drawable.ic_launcher;
*/


	   // バッファの生成
	   apexBuff = makeFloatBuffer(apexs);
	   coordsBuff = makeFloatBuffer(coords);

	   // テクスチャ管理番号割り当て
	   gl.glDeleteTextures(MAX_IMAGE_NUM, textures, 0);
	   gl.glGenTextures(MAX_IMAGE_NUM, textures, 0);

	   for (int i=0; i<MAX_IMAGE_NUM; i++){
	    //画像をロードし
	    //画像が1辺のサイズに収まる大きさに縦横比率を保ったまま伸縮

		   Bitmap srcImage;

		if(images == null){
		  srcImage = BitmapFactory.decodeResource(getResources(),imageIDs[i]);
		}else{
			//画像を外部設定
			if(images.length > i){
				srcImage = images[i];
			}else{
				srcImage = BitmapFactory.decodeResource(getResources(),imageIDs[0]);
			}
		}


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
	   cameraDistDef = cameraDist;

System.out.println("surface_width " + width);
System.out.println("surface height " + height);

	  deviceWidth = width;
	  deviceHeight = height;

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
	   //GLU.gluPerspective(gl, this.fovy, aspect, 0.1f, 100f);
	   GLU.gluPerspective(gl, this.fovy, aspect, 1f, 1000f);

//	   GLU.gluOrtho2D(gl,-400f,400f,-512f,512f);
//	   GLU.gluOrtho2D(gl,0.0f,new Float(deviceWidth).floatValue(),new Float(deviceHeight).floatValue(), 0.0f);

	   //真ん中基準ではなく、左上を基準としたい。
	   float def_pos_y = -deviceHeight/2;
	   float def_pos_x = -deviceWidth/2;

	   // カメラ
	   GLU.gluLookAt(gl, cameraX, cameraY, 3, cameraX, cameraY, 0, 0, 1, 0);



	   //テクスチャ画像を全て表示



	   for(int v=0;v<vertical;v++){
		   for (int h=0; h<horizon ; h++){
			   int i =  v * horizon + h;

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

			    //位置(縦＊横に収める)

			    //gl.glTranslatef(h, -v, 0);
			    gl.glTranslatef(h - 275f/250f , -v + 442f/250f, 0);


	gl.glRotatef(roll_y, 0, 1, 0);
	gl.glRotatef(roll_x, 1, 0, 0);

			    // 描画
			    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

			    // テクスチャーの無効化
			    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			    gl.glDisable(GL10.GL_TEXTURE_2D);
			    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			    gl.glPopMatrix();
		   }
	   }

	   gl.glPopMatrix();
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

}
