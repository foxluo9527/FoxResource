/*
 * Copyright 2018-2019 KunMinX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foxluo.resource.music.player.domain;

import static androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS;
import static androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND;
import static androidx.media3.common.PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE;
import static androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT;
import static com.xuexiang.xui.utils.XToastUtils.toast;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.foxluo.baselib.util.ImageExt;
import com.foxluo.baselib.util.StringUtil;
import com.foxluo.resource.music.player.bean.base.BaseAlbumItem;
import com.foxluo.resource.music.player.bean.base.BaseArtistItem;
import com.foxluo.resource.music.player.bean.base.BaseMusicItem;
import com.foxluo.resource.music.player.contract.ICacheProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Create by KunMinX at 18/9/25
 */
public class PlayerController<
        B extends BaseAlbumItem<M, A>,
        M extends BaseMusicItem<A>,
        A extends BaseArtistItem> {

    private boolean mIsChangingPlayingMusic;

    private ICacheProxy mICacheProxy;
    private final MusicDTO<B, M, A> mCurrentPlay = new MusicDTO<>();
    private final MutableLiveData<MusicDTO<B, M, A>> mUiStates = new MutableLiveData<>();

    public static ExoPlayer mPlayer;
    private final static Handler mHandler = new Handler();
    private final Runnable mProgressAction = this::updateProgress;
    private final List<MediaItem> playingList = new ArrayList<MediaItem>();
    private B currentAlbum = null;

    public void init(ExoPlayer player, ICacheProxy iCacheProxy) {
        mICacheProxy = iCacheProxy;
        mPlayer = player;
        mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                mCurrentPlay.setBuffering(playbackState == Player.STATE_BUFFERING);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    if (mCurrentPlay.getMusicId() != getCurrentPlayingMusic().musicId) {
                        setChangingPlayingMusic(true);
                    }
                    afterPlay();
                } else {
                    mCurrentPlay.setPaused(true);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                System.out.println("播放错误=>" + mCurrentPlay.getTitle() + ",errorCode:" + error.errorCode);
                toast("播放错误:" + mCurrentPlay.getTitle());
                if (error.errorCode == ERROR_CODE_IO_BAD_HTTP_STATUS
                        || error.errorCode == ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE
                        || error.errorCode == ERROR_CODE_IO_FILE_NOT_FOUND
                        || error.errorCode == ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
                ) {
                    if (isInit()) {
                        mPlayer.seekToNext();
                    }
                }
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    public boolean isInit() {
        return mPlayer != null && !mPlayer.isReleased();
    }

    public void loadAlbum(B musicAlbum) {
        setAlbum(musicAlbum, 0);
    }

    private void updateProgress() {
        if (isInit() && getCurrentPlayingMusic() != null) {
            if (mCurrentPlay.getMusicId() != getCurrentPlayingMusic().musicId) {
                mCurrentPlay.setBaseInfo(currentAlbum, getCurrentPlayingMusic());
            }
            mCurrentPlay.setNowTime(calculateTime(mPlayer.getCurrentPosition() / 1000));
            mCurrentPlay.setAllTime(calculateTime(mPlayer.getDuration() / 1000));
            mCurrentPlay.setDuration((int) mPlayer.getDuration());
            mCurrentPlay.setCacheBufferProgress(mPlayer.getBufferedPercentage());
            mCurrentPlay.setProgress((int) mPlayer.getCurrentPosition());
            mUiStates.setValue(mCurrentPlay);
            mHandler.postDelayed(mProgressAction, 1000);
        } else {
            mCurrentPlay.setNowTime(calculateTime(0));
            mCurrentPlay.setAllTime(calculateTime(0));
            mCurrentPlay.setDuration(0);
            mCurrentPlay.setCacheBufferProgress(0);
            mCurrentPlay.setProgress(0);
            mUiStates.setValue(mCurrentPlay);
        }
    }

    private void setAlbum(B musicAlbum, int albumIndex) {
        if (currentAlbum != null && currentAlbum.musics.size() == musicAlbum.musics.size()) {
            if (currentAlbum.musics.equals(musicAlbum.musics)) {
                if (isInit()) mPlayer.seekToDefaultPosition(albumIndex);
                return;
            }
        }
        currentAlbum = musicAlbum;
        playingList.clear();
        musicAlbum.musics.forEach(new Consumer<M>() {
            @Override
            public void accept(M m) {
                MediaItem mediaItem = getMusicMediaItem(m);
                if (mediaItem != null) {
                    playingList.add(mediaItem);
                }
            }
        });
        if (!isInit()) return;
        mPlayer.clearMediaItems();
        mPlayer.addMediaItems(playingList);
        mPlayer.seekToDefaultPosition(albumIndex);
        setChangingPlayingMusic(true);
    }

    public void loadAlbum(B musicAlbum, int albumIndex) {
        setAlbum(musicAlbum, albumIndex);
        playAudio();
        if (!musicAlbum.autoPlay) {
            pauseAudio();
        }
    }

    public boolean isPlaying() {
        if (!isInit()) return false;
        return mPlayer.isPlaying();
    }

    public boolean isPaused() {
        if (!isInit()) return true;
        return !mPlayer.isPlaying();
    }

    public void playAudio(int albumIndex) {
        if (!isInit()) return;
        int currentIndex = mPlayer.getCurrentMediaItemIndex();
        if (isPlaying() && albumIndex == mPlayer.getCurrentMediaItemIndex()) {
            return;
        }
        mPlayer.seekTo(albumIndex, 0L);
        setChangingPlayingMusic(true);
        playAudio();
    }

    public void playAudio() {
        if (getCurrentPlayingMusic() == null) {
            return;
        }
        if (mIsChangingPlayingMusic) {
            mPlayer.prepare();
            mPlayer.play();
        } else if (isPaused() || mCurrentPlay.getProgress() > 0) {
            resumeAudio();
        }
    }

    private Uri convertResource2Uri(int resId) {
        Resources resources = Utils.getApp().getResources();
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(
                        resId
                ) + '/' + resources.getResourceEntryName(resId)
        );
    }

    private @Nullable MediaItem getMusicMediaItem(M currentMusic) {
        String url;
        url = currentMusic.url;
        MediaItem mediaItem = null;
        try {
            if ((url.contains("http:") || url.contains("ftp:") || url.contains("https:"))) {
                String urlName = StringUtil.INSTANCE.getUrlName(url);
                MediaMetadata.Builder builder = new MediaMetadata.Builder()
                        .setTitle(currentMusic.title)
                        .setArtist(currentMusic.artist.name)
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC);
                if (currentMusic.coverImg != null && !currentMusic.coverImg.isEmpty()) {
                    builder.setArtworkUri(Uri.parse(ImageExt.INSTANCE.processUrl(currentMusic.coverImg)));
                } else {
                    builder.setArtworkUri(convertResource2Uri(com.foxluo.baselib.R.mipmap.ic_app));
                }
                MediaMetadata metadata = builder.build();
                mediaItem = new MediaItem.Builder()
                        .setUri(mICacheProxy.getCacheUrl(url))
                        .setMediaMetadata(metadata)
                        .setTag(currentMusic).build();
            } else if (url.contains("storage")) {
                mediaItem = new MediaItem.Builder().setUri(url).setTag(currentMusic).build();
            } else {
                mediaItem = new MediaItem.Builder().setUri(Uri.parse("file:///android_asset/" + url)).setTag(currentMusic).build();
            }
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            return null;
        }
        return mediaItem;
    }

    private void afterPlay() {
        setChangingPlayingMusic(false);
        mHandler.post(mProgressAction);
        mCurrentPlay.setPaused(false);
        mUiStates.setValue(mCurrentPlay);
    }

    public void setSeek(int progress) {
        mPlayer.seekTo(progress);
    }

    public String getTrackTime(int progress) {
        return calculateTime(progress / 1000);
    }

    private String calculateTime(long _time) {
        int time = (int) _time;
        int minute;
        int second;
        if (time >= 60) {
            minute = time / 60;
            second = time % 60;
            return (minute < 10 ? "0" + minute : "" + minute) + (second < 10 ? ":0" + second : ":" + second);
        } else {
            second = time;
            if (second < 10) return "00:0" + second;
            return "00:" + second;
        }
    }

    public M removeAlbumIndex(int removeIndex) {
        if (!isInit()) return null;
        mPlayer.removeMediaItem(removeIndex);
        return (M) playingList.remove(removeIndex).localConfiguration.tag;
    }

    public void appendPlayingList(List<M> list) {
        if (!isInit()) return;
        list.forEach(new Consumer<M>() {
            @Override
            public void accept(M m) {
                MediaItem mediaItem = getMusicMediaItem(m);
                if (m != null && !playingList.contains(mediaItem)) {
                    playingList.add(mediaItem);
                }
            }
        });
        mPlayer.addMediaItems(playingList);
    }

    public void playNext() {
        if (!isInit()) return;
        mPlayer.seekToNext();
        setChangingPlayingMusic(true);
        playAudio();
    }

    public void playPrevious() {
        if (!isInit()) return;
        if (mPlayer.getDuration() > 0) {
            mPlayer.seekTo(0);
        }
        mPlayer.seekToPrevious();
        setChangingPlayingMusic(true);
        playAudio();
    }

    public void playAgain() {
        setChangingPlayingMusic(true);
        playAudio();
    }

    public void pauseAudio() {
        if (!isInit()) return;
        mPlayer.pause();
        mHandler.removeCallbacks(mProgressAction);
        mCurrentPlay.setPaused(true);
        mUiStates.setValue(mCurrentPlay);
    }

    public void resumeAudio() {
        if (!isInit()) return;
        mPlayer.prepare();
        mPlayer.play();
        mHandler.post(mProgressAction);
        mCurrentPlay.setPaused(false);
        mUiStates.setValue(mCurrentPlay);
    }

    public void clear() {
        if (!isInit()) return;
        mPlayer.clearMediaItems();
        playingList.clear();
        mCurrentPlay.setPaused(true);
        mUiStates.setValue(mCurrentPlay);
        resetIsChangingPlayingChapter();
    }

    public void resetIsChangingPlayingChapter() {
        mIsChangingPlayingMusic = true;
        setChangingPlayingMusic(true);
    }

    public void changeMode() {
        if (!isInit()) return;
        if (mPlayer.getShuffleModeEnabled()) {
            mPlayer.setShuffleModeEnabled(false);
            mPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
            mCurrentPlay.setRepeatMode(PlayingInfoManager.RepeatMode.LIST_CYCLE);
        } else if (mPlayer.getRepeatMode() == Player.REPEAT_MODE_ALL) {
            mPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            mCurrentPlay.setRepeatMode(PlayingInfoManager.RepeatMode.SINGLE_CYCLE);
        } else {
            mPlayer.setShuffleModeEnabled(true);
            mPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
            mCurrentPlay.setRepeatMode(PlayingInfoManager.RepeatMode.RANDOM);
        }
        mUiStates.setValue(mCurrentPlay);
    }

    public B getAlbum() {
        return currentAlbum;
    }

    public List<M> getAlbumMusics() {
        ArrayList<M> albumMusics = new ArrayList<>();
        playingList.forEach(new Consumer<MediaItem>() {
            @Override
            public void accept(MediaItem mediaItem) {
                albumMusics.add((M) mediaItem.localConfiguration.tag);
            }
        });
        return albumMusics;
    }

    public void setChangingPlayingMusic(boolean changingPlayingMusic) {
        mIsChangingPlayingMusic = changingPlayingMusic;
        if (mIsChangingPlayingMusic) {
            mCurrentPlay.setBaseInfo(currentAlbum, getCurrentPlayingMusic());
            mCurrentPlay.setNowTime("00:00");
            mCurrentPlay.setAllTime("00:00");
            mCurrentPlay.setProgress(0);
            mCurrentPlay.setDuration(0);
            mUiStates.setValue(mCurrentPlay);
        }
    }

    public int getAlbumIndex() {
        if (!isInit()) return 0;
        return mPlayer.getCurrentMediaItemIndex();
    }

    public Enum<PlayingInfoManager.RepeatMode> getRepeatMode() {
        if (!isInit()) return PlayingInfoManager.RepeatMode.LIST_CYCLE;
        if (mPlayer.getShuffleModeEnabled()) {
            return PlayingInfoManager.RepeatMode.RANDOM;
        } else if (mPlayer.getRepeatMode() == Player.REPEAT_MODE_ONE) {
            return PlayingInfoManager.RepeatMode.SINGLE_CYCLE;
        } else {
            return PlayingInfoManager.RepeatMode.LIST_CYCLE;
        }
    }

    public void togglePlay() {
        if (!isInit()) return;
        if (isPlaying() || mCurrentPlay.isPaused() == false) pauseAudio();
        else playAudio();
    }

    @Nullable
    public M getCurrentPlayingMusic() {
        if (!isInit()) return null;
        if (mPlayer.getCurrentMediaItem() == null) {
            return null;
        }
        return (M) mPlayer.getCurrentMediaItem().localConfiguration.tag;
    }

    public LiveData<MusicDTO<B, M, A>> getUiStates() {
        return mUiStates;
    }
}
