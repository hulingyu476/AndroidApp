package com.hht.hhtapi;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private String TAG = "hhtapi";
    private static NsdManager nsdManager;
    private static String SERVICE_TYPE = "_hht_nsd._tcp";
    private NsdManager.DiscoveryListener mDiscoverListener;
    private NsdManager.ResolveListener mDResolveListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GetPlatformCodecList();

        nsdManager = (NsdManager) getSystemService(NSD_SERVICE);
        createDiscoverListener();

    }

    private void createDiscoverListener() {
        Log.i(TAG,"createDiscoverListener >>");
        mDResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int i) {
                Toast.makeText(MainActivity.this, "onResolverFailed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, "onServiceResolved",Toast.LENGTH_SHORT).show();
                Log.d("TAG","hoohoohoo:"+nsdServiceInfo.toString());
            }
        };

        mDiscoverListener = new NsdManager.DiscoveryListener() {
            private  NsdServiceInfo mmNsdServiceInfo;

            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "onStartDiscoveryFailed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                Toast.makeText(MainActivity.this, "onStopDiscoveryFailed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStarted(String s) {
                Toast.makeText(MainActivity.this, "onDiscoveryStarted",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDiscoveryStopped(String s) {
                Toast.makeText(MainActivity.this, "onDiscoveryStopped",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
                mmNsdServiceInfo = nsdServiceInfo;
                Toast.makeText(MainActivity.this, "onServiceFound",Toast.LENGTH_SHORT).show();
                nsdManager.resolveService(mmNsdServiceInfo,mDResolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Toast.makeText(MainActivity.this, "onServiceLost",Toast.LENGTH_SHORT).show();
            }
        };

        nsdManager.discoverServices(SERVICE_TYPE,NsdManager.PROTOCOL_DNS_SD,mDiscoverListener);
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