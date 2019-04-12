package com.henley.bottomnavigation.demo;

import com.henley.bottomnavigation.BottomNavigationLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainPagerAdapter extends FragmentPagerAdapter implements BottomNavigationLayout.INavigationIconProvider {

    private List<String> titles;
    private List<Integer> icons;
    private List<? extends Fragment> fragments;

    MainPagerAdapter(@NonNull FragmentManager fm, List<String> titles, List<Integer> icons, List<? extends Fragment> fragments) {
        super(fm);
        this.titles = titles;
        this.icons = icons;
        this.fragments = fragments;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getNavigationIcon(int position) {
        return icons.get(position);
    }

}
