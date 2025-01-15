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

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.Utils;
import com.danikula.videocache.CacheListener;
import com.foxluo.baselib.ui.BaseApplication;
import com.foxluo.resource.music.player.bean.base.BaseAlbumItem;
import com.foxluo.resource.music.player.bean.base.BaseArtistItem;
import com.foxluo.resource.music.player.bean.base.BaseMusicItem;
import com.foxluo.resource.music.player.contract.ICacheProxy;
import com.foxluo.resource.music.player.contract.IServiceNotifier;

import java.io.File;
import java.net.URL;
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

  private MediaPlayer mPlayer;
  private final static Handler mHandler = new Handler();
  private final Runnable mProgressAction = this::updateProgress;
  private CacheListener lastCacheListener = null;

  public void init(Context context, IServiceNotifier iServiceNotifier, ICacheProxy iCacheProxy) {
    mIServiceNotifier = iServiceNotifier;
    mICacheProxy = iCacheProxy;
    mPlayer = new MediaPlayer();
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
    mCurrentPlay.setDuration(mPlayer.getDuration());
    mCurrentPlay.setProgress(mPlayer.getCurrentPosition());
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
    if (mIsChangingPlayingMusic) getUrlAndPlay();
    else if (isPaused() || mCurrentPlay.getProgress() > 0) resumeAudio();
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
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.reset();
        if ((url.contains("http:") || url.contains("ftp:") || url.contains("https:"))) {
          String urlPath = new URL(url).getPath().toLowerCase();
          String urlName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
          //清除未缓存完成的文件，因为服务器是转发的，每次获取的内容都不一样，缓存框架会追加写入已缓存的部分后面，导致文件错误
          //todo 服务器转发文件待修改
          String filePath = SPUtils.getInstance().getString(urlName);
          File cacheFile = new File(filePath);
          int cachedPercent = SPUtils.getInstance().getInt(urlName + "-percent", 0);
          if (cacheFile.isFile() && cacheFile.exists() && cachedPercent < 100) {
            try {
              cacheFile.delete();
              System.out.println("删除未完成缓存文件：" + cacheFile.getName());
            } catch (Exception e) {
              System.out.println(e.getMessage());
            }
          } else if (BaseApplication.proxy.isCached(url)) {
            mCurrentPlay.setCacheBufferProgress(100);
          }
          mPlayer.setDataSource(mICacheProxy.getCacheUrl(url));
          if (lastCacheListener != null) {
            BaseApplication.proxy.unregisterCacheListener(lastCacheListener);
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
          BaseApplication.proxy.registerCacheListener(lastCacheListener, url);
        } else if (url.contains("storage")) {
          mPlayer.setDataSource(url);
        } else {
          mPlayer.setDataSource(Utils.getApp(), Uri.parse("file:///android_asset/" + url));
        }
        mPlayer.prepareAsync();
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
          @Override
          public void onPrepared(MediaPlayer iMediaPlayer) {
            iMediaPlayer.start();
            iMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
              @Override
              public void onCompletion(MediaPlayer iMediaPlayer) {
                System.out.println("播放完成");
                if (getRepeatMode() == PlayingInfoManager.RepeatMode.SINGLE_CYCLE) playAgain();
                else playNext();
              }
            });
          }
        });
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
          @Override
          public boolean onError(MediaPlayer mp, int what, int extra) {
            System.out.println("播放错误");
            if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT || (what == 1 && extra == -2147483648)) {
              if (getRepeatMode() == PlayingInfoManager.RepeatMode.SINGLE_CYCLE) playAgain();
              else playNext();
            }
            return true;
          }
        });
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
    mPlayer.start();
    mHandler.post(mProgressAction);
    mCurrentPlay.setPaused(false);
    mUiStates.setValue(mCurrentPlay);
    if (mIServiceNotifier != null) mIServiceNotifier.notifyService(true);
  }

  public void clear() {
    mPlayer.stop();
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
