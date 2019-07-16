# BottomNavigation —— 自定义底部导航栏
使用方式类似于 `TabLayout`，可以关联 `ViewPager`。

## Download ##
### Gradle ###
```gradle
dependencies {
    implementation 'com.henley.android:bottomnavigation:1.0.1'
}
```

### APK Demo ###

下载 [APK-Demo](https://github.com/HenleyLee/BottomNavigation/raw/master/app/app-release.apk)

## 使用方法 ##
 - 使用方法一：
```java
navigation.addNavigation(navigation.newNavigation().setText(R.string.title_home).setIcon(R.drawable.ic_home_black_24dp));
navigation.addNavigation(navigation.newNavigation().setText(R.string.title_dashboard).setIcon(R.drawable.ic_dashboard_black_24dp));
navigation.addNavigation(navigation.newNavigation().setText(R.string.title_notifications).setIcon(R.drawable.ic_notifications_black_24dp));
```

 - 使用方法二：
```java
final int NAVIGATION_COUNT = 3;

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
BottomNavigationLayout navigation = findViewById(R.id.navigation_layout);
viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager(), titles, icons, fragments));
navigation.addOnNavigationSelectedListener(new BottomNavigationLayout.OnNavigationSelectedListener() {
    @Override
    public void onNavigationSelectedChanged(BottomNavigationLayout.Navigation selected, BottomNavigationLayout.Navigation unselected) {
        Log.i("TAG", "选中 Navigation：" + selected.getPosition() + " --> " + selected.getText());
    }
});
navigation.setupWithViewPager(viewPager);
```

```java
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
```

 - 使用方法三：
```xml
<com.henley.bottomnavigation.BottomNavigationLayout
    android:id="@+id/navigation_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom"
    android:background="?android:attr/windowBackground"
    android:minHeight="50dp"
    app:navigationIconSize="24dp"
    app:navigationTextSize="11sp">

    <com.henley.bottomnavigation.NavigationItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:icon="@drawable/ic_home_black_24dp"
        android:text="@string/title_home" />

    <com.henley.bottomnavigation.NavigationItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:icon="@drawable/ic_dashboard_black_24dp"
        android:text="@string/title_dashboard" />

    <com.henley.bottomnavigation.NavigationItem
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:icon="@drawable/ic_notifications_black_24dp"
        android:text="@string/title_notifications" />

</com.henley.bottomnavigation.BottomNavigationLayout>
```