package com;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class EditActivity_TestPlay {

    MediaPlayer mediaPlayer;
    List<String> path;
    private Context context;

    public EditActivity_TestPlay(Context context, String path)
    {
        this.context = context;
        mediaPlayer = new MediaPlayer();
        loadLocalMusicData();;

        try {
            mediaPlayer.setDataSource(path);
            playMusic();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadLocalMusicData( ){
        /* 加载本地音频文件*/
        ContentResolver resolver = context.getContentResolver();
        //获取音频地址
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        //查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        while(cursor.moveToNext()){
            path.add( cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) );
        }

        //显示结果信息
        String songs="";
        for(int i=0;i<path.size();++i)
        {
            songs+=path.get(i)+'\n';
        }
        Toast.makeText(context, "成功播放！！", Toast.LENGTH_SHORT).show();
    }

    public void playMusic(){
        if(mediaPlayer!=null && !mediaPlayer.isPlaying()){
            try {
                mediaPlayer.prepare();
                mediaPlayer.start();;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
