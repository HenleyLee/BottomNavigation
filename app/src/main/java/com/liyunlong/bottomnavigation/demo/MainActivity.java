package com.liyunlong.bottomnavigation.demo;

import android.os.Bundle;
import android.util.Log;

import com.liyunlong.bottomnavigation.BottomNavigationLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity implements BottomNavigationLayout.OnNavigationSelectedListener {

    private static final int NAVIGATION_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> titles = new ArrayList<>(NAVIGATION_COUNT);
        titles.add(getString(R.string.title_home));
        titles.add(getString(R.string.title_dashboard));
        titles.add(getString(R.string.title_notifications));

        List<Integer> icons = new ArrayList<>(NAVIGATION_COUNT);
        icons.add(R.drawable.ic_home_black_24dp);
        icons.add(R.drawable.ic_dashboard_black_24dp);
        icons.add(R.drawable.ic_notifications_black_24dp);

        List<TestFragment> fragments = new ArrayList<>(NAVIGATION_COUNT);
        for (int i = 0; i < NAVIGATION_COUNT; i++) {
            fragments.add(TestFragment.newInstance(titles.get(i)));
        }
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), titles, icons, fragments));
        BottomNavigationLayout navigation = findViewById(R.id.navigation_layout);
        navigation.addOnNavigationSelectedListener(this);
        navigation.setupWithViewPager(viewPager);
    }


    @Override
    public void onNavigationSelectedChanged(BottomNavigationLayout.Navigation selected, BottomNavigationLayout.Navigation unselected) {
        Log.i("TAG", "选中 Navigation：" + selected.getPosition() + " --> " + selected.getText());
    }

}
