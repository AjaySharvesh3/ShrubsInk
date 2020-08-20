package com.shrubsink.everylifeismatter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class CreditsFragment extends Fragment {

    Button viewCreditBtn;

    public CreditsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_credits, container, false);

        viewCreditBtn = view.findViewById(R.id.view_credit_btn);

        viewCreditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        requireContext(), R.style.BottomSheetDialogTheme
                );
                final View bottomSheetView = LayoutInflater.from(requireActivity().getApplicationContext())
                        .inflate(
                                R.layout.credits_view_bottom_sheet,
                                (ScrollView) view.findViewById(R.id.bottom_sheet_container_credits)
                        );
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

        return view;
    }
}