package io.github.ryanhoo.music.ui.local.all;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.ryanhoo.music.R;
import io.github.ryanhoo.music.RxBus;
import io.github.ryanhoo.music.data.model.Song;
import io.github.ryanhoo.music.data.source.AppRepository;
import io.github.ryanhoo.music.event.PlayListUpdatedEvent;
import io.github.ryanhoo.music.event.PlaySongEvent;
import io.github.ryanhoo.music.ui.base.BaseFragment;
import io.github.ryanhoo.music.ui.base.adapter.OnItemClickListener;
import io.github.ryanhoo.music.ui.common.DefaultDividerDecoration;
import io.github.ryanhoo.music.ui.details.PlayListDetailsActivity;
import io.github.ryanhoo.music.ui.widget.RecyclerViewFastScroller;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import java.util.List;

/**
 * Created with Android Studio.
 * User: ryan.hoo.j@gmail.com
 * Date: 9/1/16
 * Time: 9:58 PM
 * Desc: LocalFilesFragment
 */
public class AllLocalMusicFragment extends BaseFragment implements LocalMusicContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;    //滚动页面
    @BindView(R.id.fast_scroller)
    RecyclerViewFastScroller fastScroller; //右侧滚轮
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;     //进度条没有visible的，不显示
    @BindView(R.id.text_view_empty)
    View emptyView;              //没有歌曲时显示无歌曲

    LocalMusicAdapter mAdapter;  //是一个List<song>
    LocalMusicContract.Presenter mPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_local_music, container, false);
    }//设置页面、简单容器、无bundle保存数据

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mAdapter = new LocalMusicAdapter(getActivity(), null);
        //(context,song),传入当前函数的上下文，song就是歌曲的地址和其他信息打包的结构体.到mAdapter里面去读已经读好的文件，注意读文件并不在此，在后面
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) { //点击音频项目，读取音乐文件并且播放了！position哪来的？
                Song song = mAdapter.getItem(position);   //读取歌曲，见LocalMusicAdapter。但不是读文件
                PlayListDetailsActivity.current_songpath=song.getPath();
                Log.e("修改2","搜索选择的歌曲路径："+song.getPath());

                RxBus.getInstance().post(new PlaySongEvent(song)); //播放音乐
            }
        });
        recyclerView.setAdapter(mAdapter);//Adapter里面的数据是怎么得到MusicPresenter读到的文件的？ 靠这个setAdapter吗
        recyclerView.addItemDecoration(new DefaultDividerDecoration());

        fastScroller.setRecyclerView(recyclerView);

        new LocalMusicPresenter(AppRepository.getInstance(), this).loadLocalMusic();
        //这里读取了内存音乐文件！
    }

    // RxBus Events

    @Override
    protected Subscription subscribeEvents() {
        return RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o instanceof PlayListUpdatedEvent) {
                            mPresenter.loadLocalMusic();
                        }
                    }
                })
                .subscribe();
    }

    // MVP View

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void emptyView(boolean visible) {
        emptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        fastScroller.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void handleError(Throwable error) {
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocalMusicLoaded(List<Song> songs) {
        mAdapter.setData(songs);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(LocalMusicContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
