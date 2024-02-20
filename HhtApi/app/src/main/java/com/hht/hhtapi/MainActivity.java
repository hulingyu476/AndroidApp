package com.hht.hhtapi;

import android.content.pm.PackageManager;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private String TAG = "hhtapi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GetPlatformCodecList();
    }

    private void GetPlatformCodecList() {
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] supportCodes = list.getCodecInfos();

        Log.i(TAG,"codec List:");
        for (MediaCodecInfo codec : supportCodes) {
            if(!codec.isEncoder()){
                String name = codec.getName();
                if(name.startsWith("OMX.google")){
                    //Log.i(TAG,"SW codec ->" +name);
                }
            }

            if(!codec.isEncoder()){
                String name = codec.getName();
                if(!name.startsWith("OMX.google")){
                    //Log.i(TAG,"HW codec ->" +name);
                }
            }

            if(codec.isEncoder()){
                String name = codec.getName();
                if(name.startsWith("OMX.google")){
                    //Log.i(TAG,"SW encodec ->" +name);
                }
            }

            if(codec.isEncoder()){
                String name = codec.getName();
                if(!name.startsWith("OMX.google")){
                   // Log.i(TAG,"HW encodec ->" +name);
                }
            }

            if(codec.isEncoder()) {
                String[] Types = codec.getSupportedTypes();
                for (String type : Types) {
                    if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_AVC)) {
                        int count = codec.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC).getMaxSupportedInstances();
                        Log.w(TAG, "AVC encodec name:" + codec.getName() + ";count=" + count);
                    }
                    if (type.equalsIgnoreCase(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                        int count = codec.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_HEVC).getMaxSupportedInstances();
                        Log.w(TAG, "HEVC encodec name:" + codec.getName() + ";count=" + count);
                    }
                }
            }

        }
    }


}