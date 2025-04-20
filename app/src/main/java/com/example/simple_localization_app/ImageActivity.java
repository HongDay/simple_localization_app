package com.example.simple_localization_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.MyDotOverlayView;
import com.example.simple_localization_app.data.MeasurePoint;
import com.example.simple_localization_app.data.WardrivingViewModel;
import com.example.simple_localization_app.databinding.ActivityImageBinding;

public class ImageActivity extends AppCompatActivity {

    private ActivityImageBinding binding;
    private WardrivingViewModel viewModel;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(WardrivingViewModel.class);

        setting();

    }

    protected void setting(){
        clickUploadButton();
        clickWardriveButton();
        clickDeleteButton();
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
                            viewModel.reset();
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
                binding.tvTop1.setText("Wardriving Mode");
                binding.tvTop1.setTextColor(Color.RED);
                binding.btnWardriving.setTextColor(Color.parseColor("#bbbbbb"));
                clickImageview();
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


}
