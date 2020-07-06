package io.github.ryanhoo.music.ui.settings;


import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

import audioeffects.EditEffect;
import butterknife.BindView;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.ui.details.PlayListDetailsActivity;
import io.github.ryanhoo.music.ui.main.copyFile;

/**
 * A simple {@link Fragment} subclass.
 */

public class Bar2Fragment extends Fragment {

    TextView textView;
    SeekBar seekBar;

    public Bar2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bar2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //设置音量显示
        textView=getView().findViewById(R.id.textView3);
        seekBar=getView().findViewById(R.id.seek_bar);
        seekBar.setProgress(100);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(seekBar.getProgress()));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        //确定按钮，需要传回设定的参数
        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double volume=seekBar.getProgress()/100.0;
                String path = PlayListDetailsActivity.current_songpath;
                if(SettingsFragment.curversion!=0)
                    path =getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion)+".wav";
                SettingsFragment.curversion+=1;
                String decodepath = getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion) +".wav";

                Log.e("修改，音量比例",String.valueOf(volume));
                Log.e("修改，歌曲路径",path);
                Log.e("修改，编辑后的歌曲路径",decodepath);
                Log.e("修改，起止时间",String.valueOf(SettingsFragment.starttime)+" "+String.valueOf(SettingsFragment.endtime));
//前端验证↑，后端实现↓
//                path = "/data/user/0/io.github.ryanhoo.music/files/1.wav";
//                decodepath = "/data/user/0/io.github.ryanhoo.music/files/2.wav";
                EditEffect.ChangeVolume(path, decodepath, SettingsFragment.starttime, SettingsFragment.endtime, volume);
//                copyFile copyFile = new copyFile("/storage/emulated/0/res/"+String.valueOf(SettingsFragment.curversion)+".wav", decodepath);
 //后端实现↑，带参数回传bar1页面↓


                Bundle bundle=new Bundle();
                bundle.putDouble("Volume",volume);

                try {
                    NavController controller= Navigation.findNavController(v);
                    controller.navigate(R.id.action_bar2Fragment_to_bar1Fragment,bundle);
                } catch (Exception e) {
                    Log.e("跳转出错", "跳转出错");
                }

                //在起点界面中用Bundle  bundle=new Bundle();  bundle.putString("para",str);
                // 再将此bundle作为navigate函数第二个参数即可在目标界面得到以"para"为键的参数
                //在目标界面的Java中getArgument.getString(key)即可得到字符串变量
                Toast.makeText(getActivity(),"音量调节成功",Toast.LENGTH_SHORT).show();
            }
        });

        //取消按钮，直接返回
        getView().findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar2Fragment_to_bar1Fragment);
            }
        });
    }


        @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       // TextView text1=getView().findViewById(R.id.textView);
       // String str1=getArguments().getString("keyname");
       // String str2=getArguments().getString("para");
       // text1.setText(str2);
    }

}
