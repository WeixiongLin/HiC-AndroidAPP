package io.github.ryanhoo.music.ui.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import io.github.ryanhoo.music.ui.base.BaseFragment;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 7/8/16
 * Time: 9:24 PM
 * Desc: MainTabAdapter
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
//MVC模式，这个是数据模型，虽然只有两个数据，但是还是封装起来好
    private String[] mTitles;
    private BaseFragment[] mFragments;

    public MainPagerAdapter(FragmentManager fm, String[] titles, BaseFragment[] fragments) {
        super(fm);
        mTitles = titles;
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        if (mTitles == null) return 0;
        return mTitles.length;
    }
}
