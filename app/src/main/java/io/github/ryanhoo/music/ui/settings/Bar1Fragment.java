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

import com.Tool.Function.AudioFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import audioeffects.EditEffect;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.data.model.PlayList;
import io.github.ryanhoo.music.ui.details.PlayListDetailsActivity;
import io.github.ryanhoo.music.ui.main.copyFile;
import zty.composeaudio.Tool.Interface.DecodeOperateInterface;

/**
 * A simple {@link Fragment} subclass.
 */
public class Bar1Fragment extends Fragment implements DecodeOperateInterface{

//    private int position;
//    //当前播放的位置
//    private MediaPlayer player = new MediaPlayer();
//    private String path = Environment.getExternalStorageDirectory().getPath()+
//            "/Music/olddriver.mp3";
//    private int paused = 0;
//    private Timer timer;
//
//    @BindView(R.id.seek_bar)
//    SeekBar seek_bar;
//    // 暂时用音量键实现播放效果
//    @BindView(R.id.btn_volume)
//    Button play;



    public Bar1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bar1, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getView().findViewById(R.id.btn_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = PlayListDetailsActivity.current_songpath;
                if(SettingsFragment.curversion!=0)
                    path =getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion)+".wav";
                String temppath = getContext().getFilesDir() + "/tmp.pcm";
                SettingsFragment.curversion+=1;
                String decodepath = getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion) +".wav";

                Log.e("修改，歌曲路径",path);
                Log.e("修改，编辑后的歌曲路径",decodepath);
                Log.e("修改，起止时间",String.valueOf(SettingsFragment.starttime)+" "+String.valueOf(SettingsFragment.endtime));
///*/前端验证↑，后端实现↓
                short[] shorts; shorts = AudioFunction.convertMp32Short (path, temppath, SettingsFragment.starttime, SettingsFragment.endtime,
                        44100, 2, 16, Bar1Fragment.this);
                for(int i=0; i<shorts.length; i++){ shorts[i] *= 1; }
                // short数组转wav,decodepath是存储路径
                AudioFunction.convershort2wav(shorts, decodepath, 44100, 2,
                        16);
                Log.e("修改，shortLength01", String.valueOf(shorts.length));
//后端实现↑*/
//                copyFile copyFile = new copyFile("/storage/emulated/0/res/"+String.valueOf(SettingsFragment.curversion)+".wav", decodepath);


                Toast.makeText(getActivity(),"剪辑成功",Toast.LENGTH_SHORT).show();
            }
        });

        // 这是张嘉乐期望定义的volume
        getView().findViewById(R.id.btn_volume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar1Fragment_to_bar2Fragment);
            }
        });

        getView().findViewById(R.id.btn_speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar1Fragment_to_bar3Fragment);
            }
        });

        getView().findViewById(R.id.btn_tone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar1Fragment_to_bar4Fragment);
            }
        });


        // 音效处理的准备工作
        EditEffect editEffect = new EditEffect(getContext());

        getView().findViewById(R.id.btn_vibrato).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourcepath = "/data/user/0/io.github.ryanhoo.music/files/1.wav";
                String decodepath = "/data/user/0/io.github.ryanhoo.music/files/3.wav";
                int start = 1, end = 10;
                try {
                    editEffect.vibrato(sourcepath, decodepath, start, end,
                            (float) 0.005, (float) 0.005, 4);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(),"颤音成功",Toast.LENGTH_SHORT).show();
            }
        });

        getView().findViewById(R.id.btn_flanger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourcepath = "/data/user/0/io.github.ryanhoo.music/files/1.wav";
                String decodepath = "/data/user/0/io.github.ryanhoo.music/files/4.wav";
                int start = 1, end = 10;
                try {
                    editEffect.flanger(sourcepath, decodepath, start, end,
                            (float)0.003, (float)0.003, 3);
                } catch (FileNotFoundException e) {
                    Log.e("flanger失败", "flanger失败");
                }
                Toast.makeText(getActivity(),"电吉他成功",Toast.LENGTH_SHORT).show();
            }
        });

        getView().findViewById(R.id.btn_chorus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sourcepath = "/data/user/0/io.github.ryanhoo.music/files/1.wav";
                String decodepath = "/data/user/0/io.github.ryanhoo.music/files/5.wav";
                int start = 10, end = 20;
                try {
                    editEffect.chorus(sourcepath, decodepath, start, end,
                            (float)0.045, (float)0.015);
                } catch (FileNotFoundException e) {
                    Log.e("唱诗班出错", "唱诗班出错");
                }
                Toast.makeText(getActivity(),"唱诗班成功",Toast.LENGTH_SHORT).show();
            }
        });

        getView().findViewById(R.id.btn_doubling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                editEffect.doubling(sourcepath, (float)0.07, (float)0.01);
            }
        });
    }

    // 注意，以下三个函数是回调操作，不要删。回调时Java对于面向对象缺陷的一种修正
    // 如果我们缓冲区出Bug其实是很难调试的，回调提供了一种优雅的方法
    // 使用在DecodeEngin 248行
    @Override
    public void updateDecodeProgress(int decodeProgress) {
    }

    @Override
    public void decodeSuccess() {
    }

    @Override
    public void decodeFail() {
    }

}
