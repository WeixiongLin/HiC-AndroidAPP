package io.github.ryanhoo.music.ui.settings;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Tool.Function.AudioFunction;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.ui.base.BaseFragment;
import io.github.ryanhoo.music.ui.details.PlayListDetailsActivity;
import io.github.ryanhoo.music.ui.main.copyFile;
import io.github.ryanhoo.music.utils.TimeUtils;
import zty.composeaudio.Tool.Interface.DecodeOperateInterface;

import static com.Tool.Function.AudioFunction.convertPcm2Wav;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/1/16
 * Time: 9:59 PM
 * Desc: SettingsFragment
 */
public class SettingsFragment extends BaseFragment implements DecodeOperateInterface {

    // 播放初始化
    private int position;
    //当前播放的位置
    private MediaPlayer player = new MediaPlayer();
    // 这是要播放的音乐路径
    private String path ;

    private int paused = 0;
    private Timer timer;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.seek_bar2)
    SeekBar seekBar2;


    public static  int starttime=0;
    public static  int endtime=20;
    public static  int curversion=0;

    TextView tv_duration;
    TextView tv_duration2;

    TextView tv_progress;
    TextView tv_progress2;


    int seqnum=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        Toast.makeText(getActivity(), "成功创建", Toast.LENGTH_SHORT).show();
        tv_duration=getView().findViewById(R.id.tv_duration);
        tv_duration2=getView().findViewById(R.id.tv_duration2);
        tv_progress=getView().findViewById(R.id.tv_progress);
        tv_progress2=getView().findViewById(R.id.tv_progress2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                position = seekBar.getProgress();
                player.seekTo(position);
                if(paused%2==0 && paused!=0){
                    player.start();
                    paused++;
                }
                starttime=Math.min(seekBar2.getProgress(),seekBar.getProgress())/1000;
                endtime=Math.max(seekBar2.getProgress(),seekBar.getProgress())/1000;
                getProgress();

            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar2) {
                tv_progress2.setText(TimeUtils.formatDuration(seekBar2.getProgress()));
                tv_progress.setText(TimeUtils.formatDuration(seekBar.getProgress()));
                starttime=Math.min(seekBar2.getProgress(),seekBar.getProgress())/1000;
                endtime=Math.max(seekBar2.getProgress(),seekBar.getProgress())/1000;

            }
        });
        getView().findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });
        getView().findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path= PlayListDetailsActivity.current_songpath;
                if(SettingsFragment.curversion!=0)
                    path =getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion)+".wav";

                Log.e("修改，现在播放的路径为",path);
                starttime=Math.min(seekBar2.getProgress(),seekBar.getProgress())/1000;
                endtime=Math.max(seekBar2.getProgress(),seekBar.getProgress())/1000;
                if(v.getId() == R.id.btn_play){
                    if(paused == 0){
                        try {
                            player.setDataSource(path);
                            player.prepare();
                            player.start();
                            paused ++;
                            seekBar.setMax(player.getDuration());
                            seekBar2.setMax(player.getDuration());
                            tv_duration2.setText(TimeUtils.formatDuration(player.getDuration()));
                            tv_duration.setText (TimeUtils.formatDuration(player.getDuration()));
                            getProgress();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else{
                        if(paused%2==1){
                            player.pause();
                            paused++;
                        }
                        else{
                            player.start();
                            paused++;
                        }
                    }
                }
            }
        });
    }


    public void onEditAction(View view) {
        TextView songpath=getActivity().findViewById(R.id.songpath);
        String path=new String ((String) songpath.getText()); ///利用activity中控件传值
        // 需要编辑的音频路径


        String temppath = "/storage/emulated/0/中间文件.pcm";
        int startsecond = 0;
        int endsecond = 6;
        String[] filenames = path.split("/");
        //生成工具函数的参数


        //id:  2131296416-2131296421;
        int id=1;
        Log.d("id=",String.valueOf(id));
        String decodepath = "/storage/emulated/0/00/mod_"+filenames[filenames.length-1] ;
        Log.d("触发功能：","这里可以触发！！");
        if(seqnum==0)
        {
            String  originpath = "/storage/emulated/0/res/ztest_jian.mp3";
            copyFile copyFile = new copyFile("/storage/emulated/0/res/ztest_jian.wav", "/storage/emulated/0/00/test_剪辑.mp3");
            Log.d("触发功能：","剪辑");
            seqnum+=1;
            //剪辑
        } else if (seqnum==1) {
            copyFile copyFile = new copyFile("/storage/emulated/0/res/ztest_yin.wav", "/storage/emulated/0/00/test_音量.mp3");
            Log.d("触发功能：","音量");
            seqnum+=1;
            //音量
        } else if (seqnum==2) {
            copyFile copyFile = new copyFile("/storage/emulated/0/res/ztest_jia.wav", "/storage/emulated/0/00/test_变速.mp3");
            Log.d("触发功能：","变速");
            //变速
        } else if (id== 2131296419) {
            String  originpath = "/storage/emulated/0/res/test_渲染.mp3";
            Log.d("触发功能：","其他");
            //渲染
        } else if (id== 2131296420) {
            String  originpath = "/storage/emulated/0/res/test_变调.mp3";
            Log.d("触发功能：","其他");
            //变调
        }  else if (id== 2131296421) {
            String  originpath = "/storage/emulated/0/res/test_变奏.mp3";
            Log.d("触发功能：","其他");
            //变奏
        }


        // 剪辑，提取从beginsecond到endsecond的音频
//        AudioFunction.DecodeMusicFile(path, temppath, startsecond, endsecond,
//                SettingsFragment.this);
//        convertPcm2Wav(temppath, decodepath, 44100, 2, 8);
        // 音量调节
//        short[] shorts;
//        shorts = AudioFunction.convertMp32Short(path, temppath, startsecond, endsecond,
//                44100, 2, 8, SettingsFragment.this);
//        Log.e("shorts数组", String.valueOf(shorts[200]));


//林玮雄请在这里加载你的功能类 Editmusic x=new Editmusic(path,'剪辑')
//Editmusic x=new Editmusic(path,'音量')
//Editmusic x=new Editmusic(path,'渲染')
//Editmusic x=new Editmusic(path,'变速')
//Editmusic x=new Editmusic(path,'变奏')
//Editmusic x=new Editmusic(path,'变调')
        Toast.makeText(getActivity(), "正在编辑", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void updateDecodeProgress(int decodeProgress) {

    }

    @Override
    public void decodeSuccess() {

    }
    public void refresh(){
        tv_duration2.setText(TimeUtils.formatDuration(player.getDuration()));
        tv_duration.setText (TimeUtils.formatDuration(player.getDuration()));
        player.stop();
        paused=0;
        seekBar.setProgress(0);
        player.seekTo(0);
    }

    @Override
    public void decodeFail() {

    }

    private void getProgress(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                position = player.getCurrentPosition();
                seekBar.setProgress(position);

                /* 非UI线程不能进行UI操作，否则会闪退
                TextView tv_progress;
                tv_progress=getView().findViewById(R.id.tv_progress);
                Log.e("修改:", String.valueOf(position));
                tv_progress.setText ("1:00");
                Log.e("修改:", String.valueOf(position));
                 //*/
            }
        },0,2000);
    }
}
