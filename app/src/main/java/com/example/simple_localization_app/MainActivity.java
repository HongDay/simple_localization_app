package com.example.simple_localization_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.simple_localization_app.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private static final String TAG = "MainActivity";

    private boolean isScanRequested = true;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        // 시스템이 보내는 브로드캐스트(Intent)를 수신했을 때 실행되는 메서드
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); // 어떤 종류의 이벤트인지 문자열 반환
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.e(TAG, "Scan results available");

                if (!isScanRequested) {
                    Log.e(TAG, "Ignoared : onRceive called while scan request is failed");
                    return;
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    List<ScanResult> scanResults = mWifiManager.getScanResults();
                    String mApStr = "";
                    for (ScanResult result : scanResults) {
                        mApStr = mApStr + result.SSID + "; ";
                        mApStr = mApStr + result.BSSID + "; ";
                        mApStr = mApStr + result.capabilities + "; ";
                        mApStr = mApStr + result.frequency + " MHz;";
                        mApStr = mApStr + result.level + " dBm\n\n";
                    }
                    setTextView(mApStr);
                    Log.e(TAG, "Scan results stored!!");
                } else {
                    Log.e(TAG, "@@@@@ no permission for scanning !!");
                }

            }
            else{
                Log.e(TAG, "Scan results unavailable!!!");
            }
        }
    };

    private void setTextView(String text){
        binding.tvWifilog.setMovementMethod(new ScrollingMovementMethod());
        binding.tvWifilog.setText(text);
    }

    // onCreate()는 액티비티가 처음 생성될 때 1회 호출 : 초기 UI설정, 변수 초기화 등
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_main); // 뷰 객체들을 findid로 수동으로 찾아야함
        setContentView(binding.getRoot());

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // 버튼클릭리스너를 버튼에 연결 (바인딩)
        binding.btWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "button click detected!!");

                boolean scanStarted = mWifiManager.startScan(); //startscan()이 스캔"요청" 함수. 시스템에 스캔을 요청한다.

                if (!scanStarted) {
                    isScanRequested = false;
                    Log.e(TAG, "WiFi scan failed..");
                    setTextView("Scan request failed. Try again!");
                }
                else Log.e(TAG, "Scan request success!");
            }
        });

        /*시스템에서 스캔이 끝났을때, 결과가 도착하면 때 그 스캔결과에 대한 알림을 브로드캐스트로서 보내는데, 이를 감지함.
          브로드캐스트 : Android에서 시스템 또는 앱이 발생한 사건을 다른 컴포넌트에 알려주는 메시지 */
        mIntentFilter = new IntentFilter();
        // 어떤 브로드캐스트에 반응할 것인지 필터에 지정하기
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    // BroadcastReceiver의 등록과 해제를 Activity의 생명주기에 맞춰 처리하는 부분

    // onResume()은 액티비티가 화면에 보일 때(화면 전환 후 복귀시)마다 호출 : 사용자와 상호작용 준비, 센서 또는 브로드캐스트 리시버 등록 등
    @Override
    protected void onResume(){
        super.onResume();
        // BroadcastReceiver를 등록하여 시스템으로부터의 이벤트를 수신할 준비 : registerReceiver()를 통해 mReceiver에 이 필터를 부여
        // mReceiver : BroadcastReceiver객체, mIntentFilter : 어떤 브로드캐스트에 반응할 것인지 지정한 필터
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }


}