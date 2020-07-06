package io.github.ryanhoo.music.ui.main;


import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.widget.RadioButton;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.ui.base.BaseActivity;
import io.github.ryanhoo.music.ui.base.BaseFragment;
import io.github.ryanhoo.music.ui.local.LocalFilesFragment;
import io.github.ryanhoo.music.ui.music.MusicPlayerFragment;
import io.github.ryanhoo.music.ui.playlist.PlayListFragment;
import io.github.ryanhoo.music.ui.settings.SettingsFragment;

import java.util.List;

public class MainActivity extends BaseActivity {

    static final int DEFAULT_PAGE_INDEX = 2;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindViews({R.id.radio_button_play_list, R.id.radio_button_music, R.id.radio_button_local_files, R.id.radio_button_settings})
    List<RadioButton> radioButtons;


    String[] mTitles;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Main Controls' Titles
        mTitles = getResources().getStringArray(R.array.mp_main_titles);

        // Fragments
        BaseFragment[] fragments = new BaseFragment[mTitles.length];
        fragments[0] = new PlayListFragment();  //最左边，收藏的歌单
        fragments[1] = new MusicPlayerFragment(); //播放界面
        fragments[2] = new LocalFilesFragment();  //文件选择界面
        fragments[3] = new SettingsFragment(); //编辑界面

        // Inflate ViewPager
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager(), mTitles, fragments);
        //(manager,标题，页面项目)
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.mp_margin_large));
        //在viewPager里面设置了对OnPageSelected的监听，但是也只做了一下赋值，没有操作，
        // 这个.setChecked的设置又会触发 @OnCheckedChanged 这个函数做具体变化
        // 所以Adapter做什么的？
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Empty
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Empty
            }

            @Override
            public void onPageSelected(int position) {
                radioButtons.get(position).setChecked(true);
            }
        });

        radioButtons.get(DEFAULT_PAGE_INDEX).setChecked(true);
        //默认值是2，也就是文件选择界面，radioButtons.get(i)表示获得下标为i的fraction界面
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);//变成false也没有影响，按返回键也不会退出
    }

    @OnCheckedChanged({R.id.radio_button_play_list, R.id.radio_button_music, R.id.radio_button_local_files, R.id.radio_button_settings})
    public void onRadioButtonChecked(RadioButton button, boolean isChecked) {
        if (isChecked) {
            int position=radioButtons.indexOf(button);
            viewPager.setCurrentItem(position);  //改变页面
           /*练习 List<String> datas=new ArrayList<String>(4);
            datas.add("1");datas.add("2");datas.add("3");datas.add("10");
            String [] datas2=new String[10];
            for(int i=0;i<4;++i) { datas2[i]=String.valueOf(i*2); }*/
            toolbar.setTitle(mTitles[position]);//改变工具栏标题
        }
    }
}
