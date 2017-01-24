package com.hienlong.mocv_8;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundVideoRecorder extends Service implements SurfaceHolder.Callback {

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;
    private SurfaceHolder surfaceHold;
    private Timer t;
    private int vol;

    @Override
    public void onCreate() {
        vol = mute();
        // Start foreground service to avoid unexpected kill
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Background Video Recorder")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(1234, notification);

        // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        surfaceView = new SurfaceView(this);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);
    }

    // Method called right after Surface created (initializing and starting MediaRecorder)
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        surfaceHold = surfaceHolder;

        startRecording();

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
                startRecording();
            }
        }
                , 600000, 600000);
    }

    public int mute()
    {
        AudioManager audioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int oldStreamVolume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);
        audioMgr.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        return oldStreamVolume;
    }

    public void unmute(int volume)
    {
        AudioManager audioMgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioMgr.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
    }

    private void startRecording() {
        camera = Camera.open();
        mediaRecorder = new MediaRecorder();
        camera.unlock();

        mediaRecorder.setPreviewDisplay(surfaceHold.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mediaRecorder.setOutputFile(
                Environment.getExternalStorageDirectory()+"/DCIM/MOCV/MOCV_"+
                        DateFormat.format("yyyyMMdd'T'HHmmss", new Date().getTime())+
                        ".mp4"
        );

        try {
            mediaRecorder.prepare();
        }
        catch (Exception e) {}

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();
    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
        t.cancel();
        stopRecording();
        unmute(vol);

        windowManager.removeView(surfaceView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
