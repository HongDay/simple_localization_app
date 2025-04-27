package com.example.simple_localization_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.MyDotOverlayView;
import com.example.simple_localization_app.data.GroundtruthViewModel;
import com.example.simple_localization_app.data.InfoRssi;
import com.example.simple_localization_app.data.LocalizationViewModel;
import com.example.simple_localization_app.data.MeasurePoint;
import com.example.simple_localization_app.data.WardrivingViewModel;
import com.example.simple_localization_app.databinding.ActivityImageBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageActivity extends AppCompatActivity {

    private ActivityImageBinding binding;
    private WardrivingViewModel viewModel;
    private LocalizationViewModel localviewModel;
    private GroundtruthViewModel gtviewModel;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_PERMISSION = 1000;

    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private Handler scanHandler;
    private Runnable scanRunnable;
    private static final String TAG = "ImageActivity";
    private boolean isScanRequested = true;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        viewModel = new ViewModelProvider(this).get(WardrivingViewModel.class);
        localviewModel = new ViewModelProvider(this).get(LocalizationViewModel.class);
        gtviewModel = new ViewModelProvider(this).get(GroundtruthViewModel.class);

        setting();
    }

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

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                Map<String, Integer> currentScan = new HashMap<>();

                InfoRssi newinfo = new InfoRssi(System.currentTimeMillis());

                for (ScanResult result : scanResults) {
                    currentScan.put(result.BSSID, result.level);
                    newinfo.addApInfo(result.BSSID,result.level);
                }

                float[] estimated = estimateLocation(currentScan, viewModel.getAllPoints());

                if(!(estimated[0]==0 && estimated[1]==0)){
                    MeasurePoint mpoint = new MeasurePoint(estimated[0], estimated[1]);
                    mpoint.addRssi(newinfo);
                    localviewModel.addMeasurePoint(mpoint);

                    for (MeasurePoint point : localviewModel.getAllPoints()) {
                        Log.d("MeasurePoint", point.toString());
                    }

                    binding.dotOverlay2.updatePoint(estimated[0],estimated[1]);

                    float normX = estimated[0] / binding.imageView.getWidth();
                    float normY = estimated[1] / binding.imageView.getHeight();
                    binding.tvLocation.setText(String.format("location : (%.2f, %.2f)", normX, normY));

                    binding.tvLocal.setText("Localization : Location updated !");
                    Log.e(TAG, "Scan results displayed!!");
                }
            } else {
                Log.e(TAG, "@@@@@ no permission for scanning !!");
            }

        }
        else{
            Log.e(TAG, "Scan results unavailable!!!");
        }
        }
    };

    protected void setting(){
        clickUploadButton();
        clickWardriveButton();
        clickDeleteButton();
        clickLocalButton();
        clickWardriveEmailButton();
        clickStopLocalButton();
        clickLocalEmailButton();
        clickGtPointButton();
        clickGtEmailButton();
    }

    protected void clickGtPointButton(){
        binding.btnGtpoint.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                binding.btnGtpoint.setTextColor(Color.parseColor("#bbbbbb"));
                clickImageview2();
            }
        });
    }

    protected void clickGtEmailButton(){
        binding.btnGtemail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new AlertDialog.Builder(ImageActivity.this)
                        .setTitle("Email Alert")
                        .setMessage("Would you like to Send Email with Ground Truth info?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            File csvFile = exportToCsv(ImageActivity.this, gtviewModel.getAllPoints(), "groundtruth_data.csv");
                            sendEmailWithCsv(ImageActivity.this, csvFile, "Ground Truth Data CSV");
                        })
                        .setNegativeButton("NO", null)
                        .show();
            }
        });
    }

    protected void clickLocalEmailButton(){
        binding.btnLocalizationemail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new AlertDialog.Builder(ImageActivity.this)
                        .setTitle("Email Alert")
                        .setMessage("Would you like to Send Email with Localization info?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            File csvFile = exportToCsv(ImageActivity.this, localviewModel.getAllPoints(), "localization_data.csv");
                            sendEmailWithCsv(ImageActivity.this, csvFile, "Localization Data CSV");
                        })
                        .setNegativeButton("NO", null)
                        .show();
            }
        });
    }

    protected void clickWardriveEmailButton(){
        binding.btnWardriveemail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new AlertDialog.Builder(ImageActivity.this)
                        .setTitle("Email Alert")
                        .setMessage("Would you like to Send Email with Wardriving info?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            File csvFile = exportToCsv(ImageActivity.this, viewModel.getAllPoints(), "wardriving_data.csv");
                            sendEmailWithCsv(ImageActivity.this, csvFile, "Wardriving Data CSV");
                        })
                        .setNegativeButton("NO", null)
                        .show();
            }
        });
    }

    public void sendEmailWithCsv(Context context, File csvFile, String title) {
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",  // Manifest에 설정한 authority
                csvFile
        );

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/csv");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the CSV file.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(emailIntent, "Send email via:"));
    }


    public File exportToCsv(Context context, List<MeasurePoint> pointList, String fileName) {
        File csvFile = new File(context.getExternalFilesDir(null), fileName);

        try (FileWriter writer = new FileWriter(csvFile)) {
            // CSV 헤더 작성
            writer.append("x,y,timestamp,BSSID,RSSI\n");

            // 데이터 작성
            for (MeasurePoint point : pointList) {
                for (InfoRssi info : point.apList) {
                    for (Map.Entry<String, Integer> entry : info.apInfoMap.entrySet()) {
                        writer.append(point.x + "," + point.y + "," + info.timestamp + ","
                                + entry.getKey() + "," + entry.getValue() + "\n");
                    }
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csvFile;
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void clickImageview2(){
        MyDotOverlayView dotOverlay = binding.dotOverlay3;
        dotOverlay.setDotColor(Color.GREEN);
        binding.imageView.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                float x = event.getX();
                float y = event.getY();
                dotOverlay.addPoint(x,y);

                new AlertDialog.Builder(this)
                        .setTitle("GT Add Alert")
                        .setMessage("Would you like to add Ground Truth here?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            InfoRssi newinfo = new InfoRssi(System.currentTimeMillis());
                            newinfo.addApInfo("0",0);
                            MeasurePoint mpoint = new MeasurePoint(x, y);
                            mpoint.addRssi(newinfo);
                            gtviewModel.addMeasurePoint(mpoint);

                            for (MeasurePoint point : gtviewModel.getAllPoints()) {
                                Log.d("MeasurePoint", point.toString());
                            }
                        })
                        .setNegativeButton("NO", (dialog, which) -> {
                            dotOverlay.removeLastPoint();
                        })
                        .setOnCancelListener(dialog -> {
                            dotOverlay.removeLastPoint();
                        })
                        .show();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void clickImageview(){
        MyDotOverlayView dotOverlay = binding.dotOverlay;
        binding.imageView.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_DOWN){
                float x = event.getX();
                float y = event.getY();
                dotOverlay.addPoint(x,y);

                new AlertDialog.Builder(this)
                        .setTitle("Scan Alert")
                        .setMessage("Would you like to Scan here?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            MeasurePoint newpoint = new MeasurePoint(x, y);
                            viewModel.addMeasurePoint(newpoint);
                            replaceFragment(new ScanFragment());
                        })
                        .setNegativeButton("NO", (dialog, which) -> {
                            dotOverlay.removeLastPoint();
                        })
                        .setOnCancelListener(dialog -> {
                            dotOverlay.removeLastPoint();
                        })
                        .show();

                return true;
            }
            return false;
        });
    }

    protected void clickStopLocalButton(){
        binding.btnLocalizationstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanHandler.removeCallbacks(scanRunnable);
                Log.d(TAG, "WiFi scan stopped.");

                unregisterReceiver(mReceiver);
                Log.d(TAG, "Receiver unregistered.");

                binding.btnLocalizationstart.setVisibility(View.VISIBLE);
                binding.btnLocalizationstop.setVisibility(View.GONE);
                binding.tvLocal.setVisibility(View.GONE);
                binding.tvTop1.setText("Waiting Mode");
                binding.tvTop1.setTextColor(Color.GRAY);
                binding.btnGtpoint.setVisibility(View.GONE);
                binding.imageView.setOnTouchListener(null);
            }
        });
    }

    protected void clickLocalButton(){
        binding.btnLocalizationstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //binding.dotOverlay.clearall();
                registerReceiver(mReceiver,mIntentFilter);
                binding.tvTop1.setText("Localization mode");
                binding.tvTop1.setTextColor(Color.BLUE);
                binding.btnWardriving.setTextColor(Color.WHITE);
                binding.btnLocalizationstart.setVisibility(View.GONE);
                binding.btnLocalizationstop.setVisibility(View.VISIBLE);
                binding.btnLocalizationemail.setVisibility(View.VISIBLE);
                binding.btnGtpoint.setVisibility(View.VISIBLE);
                binding.btnGtemail.setVisibility(View.VISIBLE);
                binding.imageView.setOnTouchListener(null);
                scanHandler = new Handler();
                scanRunnable = new Runnable() {
                    @Override
                    public void run() {
                        binding.tvLocal.setVisibility(View.VISIBLE);
                        boolean scanStarted = mWifiManager.startScan();
                        if (!scanStarted) {
                            isScanRequested = false;
                            Log.e(TAG, "WiFi scan failed..");
                            binding.tvLocal.setText("Localization : Scan pending.. (scan limit exceeded)");
                        } else {
                            isScanRequested = true;
                            Log.e(TAG, "Scan request success");
                            binding.tvLocal.setText("Localizatoin : Scanning ..");
                        }
                        scanHandler.postDelayed(this, 10000);
                    }
                };
                scanHandler.post(scanRunnable);
            }
        });
    }

    public float[] estimateLocation(Map<String, Integer> currentScan, List<MeasurePoint> pointList) {
        List<Pair<MeasurePoint, Double>> distances = new ArrayList<>();

        for (MeasurePoint point : pointList) {
            double avgDistance = computeDistance(currentScan, point);
            distances.add(new Pair<>(point, avgDistance));
        }

        distances.sort(Comparator.comparingDouble(pair -> pair.second));

        if (distances.size() < 3) {
            binding.tvLocal.setText("Not enough wardriving points !! (at least 3)");
            return new float[]{0,0};
        }
        List<Pair<MeasurePoint, Double>> nearestK = distances.subList(0,3);

        double sumWeights = 0;
        double weightedX = 0;
        double weightedY = 0;

        for (Pair<MeasurePoint, Double> entry : nearestK) {
            double weight = 1.0 / Math.pow(entry.second, 2);
            weightedX += entry.first.x * weight;
            weightedY += entry.first.y * weight;
            sumWeights += weight;
        }

        return new float[]{(float)(weightedX/sumWeights), (float)(weightedY/sumWeights)};
    }

    private double computeDistance(Map<String, Integer> currentScan, MeasurePoint point){
        Map<String, Double> avgRssiMap = getAverageRssi(point);
        double sum = 0;
        int count = 0;

        for (String bssid : currentScan.keySet()) {
            if (avgRssiMap.containsKey(bssid)) {
                int scanRssi = currentScan.get(bssid);
                double refRssi = avgRssiMap.get(bssid);
                sum += Math.pow(scanRssi - refRssi, 2);
                count++;
            }
            /*
            else{
                sum += 10000;
            }
             */
            /*
            else{
                sum += Math.pow(scanRssi, 2);
                count++;
             */
        }
        return (double) Math.sqrt(sum/count);
    }

    private Map<String, Double> getAverageRssi(MeasurePoint point) {
        Map<String, List<Integer>> rssiValues = new HashMap<>();

        for (InfoRssi info : point.apList) {
            for (Map.Entry<String, Integer> entry : info.apInfoMap.entrySet()){
                rssiValues.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        Map<String, Double> avgRssiMap = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : rssiValues.entrySet()){
            avgRssiMap.put(entry.getKey(), entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(Double.NaN));
        }

        return avgRssiMap;
    }

    protected void clickDeleteButton(){
        // 익명 class를 만들어서 OnClickListener()라는 인터페이스를 구현하여 btn.Deleteimg에 바로 정의하는 구조.
        // 따라서 여기는 class내부임. Builder에 this를 쓰면 이 클래스를 가리키므로 안됨.
        binding.btnDeleteimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                new AlertDialog.Builder(ImageActivity.this)
                        .setTitle("Delete Alert")
                        .setMessage("Do you really want to delete image and done whole the process?")
                        .setPositiveButton("YES", (dialog, which) -> {
                            binding.imageView.setImageDrawable(null);
                            binding.btnDeleteimg.setVisibility(View.GONE);
                            binding.btnWardriving.setTextColor(Color.parseColor("#ffffff"));
                            binding.btnWardriving.setVisibility(View.GONE);
                            binding.btnUploadimg.setVisibility(View.VISIBLE);
                            binding.tvTop1.setText("Image Loading");
                            binding.tvTop1.setTextColor(Color.parseColor("#555555"));
                            binding.dotOverlay.clearall();
                            binding.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
                            binding.imageView.setOnTouchListener(null);
                            binding.btnWardriveemail.setVisibility(View.GONE);
                            binding.btnLocalizationstop.setVisibility(View.GONE);
                            binding.btnLocalizationemail.setVisibility(View.GONE);
                            binding.btnLocalizationstart.setVisibility(View.GONE);
                            binding.tvLocal.setVisibility(View.GONE);
                            viewModel.reset();
                            if (scanHandler != null && scanRunnable != null) {
                                scanHandler.removeCallbacks(scanRunnable);
                                Log.d(TAG, "WiFi scan stopped.");
                            }
                            try {
                                unregisterReceiver(mReceiver);
                                Log.d(TAG, "Receiver unregistered.");
                            } catch (IllegalArgumentException e) {
                                Log.w(TAG, "Receiver was already unregistered.");
                            }
                        })
                        .setNegativeButton("NO", null)
                        .show();

            }
        });
    }

    protected void clickWardriveButton(){
        binding.btnWardriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if(binding.btnLocalizationstop.getVisibility() == View.VISIBLE){
                    binding.tvLocal.setText("!!!!!! Cannot start wardriving !! Stop Localization First!!");
                }
                else{
                    binding.tvTop1.setText("Wardriving Mode");
                    binding.tvTop1.setTextColor(Color.RED);
                    binding.btnWardriving.setTextColor(Color.parseColor("#bbbbbb"));
                    clickImageview();
                }
            }
        });
    }

    protected void clickUploadButton(){
        binding.btnUploadimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null){
            Uri selectedImage = data.getData();
            binding.imageView.setImageURI(selectedImage);
            binding.btnWardriving.setVisibility(View.VISIBLE);
            binding.btnUploadimg.setVisibility(View.GONE);
            binding.btnDeleteimg.setVisibility(View.VISIBLE);
        }
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void removeLastDotOverlayPoint() {
        binding.dotOverlay.removeLastPoint();
    }
    public void visibleLocalizationBtn() {binding.btnLocalizationstart.setVisibility(View.VISIBLE);}
    public void visibleEmailBtn() {binding.btnWardriveemail.setVisibility(View.VISIBLE);}
    /*
    @Override
    public void onResume(){
        super.onResume();
        this.registerReceiver(mReceiver, mIntentFilter);
    }
    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mReceiver);
    }
    */

}
