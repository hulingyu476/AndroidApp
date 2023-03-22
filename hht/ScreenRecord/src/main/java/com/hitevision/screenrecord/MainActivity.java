package com.hitevision.screenrecord;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "hmrecorder";
    private File recordFile;
    private static final int DISPLAY_WIDTH = 1080;
    private static final int DISPLAY_HEIGHT = 720;
    private static final int RECORD_REQUEST_CODE = 3839;
    private static final int mRequestCode = 1312;
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

    //Manifest.permission.CAPTURE_AUDIO_OUTPUT need system app，temp marked it
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,/*Manifest.permission.CAPTURE_AUDIO_OUTPUT,*/
            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
    List<String> mPermissionlist = new ArrayList<>();

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

        if(Build.VERSION.SDK_INT >= 23){
            //6.0 need to dynamic Permission
            initPermission();
        }
    }

    private void initPermission() {
        //clear non granted permissions
        mPermissionlist.clear();

        //check permissions is grant
        for(int i=0; i<permissions.length; i++){
            if(ContextCompat.checkSelfPermission(this,permissions[i]) != PackageManager.PERMISSION_GRANTED){
                mPermissionlist.add(permissions[i]);  //add non granted permission
            }
        }

        //sumit to request permission
        if(mPermissionlist.size()>0){
            ActivityCompat.requestPermissions(this,permissions,mRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss=false;
        if(mRequestCode == requestCode){
            for(int i=0; i< grantResults.length; i++){
                if(grantResults[i] == -1){
                    hasPermissionDismiss = true;
                }
            }
            if(hasPermissionDismiss){
                showPermissionDialog(); //goto settings
            }
        }
    }

    AlertDialog mPermissionDialog;
    private void showPermissionDialog() {
        if(mPermissionDialog == null){
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("please granted permission")
                    .setCancelable(false)
                    .setPositiveButton("setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            //go to setting page
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",MainActivity.this.getPackageName(),null);
                            intent.setData(uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    })
                    .setNegativeButton("cancle", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
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

