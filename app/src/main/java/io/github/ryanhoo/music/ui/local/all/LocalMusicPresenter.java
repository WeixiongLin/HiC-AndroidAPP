package io.github.ryanhoo.music.ui.local.all;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.util.Log;
import io.github.ryanhoo.music.data.model.Song;
import io.github.ryanhoo.music.data.source.AppRepository;
import io.github.ryanhoo.music.utils.FileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/13/16
 * Time: 8:36 PM
 * Desc: LocalMusicPresenter
 */
//就是这里了！为上层AllLocalMusicFragment提供音频文件读取，调用的是下层FileUtils
public class LocalMusicPresenter implements LocalMusicContract.Presenter, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "LocalMusicPresenter";

    private static final int URL_LOAD_LOCAL_MUSIC = 0;
    private static final Uri MEDIA_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String WHERE = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
            + MediaStore.Audio.Media.SIZE + ">0";
    private static final String ORDER_BY = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";
    private static String[] PROJECTIONS = {
            MediaStore.Audio.Media.DATA, // the real path
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.IS_RINGTONE,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.IS_NOTIFICATION,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
    };

    private LocalMusicContract.View mView;
    private AppRepository mRepository;
    private CompositeSubscription mSubscriptions;

    public LocalMusicPresenter(AppRepository repository, LocalMusicContract.View view) {
        mView = view;
        mRepository = repository;
        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadLocalMusic();
    }

    @Override
    public void unsubscribe() {
        mView = null;
        mSubscriptions.clear();
    }

    @Override
    public void loadLocalMusic() {
        mView.showProgress();//接口的实现在上级AllLocalMusicFragment,共用了接口可以与上层类互相调用，不错
        mView.getLoaderManager().initLoader(URL_LOAD_LOCAL_MUSIC, null, this);
        //这已经是库了？ 读回来的文件信息存哪呢？ 还是只是个初始化，信息在下面的Loader<Cursor>里面
    }
//一百行的函数完成了音乐文件读取，读取的工具函数在160行之后才真正读取
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != URL_LOAD_LOCAL_MUSIC) return null;

        return new CursorLoader(
                mView.getContext(),
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY
        );
    }

    @Override  //这里调用了后面实际读取mp3文件的函数获得了song实体，Observer和subscriber的办法处理数据变化
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Subscription subscription = Observable.just(cursor)
                .flatMap(new Func1<Cursor, Observable<List<Song>>>() {
                    @Override
                    public Observable<List<Song>> call(Cursor cursor) {
                        List<Song> songs = new ArrayList<>();
                        if (cursor != null && cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            do {
                                Song song = cursorToMusic(cursor);  //这里得到了mp3文件了
                                songs.add(song);
                            } while (cursor.moveToNext());
                        }
                        return mRepository.insert(songs);
                    }
                })
                .doOnNext(new Action1<List<Song>>() {
                    @Override
                    public void call(List<Song> songs) {
                        Log.d(TAG, "onLoadFinished: " + songs.size());
                        Collections.sort(songs, new Comparator<Song>() {
                            @Override
                            public int compare(Song left, Song right) {
                                return left.getDisplayName().compareTo(right.getDisplayName());
                            }
                        });

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Song>>() {
                    @Override
                    public void onStart() {
                        mView.showProgress();
                    }

                    @Override
                    public void onCompleted() {
                        mView.hideProgress();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        mView.hideProgress();
                        Log.e(TAG, "onError: ", throwable);
                    }

                    @Override
                    public void onNext(List<Song> songs) {
                        mView.onLocalMusicLoaded(songs);
                        mView.emptyView(songs.isEmpty());
                    }
                });
        mSubscriptions.add(subscription);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Empty
    }
//到此为止反正先初始化Loader，再用cursor读文本，现在要用路径信息读取mp3文件了！
    private Song cursorToMusic(Cursor cursor) {
        String realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        File songFile = new File(realPath);
        //关键！  cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));是path!
        Song song;
        if (songFile.exists()) {
            // Using song parsed from file to avoid encoding problems
            song = FileUtils.fileToMusic(songFile); // Path->song
            Log.d("路径的类型是", String.valueOf(realPath.getClass()));
            Log.d("路径长成这样！！", String.valueOf(realPath));
            if (song != null) {
                return song;
            }
        }   //这一段函数
        song = new Song();
        song.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)));
        String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
        if (displayName.endsWith(".mp3")) {
            displayName = displayName.substring(0, displayName.length() - 4);
        }
        song.setDisplayName(displayName);
        song.setArtist(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
        song.setAlbum(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)));
        song.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
        song.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
        song.setSize(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
        return song;
    }
}
