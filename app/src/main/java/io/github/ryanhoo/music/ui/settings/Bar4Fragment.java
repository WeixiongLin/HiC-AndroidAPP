package io.github.ryanhoo.music.ui.settings;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import butterknife.BindView;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.ui.details.PlayListDetailsActivity;
import io.github.ryanhoo.music.ui.main.copyFile;

/**
 * A simple {@link Fragment} subclass.
 */

public class Bar4Fragment extends Fragment {

    TextView textView;
    SeekBar seekBar;
    public Bar4Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bar4, container, false);
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

        //确定按钮，传回设定的参数

        getView().findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int rate=seekBar.getProgress();
                String path = PlayListDetailsActivity.current_songpath;
                if(SettingsFragment.curversion!=0)
                    path =getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion)+".wav";
                SettingsFragment.curversion+=1;
                String temppath = getContext().getFilesDir() + "/tmp.pcm";
                String decodepath = getContext().getFilesDir() + "/"+String.valueOf(SettingsFragment.curversion) +".wav";

                Log.e("修改，变调比例",String.valueOf(rate));
                Log.e("修改，歌曲路径",path);
                Log.e("修改，编辑后的歌曲路径",decodepath);
                Log.e("修改，起止时间",String.valueOf(SettingsFragment.starttime)+" "+String.valueOf(SettingsFragment.endtime));

                copyFile copyFile = new copyFile("/storage/emulated/0/res/"+String.valueOf(SettingsFragment.curversion)+".wav", decodepath);

//前端实现与验证↑，后端实现↓
/*                // 变调比率
                int rate = seekBar.getProgress();

                double[] data= {-0.35668879080953375, -0.6118094913035987, 0.8534269560320435, -0.6699697478438837, 0.35425500561437717,
                        0.8910250650549392, -0.025718699518642918, 0.07649691490732002};
                FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
                org.apache.commons.math3.complex.Complex[] result = fft.transform(data, TransformType.FORWARD);
                for (org.apache.commons.math3.complex.Complex complex : result) {
                    System.out.println(complex.abs());
                }
*/
                Bundle bundle=new Bundle();
                bundle.putInt("tonerate",rate);
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar4Fragment_to_bar1Fragment,bundle);
                Toast.makeText(getActivity(),"音调调节成功",Toast.LENGTH_SHORT).show();
            }
        });

        //取消按钮，直接返回
        getView().findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController controller= Navigation.findNavController(v);
                controller.navigate(R.id.action_bar4Fragment_to_bar1Fragment);
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
