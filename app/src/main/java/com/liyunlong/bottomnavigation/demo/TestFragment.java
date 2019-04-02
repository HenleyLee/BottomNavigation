package com.liyunlong.bottomnavigation.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TestFragment extends Fragment {

    private static final String KEY_TEXT = "text";

    public static TestFragment newInstance(String text) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TEXT, text);
        TestFragment fragment = new TestFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        TextView tvText = rootView.findViewById(R.id.text);
        if (getArguments() != null) {
            tvText.setText(getArguments().getString(KEY_TEXT));
        }
        return rootView;
    }

}
