package com.xugaoxiang.vlcandroidmultiwindow;

import java.lang.ref.WeakReference;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcUtil;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.NativeCrashHandler;
import org.videolan.libvlc.NativeCrashHandler.OnNativeCrashListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

public class VideoView {
	public static final String TAG = "MultipleVLC";
    private String mFilePath;
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    private LibVLC libvlc;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    private Activity mContext;
    private EventHandler mEventHandler;  
    private FrameLayout mSurfaceFrame;
    
    /*************
     * IVideoPlayer
     *************/
	IVideoPlayer mVideoPlayer = new IVideoPlayer(){
		@Override
		public void eventHardwareAccelerationError() {
	        Log.e(TAG, "Error with hardware acceleration");
	        releasePlayer();
	        Toast.makeText(mContext, "Error with hardware acceleration", Toast.LENGTH_LONG).show();	
		}
		
	    @Override
	    public void setSurfaceLayout(int width, int height, int visible_width,
	            int visible_height, int sar_num, int sar_den) {
	        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
	        msg.sendToTarget();
	    }

	    // Used only for old stuff
	    @Override
	    public int configureSurface(Surface surface, int width, int height, int hal) {
	        Log.d("", "configureSurface: width = " + width + ", height = " + height);
	        if (LibVlcUtil.isICSOrLater() || surface == null)
	            return -1;
	        if (width * height == 0)
	            return 0;
	        if(hal != 0)
	            holder.setFormat(hal);
	        holder.setFixedSize(width, height);
	        return 1;
	    }
	
	};
	
    /*************
     * Surface
     *************/
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {

	    }

	    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
	            int width, int height) {
	        if (libvlc != null)
	            libvlc.attachSurface(holder.getSurface(), mVideoPlayer);
	    }

	    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
	    }
	};

	
    /*************
     * Events
     *************/

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<VideoView> mOwner;

        public MyHandler(VideoView owner) {
            mOwner = new WeakReference<VideoView>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
        	VideoView player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
            case EventHandler.MediaPlayerEndReached:
                player.releasePlayer();
                break;
            case EventHandler.MediaPlayerPlaying:
            case EventHandler.MediaPlayerPaused:
            case EventHandler.MediaPlayerStopped:
            default:
                break;
            }
        }
    }
    

    
    public VideoView(SurfaceView view, String url, Activity ac, EventHandler handler, FrameLayout frame){
    	mSurfaceFrame = frame;
    	mContext  = ac;
    	mFilePath = url;
        mSurface = view;
        holder = mSurface.getHolder();

        holder.addCallback(mSHCallback);
        mEventHandler = handler;
    }
    

	public OnNativeCrashListener nativecrashListener = new OnNativeCrashListener(){

		@Override
		public void onNativeCrash() {
			Log.e("vlcdebug", "nativecrash");
		}
		
	};
	
    public void createPlayer() {
        releasePlayer();
        try {
            String media = mFilePath;

            // Create a new media player
            libvlc = new LibVLC();
            libvlc.init(mContext);
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_FULL);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setVerboseMode(true);
            libvlc.setNetworkCaching(1000);
            NativeCrashHandler.getInstance().setOnNativeCrashListener(nativecrashListener);
          
            
            if(LibVlcUtil.isGingerbreadOrLater())
                libvlc.setVout(LibVLC.VOUT_ANDROID_WINDOW);
            else
                libvlc.setVout(LibVLC.VOUT_ANDROID_SURFACE);
            LibVLC.restartInstance(mContext);
            mEventHandler.addHandler(mHandler);
            holder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
            mute();
        } catch (Exception e) {
        	Log.e(TAG, e.toString());
            Toast.makeText(mContext, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    public void releasePlayer() {
        if (libvlc == null)
            return;
        mEventHandler.removeHandler(mHandler);
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private void setSize(int width, int height) {
    	   if(libvlc!=null){
           	libvlc.closeAout();
           	libvlc.setVolume(0);
           }
           
    	   // Dimensions of the native video
           mVideoWidth = width;
           mVideoHeight = height;
           
           if (mVideoWidth * mVideoHeight <= 1)
               return;
           
           // Dimensions of the device screen
           int deviceW = mContext.getWindow().getDecorView().getWidth();
           int deviceH = mContext.getWindow().getDecorView().getHeight();
           
           // Dimensions of the surface frame
           int surfaceFrameW = mSurfaceFrame.getMeasuredWidth();
           int surfaceFrameH = mSurfaceFrame.getMeasuredHeight();
           
           
           float videoAR = (float) mVideoWidth / (float) mVideoHeight;
           float surfaceFrameAr = (float) surfaceFrameW / (float) surfaceFrameH;

           int vidW = surfaceFrameW;
           int vidH = surfaceFrameH;
           
           if (surfaceFrameAr < videoAR)
               vidH = (int) (surfaceFrameW / videoAR);
           else
               vidW = (int) (surfaceFrameH * videoAR);

           // force surface buffer size
           if(holder!=null){
        	   holder.setFixedSize(mVideoWidth, mVideoHeight);
           	
           }
           else{
        	   Toast.makeText(mContext, "Holder was null!!!", Toast.LENGTH_SHORT); 
           }
           

           // set display size
           LayoutParams lp = mSurface.getLayoutParams();
           lp.width = vidW;
           lp.height = vidH;
           mSurface.setLayoutParams(lp);
           mSurface.invalidate();
    }
    
    public void mute(){
        libvlc.closeAout();
        libvlc.setVolume(0);
    }
}
