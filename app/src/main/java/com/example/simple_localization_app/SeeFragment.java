package com.example.simple_localization_app;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.simple_localization_app.data.InfoRssi;
import com.example.simple_localization_app.data.MeasurePoint;
import com.example.simple_localization_app.data.WardrivingViewModel;
import com.example.simple_localization_app.databinding.FragmentScanBinding;
import com.example.simple_localization_app.databinding.FragmentSeeBinding;

import java.util.Map;

public class SeeFragment extends Fragment {

    private FragmentSeeBinding binding;
    private WardrivingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSeeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(WardrivingViewModel.class);

        Bundle args = getArguments();
        if (args != null){
            float x = args.getFloat("x");
            float y = args.getFloat("y");

            MeasurePoint targetPoint = findMeasurePoint(x, y);
            if (targetPoint != null){
                String displayText = buildDisplayText(targetPoint);
                binding.tvWifilog.setText(displayText);
            }
            else{
                binding.tvWifilog.setText("No data found for this location.");
            }
        }

        clickYesbtn();

    }

    protected void clickYesbtn() {
        binding.btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageActivity activity = (ImageActivity) requireActivity();
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }

    private MeasurePoint findMeasurePoint(float x, float y) {
        float threshold = 1f;
        for (MeasurePoint point : viewModel.getAllPoints()) {
            if (Math.abs(point.x - x) < threshold && Math.abs(point.y - y) < threshold) {
                return point;
            }
        }
        return null;
    }

    private String buildDisplayText(MeasurePoint point) {
        StringBuilder sb = new StringBuilder();
        sb.append("Location (x,y) = (")
                .append(point.x).append(", ").append(point.y).append(")\n\n");

        for (InfoRssi info : point.apList) {
            sb.append("[[timestamp : ").append(info.timestamp).append("]]\n");
            for (Map.Entry<String, Integer> entry : info.apInfoMap.entrySet()) {
                sb.append(entry.getKey())
                        .append(" ==> ").append(entry.getValue()).append(" dBm\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
