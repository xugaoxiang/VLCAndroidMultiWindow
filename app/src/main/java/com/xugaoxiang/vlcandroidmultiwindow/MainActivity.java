package com.xugaoxiang.vlcandroidmultiwindow;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //禁止屏保
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        String video = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";
        String video1 = "udp://@225.0.0.11:9001";
        String video2 = "rtmp://live.hkstv.hk.lxdns.com/live/hks";
        String video3 = "/storage/usb_storage/DJ/dhxy2.mp4";

        // add feed fragment to view
        this.showFragment(VideoFragment.newInstance(video), R.id.video_container);
        this.showFragment(VideoFragment.newInstance(video1), R.id.video_container1);
        this.showFragment(VideoFragment.newInstance(video2), R.id.video_container2);
        this.showFragment(VideoFragment.newInstance(video3), R.id.video_container3);
    }

    protected void showFragment(Fragment newFragment, int container) {
        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
        transaction.add(container, newFragment);
        transaction.commit();
    }
}
