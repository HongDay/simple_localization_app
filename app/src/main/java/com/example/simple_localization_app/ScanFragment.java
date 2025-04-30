package com.example.simple_localization_app;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.simple_localization_app.data.InfoRssi;
import com.example.simple_localization_app.data.MeasurePoint;
import com.example.simple_localization_app.data.WardrivingViewModel;
import com.example.simple_localization_app.databinding.ActivityImageBinding;
import com.example.simple_localization_app.databinding.FragmentScanBinding;

import java.util.ArrayList;
import java.util.List;

public class ScanFragment extends Fragment {

    private FragmentScanBinding binding;
    private WardrivingViewModel viewModel;
    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private static final String TAG = "ScanFragment";
    private boolean isScanRequested = false;

    private int scanCount = 0;
    private Handler scanHandler;
    private Runnable scanRunnable;

    private Float targetX = null;
    private Float targetY = null;

    private final List<InfoRssi> tempRssiList = new ArrayList<>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.e(TAG, "Scan results available");

                if (!isScanRequested) {
                    Log.e(TAG, "Ignoared : onRceive called while scan request is failed");
                    return;
                }
                else {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        List<ScanResult> scanResults = mWifiManager.getScanResults();
                        InfoRssi newinfo = new InfoRssi(System.currentTimeMillis());
                        String mApStr = "";
                        for (ScanResult result : scanResults) {
                            mApStr = mApStr + result.SSID + "; ";
                            mApStr = mApStr + result.BSSID + "; ";
                            mApStr = mApStr + result.capabilities + "; ";
                            mApStr = mApStr + result.frequency + " MHz;";
                            mApStr = mApStr + result.level + " dBm\n\n";
                            newinfo.addApInfo(result.BSSID,result.level);
                        }
                        setTextView(mApStr);

                        tempRssiList.add(newinfo);

                        Log.e(TAG, "Scan results stored!!");
                    } else {
                        Log.e(TAG, "@@@@@ no permission for scanning !!");
                    }
                }
            }
            else{
                Log.e(TAG, "Scan results unavailable!!!");
            }
        }
    };

    public ScanFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater,container,false);

        if (getArguments() != null) {
            targetX = getArguments().getFloat("x", Float.NaN);
            targetY = getArguments().getFloat("y", Float.NaN);
            if (Float.isNaN(targetX) || Float.isNaN(targetY)) {
                targetX = null;
                targetY = null;
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WardrivingViewModel.class);

        mWifiManager = (WifiManager) requireContext().getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        setting();
    }

    private void setting() {
        clickCheckbtn();
        clickYesbtn();
        clickNobtn();
        if(targetX != null && targetY != null){
            binding.tvSave.setText("2 Scan done. Would you like to add this data to this point?");
        }
    }

    protected void clickYesbtn(){
        binding.btnScanyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MeasurePoint mpoint;
                if (targetX != null && targetY != null) {
                    mpoint = viewModel.findMeasurePointByCoordinates(targetX, targetY);
                } else {
                    mpoint = viewModel.getLastPoint();
                }

                for (InfoRssi info : tempRssiList) {
                    mpoint.addRssi(info);
                }

                ImageActivity activity = (ImageActivity) requireActivity();
                activity.visibleLocalizationBtn();
                activity.visibleEmailBtn();
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    protected void clickNobtn(){
        binding.btnScanno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempRssiList.clear();
                ImageActivity activity = (ImageActivity) requireActivity();
                if(targetX == null || targetY == null){
                    viewModel.removeLastPoint();
                    activity.removeLastDotOverlayPoint();
                }
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    protected void clickCheckbtn() {
        binding.btnStartscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "button click detected!!");

                binding.tvIters.setVisibility(View.VISIBLE);

                // 초기화
                scanCount = 0;
                scanHandler = new Handler();
                scanRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (scanCount >= 2) {
                            // 모든 스캔 완료 후 처리
                            isScanRequested = false;
                            for (MeasurePoint point : viewModel.getAllPoints()) {
                                Log.d("MeasurePoint", point.toString());
                            }
                            binding.tvSave.setVisibility(View.VISIBLE);
                            binding.btnScanno.setVisibility(View.VISIBLE);
                            binding.btnScanyes.setVisibility(View.VISIBLE);
                            return;
                        }
                        boolean scanStarted = mWifiManager.startScan();
                        if (!scanStarted) {
                            isScanRequested = false;
                            Log.e(TAG, "WiFi scan failed..");
                            setTextView("Maximum scan limit exceeded (4times/min) Press the button again at least 1 min later!");
                            scanCount = 0;
                            scanHandler.removeCallbacks(scanRunnable);
                            return;
                        } else {
                            isScanRequested = true;
                            Log.e(TAG, "▶ Scan request #" + (scanCount + 1) + " success");
                            binding.tvIters.setText("Scan #" + (scanCount + 1) + " requesting...");
                        }
                        scanCount++;
                        scanHandler.postDelayed(this, 8000);
                    }
                };
                scanHandler.post(scanRunnable);
            }
        });
    }


    private void setTextView(String text){
        binding.tvWifilog.setMovementMethod(new ScrollingMovementMethod());
        binding.tvWifilog.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireContext().registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(mReceiver);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
