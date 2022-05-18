package com.example.hmtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hmrecorder";
    private File recordFile;
    private static final int DISPLAY_WIDTH = 1080;
    private static final int DISPLAY_HEIGHT = 720;
    private static final int RECORD_REQUEST_CODE = 3839;
    private int mScreenDensity;
    private Button mBtnRecoder;
    boolean isRecording = false;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjectionCallback mMediaProjectionCallback;
    private static final SparseIntArray ORIENTTIONS = new SparseIntArray();

    static {
        ORIENTTIONS.append(Surface.ROTATION_0,90);
        ORIENTTIONS.append(Surface.ROTATION_90,0);
        ORIENTTIONS.append(Surface.ROTATION_180,270);
        ORIENTTIONS.append(Surface.ROTATION_270,180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mMediaRecorder = new MediaRecorder();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        mBtnRecoder = (Button) findViewById(R.id.id_btn_screen_recorder);
        mBtnRecoder.setOnClickListener(v -> isStartRecordScreen());
    }

    private void isStartRecordScreen() {
        if(!isRecording){
            initRecorder();
            recordScreen();
        }else{
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            stopRecordScreen();
        }
    }

    private void initRecorder() {
        try {
            if(mMediaRecorder == null){
                Log.d(TAG,"initRecorder:MediaRecorder is null");
                return;
            }
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            recordFile = getExternalFilesDir("RecordFile");
            boolean mkdirs = recordFile.mkdirs();
            String absolutePath = new File(recordFile + "/"+"record_"+ System.currentTimeMillis() +".mp4").getAbsolutePath();

            mMediaRecorder.setOutputFile(absolutePath);
            mMediaRecorder.setVideoSize(DISPLAY_WIDTH,DISPLAY_HEIGHT);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setVideoFrameRate(12);
            mMediaRecorder.setVideoEncodingBitRate(5*1024*1024);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientataion = ORIENTTIONS.get(rotation + 90);
            mMediaRecorder.setOrientationHint(orientataion);
            mMediaRecorder.prepare();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void recordScreen() {
        if(mMediaProjection == null){
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(),RECORD_REQUEST_CODE);
            return;
        }
        mVirtualDisplay = createVirtualDisplay();
        mMediaRecorder.start();
        isRecording = true;
        changeText();
    }

    private void changeText() {
        if(isRecording){
            mBtnRecoder.setText("stop record");
        }else{
            mBtnRecoder.setText("start record");
        }
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay("ScreenRecorder",DISPLAY_WIDTH,DISPLAY_HEIGHT,mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mMediaRecorder.getSurface(),null,null);
    }

    private void stopRecordScreen() {
        if(mVirtualDisplay == null){
            return;
        }
        mVirtualDisplay .release();
        destroyMediaProjection();
        isRecording = false;
        changeText();
    }

    private void destroyMediaProjection() {
        if(mMediaProjection != null){
            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }
    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            if (isRecording) {
                isRecording = false;
                changeText();
                mMediaRecorder.stop();
                mMediaRecorder.reset();
            }
            mMediaProjection = null;
            stopRecordScreen();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyMediaProjection();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE) {

            if (resultCode != RESULT_OK) {
                Toast.makeText(MainActivity.this, "recorder permission is forbbid", Toast.LENGTH_SHORT).show();
                isRecording = false;
                changeText();
                return;
            }

            mMediaProjectionCallback = new MediaProjectionCallback();
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            mMediaProjection.registerCallback(mMediaProjectionCallback, null);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
            isRecording = true;
            changeText();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isRecording){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("are you sure stop recorder")
                    .setPositiveButton("Stop", (dialog, which) -> {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        stopRecordScreen();
                        finish();
                    }).setNegativeButton("continue", (dialog, which) -> {
                    }).create().show();
        }else{
            finish();
        }
    }
}

