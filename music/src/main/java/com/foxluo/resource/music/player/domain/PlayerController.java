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

import static com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS;
import static com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND;
import static com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE;
import static com.google.android.exoplayer2.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT;
import static com.xuexiang.xui.utils.XToastUtils.toast;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.session.MediaSession;

import com.blankj.utilcode.util.SPUtils;
import com.danikula.videocache.CacheListener;
import com.foxluo.baselib.util.StringUtil;
import com.foxluo.resource.music.player.bean.base.BaseAlbumItem;
import com.foxluo.resource.music.player.bean.base.BaseArtistItem;
import com.foxluo.resource.music.player.bean.base.BaseMusicItem;
import com.foxluo.resource.music.player.contract.ICacheProxy;
import com.foxluo.resource.music.player.contract.IServiceNotifier;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;

import java.io.File;
import java.util.List;

/**
 * Create by KunMinX at 18/9/25
 */
public class PlayerController<
        B extends BaseAlbumItem<M, A>,
        M extends BaseMusicItem<A>,
        A extends BaseArtistItem> {

  private final PlayingInfoManager<B, M, A> mPlayingInfoManager = new PlayingInfoManager<>();
  private boolean mIsChangingPlayingMusic;

  private ICacheProxy mICacheProxy;
  private IServiceNotifier mIServiceNotifier;
  private final com.foxluo.resource.music.player.domain.MusicDTO<B, M, A> mCurrentPlay = new com.foxluo.resource.music.player.domain.MusicDTO<>();
  private final MutableLiveData<com.foxluo.resource.music.player.domain.MusicDTO<B, M, A>> mUiStates = new MutableLiveData<>();

  private ExoPlayer mPlayer;
  private final static Handler mHandler = new Handler();
  private final Runnable mProgressAction = this::updateProgress;
  private CacheListener lastCacheListener = null;
  public void init(Context context, IServiceNotifier iServiceNotifier, ICacheProxy iCacheProxy) {
    mIServiceNotifier = iServiceNotifier;
    mICacheProxy = iCacheProxy;
    AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build();
    mPlayer = new ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .setAudioAttributes(audioAttributes, true)
            .build();
  }

  public boolean isInit() {
    return mPlayingInfoManager.isInit();
  }

  public void loadAlbum(B musicAlbum) {
    setAlbum(musicAlbum, 0);
  }

  private void updateProgress() {
    mCurrentPlay.setNowTime(calculateTime(mPlayer.getCurrentPosition() / 1000));
    mCurrentPlay.setAllTime(calculateTime(mPlayer.getDuration() / 1000));
    mCurrentPlay.setDuration((int) mPlayer.getDuration());
    mCurrentPlay.setProgress((int) mPlayer.getCurrentPosition());
    mUiStates.setValue(mCurrentPlay);
    mHandler.postDelayed(mProgressAction, 1000);
  }

  private void setAlbum(B musicAlbum, int albumIndex) {
    mPlayingInfoManager.setMusicAlbum(musicAlbum);
    mPlayingInfoManager.setAlbumIndex(albumIndex);
    setChangingPlayingMusic(true);
  }

  public void loadAlbum(B musicAlbum, int albumIndex) {
    setAlbum(musicAlbum, albumIndex);
    playAudio();
  }

  public boolean isPlaying() {
    return mPlayer.isPlaying();
  }

  public boolean isPaused() {
    return !mPlayer.isPlaying();
  }

  public void playAudio(int albumIndex) {
    if (isPlaying() && albumIndex == mPlayingInfoManager.getAlbumIndex()) {
      return;
    }
    mPlayingInfoManager.setAlbumIndex(albumIndex);
    setChangingPlayingMusic(true);
    playAudio();
  }

  public void playAudio() {
    if (getCurrentPlayingMusic() == null) {
      return;
    }
    if (mIsChangingPlayingMusic) {
      getUrlAndPlay();
    } else if (isPaused() || mCurrentPlay.getProgress() > 0) {
      resumeAudio();
    }
  }

  public void reloadAudio() {
    if (getCurrentPlayingMusic() == null) {
      return;
    }
    setChangingPlayingMusic(true);
//    String filePath = SPUtils.getInstance().getString(StringUtil.INSTANCE.getUrlName(getCurrentPlayingMusic().url));
//    File cacheFile = new File(filePath);
//    try {
//      cacheFile.delete();
//      System.out.println("重新加载缓存文件：" + cacheFile.getName());
//    } catch (Exception e) {
//      System.out.println(e.getMessage());
//    }
    playAudio();
  }

  private void getUrlAndPlay() {
    String url;
    M freeMusic;
    freeMusic = mPlayingInfoManager.getCurrentPlayingMusic();
    url = freeMusic.url;
    if (TextUtils.isEmpty(url)) {
      pauseAudio();
    } else {
      try {
        if ((url.contains("http:") || url.contains("ftp:") || url.contains("https:"))) {
          String urlName = StringUtil.INSTANCE.getUrlName(url);
          if (mICacheProxy.getHttpProxy().isCached(url)) {
            mCurrentPlay.setCacheBufferProgress(100);
          }
          MediaItem item = MediaItem.fromUri(mICacheProxy.getCacheUrl(url));
          mPlayer.setMediaItem(item);
          if (lastCacheListener != null) {
            mICacheProxy.getHttpProxy().unregisterCacheListener(lastCacheListener);
          }
          lastCacheListener = new CacheListener() {
            @Override
            public void onCacheAvailable(File cacheFile, String url, int percent) {
              System.out.println("缓冲==>" + percent + "%");
              mCurrentPlay.setCacheBufferProgress(percent);
              if (mCurrentPlay.isPaused()) {//暂停也发送缓存进度
                mUiStates.setValue(mCurrentPlay);
              }
              SPUtils.getInstance().put(urlName, cacheFile.getAbsolutePath());
              SPUtils.getInstance().put(urlName + "-percent", percent);
            }
          };
          mICacheProxy.getHttpProxy().registerCacheListener(lastCacheListener, url);
        } else if (url.contains("storage")) {
          MediaItem item = MediaItem.fromUri(url);
          mPlayer.setMediaItem(item);
        } else {
          MediaItem item = MediaItem.fromUri(Uri.parse("file:///android_asset/" + url));
          mPlayer.setMediaItem(item);
        }
        mPlayer.prepare();
        mPlayer.addListener(new Player.Listener() {
          @Override
          public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            if (playbackState == Player.STATE_ENDED) {
              System.out.println("播放完成");
              if (getRepeatMode() == PlayingInfoManager.RepeatMode.SINGLE_CYCLE) playAgain();
              else playNext();
            } else if (playbackState == Player.STATE_READY) {
              if (mIServiceNotifier != null) mIServiceNotifier.notifyService(true);
            }
            mCurrentPlay.setBuffering(playbackState == Player.STATE_BUFFERING);
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
              if (getRepeatMode() == PlayingInfoManager.RepeatMode.SINGLE_CYCLE) playAgain();
              else playNext();
            }
          }
        });
        mPlayer.setPlayWhenReady(true);
        mPlayer.play();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      afterPlay();
    }
  }

  private void afterPlay() {
    setChangingPlayingMusic(false);
    mHandler.post(mProgressAction);
    mCurrentPlay.setPaused(false);
    mUiStates.setValue(mCurrentPlay);
    if (mIServiceNotifier != null) mIServiceNotifier.notifyService(true);
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

  public boolean removeAlbumIndex(int removeIndex) {
    if (mPlayingInfoManager.removeAlbumIndex(removeIndex)) {
      setChangingPlayingMusic(true);
      playAudio();
      return true;
    }
    return false;
  }

  public void appendPlayingList(List<M> list) {
    mPlayingInfoManager.appendPlayingList(list);
  }

  public void currentPlayAlbumIndex(int currentIndex) {
    mPlayingInfoManager.currentAlbumIndex(currentIndex);
    setChangingPlayingMusic(true);
    playAudio();
  }

  public void playNext() {
    mPlayingInfoManager.countNextIndex();
    setChangingPlayingMusic(true);
    playAudio();
  }

  public void playPrevious() {
    mPlayingInfoManager.countPreviousIndex();
    setChangingPlayingMusic(true);
    playAudio();
  }

  public void playAgain() {
    setChangingPlayingMusic(true);
    playAudio();
  }

  public void pauseAudio() {
    mPlayer.pause();
    mHandler.removeCallbacks(mProgressAction);
    mCurrentPlay.setPaused(true);
    mUiStates.setValue(mCurrentPlay);
    if (mIServiceNotifier != null) mIServiceNotifier.notifyService(true);
  }

  public void resumeAudio() {
    mPlayer.play();
    mHandler.post(mProgressAction);
    mCurrentPlay.setPaused(false);
    mUiStates.setValue(mCurrentPlay);
    if (mIServiceNotifier != null) mIServiceNotifier.notifyService(true);
  }

  public void clear() {
    mPlayer.stop();
    mPlayer.clearMediaItems();
    mPlayer.release();
    mCurrentPlay.setPaused(true);
    mUiStates.setValue(mCurrentPlay);
    resetIsChangingPlayingChapter();
    if (mIServiceNotifier != null) mIServiceNotifier.notifyService(false);
  }

  public void resetIsChangingPlayingChapter() {
    mIsChangingPlayingMusic = true;
    setChangingPlayingMusic(true);
  }

  public void changeMode() {
    mCurrentPlay.setRepeatMode(mPlayingInfoManager.changeMode());
    mPlayingInfoManager.updateModelChangePlayIndex();
    mUiStates.setValue(mCurrentPlay);
  }

  public B getAlbum() {
    return mPlayingInfoManager.getMusicAlbum();
  }

  public List<M> getAlbumMusics() {
    return mPlayingInfoManager.getOriginPlayingList();
  }

  public void setChangingPlayingMusic(boolean changingPlayingMusic) {
    mIsChangingPlayingMusic = changingPlayingMusic;
    if (mIsChangingPlayingMusic) {
      mCurrentPlay.setBaseInfo(mPlayingInfoManager.getMusicAlbum(), getCurrentPlayingMusic());
      mCurrentPlay.setNowTime("00:00");
      mCurrentPlay.setAllTime("00:00");
      mCurrentPlay.setProgress(0);
      mCurrentPlay.setDuration(0);
      mUiStates.setValue(mCurrentPlay);
    }
  }

  public int getAlbumIndex() {
    return mPlayingInfoManager.getAlbumIndex();
  }

  public Enum<PlayingInfoManager.RepeatMode> getRepeatMode() {
    return mPlayingInfoManager.getRepeatMode();
  }

  public void togglePlay() {
    if (isPlaying() || mCurrentPlay.isPaused() == false) pauseAudio();
    else playAudio();
  }

  public M getCurrentPlayingMusic() {
    return mPlayingInfoManager.getCurrentPlayingMusic();
  }

  public LiveData<MusicDTO<B, M, A>> getUiStates() {
    return mUiStates;
  }
}
