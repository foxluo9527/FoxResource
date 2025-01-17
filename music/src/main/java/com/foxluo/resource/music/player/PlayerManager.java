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

package com.foxluo.resource.music.player;

import static android.media.MediaMetadata.METADATA_KEY_ARTIST;
import static android.media.MediaMetadata.METADATA_KEY_DURATION;
import static android.media.MediaMetadata.METADATA_KEY_TITLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

import androidx.lifecycle.LiveData;

import com.foxluo.resource.music.data.bean.AlbumData;
import com.foxluo.resource.music.data.bean.ArtistData;
import com.foxluo.resource.music.data.bean.MusicData;
import com.foxluo.resource.music.player.contract.ICacheProxy;
import com.foxluo.resource.music.player.contract.IPlayController;
import com.foxluo.resource.music.player.contract.IServiceNotifier;
import com.foxluo.resource.music.player.domain.MusicDTO;
import com.foxluo.resource.music.player.domain.PlayerController;
import com.foxluo.resource.music.player.domain.PlayingInfoManager;

import java.util.List;

/**
 * Create by KunMinX at 19/10/31
 */
public class PlayerManager implements IPlayController<AlbumData, MusicData, ArtistData> {

  @SuppressLint("StaticFieldLeak")
  private static final PlayerManager sManager = new PlayerManager();

  private final PlayerController<AlbumData, MusicData, ArtistData> mController;

  private PlayerManager() {
    mController = new PlayerController<>();
  }

  public static PlayerManager getInstance() {
    return sManager;
  }

  @Override
  public void init(Context context, IServiceNotifier iServiceNotifier, ICacheProxy iCacheProxy) {
    mController.init(context, iServiceNotifier, iCacheProxy);
  }

  @Override
  public void loadAlbum(AlbumData musicAlbum) {
    mController.loadAlbum(musicAlbum);
  }

  @Override
  public void loadAlbum(AlbumData musicAlbum, int playIndex) {
    mController.loadAlbum(musicAlbum, playIndex);
  }

  @Override
  public void playAudio() {
    mController.playAudio();
  }

  public void reloadAudio() {
    mController.reloadAudio();
  }

  @Override
  public void playAudio(int albumIndex) {
    mController.playAudio(albumIndex);
  }

  @Override
  public void playNext() {
    mController.playNext();
  }

  @Override
  public void playPrevious() {
    mController.playPrevious();
  }

  @Override
  public void playAgain() {
    mController.playAgain();
  }

  @Override
  public void pauseAudio() {
    mController.pauseAudio();
  }

  @Override
  public void resumeAudio() {
    mController.resumeAudio();
  }

  @Override
  public void clear() {
    mController.clear();

  }

  @Override
  public void changeMode() {
    mController.changeMode();
  }

  @Override
  public boolean isPlaying() {
    return mController.isPlaying();
  }

  @Override
  public boolean isPaused() {
    return mController.isPaused();
  }

  @Override
  public boolean isInit() {
    return mController.isInit();
  }

  @Override
  public void setSeek(int progress) {
    mController.setSeek(progress);
  }

  @Override
  public String getTrackTime(int progress) {
    return mController.getTrackTime(progress);
  }

  @Override
  public LiveData<MusicDTO<AlbumData, MusicData, ArtistData>> getUiStates() {
    return mController.getUiStates();
  }

  @Override
  public AlbumData getAlbum() {
    return mController.getAlbum();
  }

  @Override
  public List<MusicData> getAlbumMusics() {
    return mController.getAlbumMusics();
  }

  @Override
  public void setChangingPlayingMusic(boolean changingPlayingMusic) {
    mController.setChangingPlayingMusic(changingPlayingMusic);
  }

  @Override
  public int getAlbumIndex() {
    return mController.getAlbumIndex();
  }

  @Override
  public Enum<PlayingInfoManager.RepeatMode> getRepeatMode() {
    return mController.getRepeatMode();
  }

  @Override
  public void togglePlay() {
    mController.togglePlay();
  }

  @Override
  public MusicData getCurrentPlayingMusic() {
    return mController.getCurrentPlayingMusic();
  }

  /**
   * 向媒体会话设置播放歌曲信息
   *
   * @param mediaSession
   * @param context
   */
  public void setMediaSessionData(MediaSession mediaSession, Context context) {
    if (getCurrentPlayingMusic() == null) {
      return;
    }
    int playbackState;
    if (getUiStates().getValue().isBuffering())
      playbackState = PlaybackState.STATE_BUFFERING;
    else if (isPlaying())
      playbackState = PlaybackState.STATE_PLAYING;
    else
      playbackState = PlaybackState.STATE_PAUSED;
    float speed = 0f;
    if (isPlaying()) {
      speed = 1f;
    }
    long duration = getUiStates().getValue().getDuration();
    long cached = duration / 100 * getUiStates().getValue().getCacheBufferProgress();
    long progress = getUiStates().getValue().getProgress();
    mediaSession.setPlaybackState(
            new PlaybackState
                    .Builder()
                    .setBufferedPosition(cached)
                    .setState(playbackState, duration, speed, progress)
                    .build()
    );
    String artist = "未知艺术家";
    if (getCurrentPlayingMusic().artist != null) {
      artist = getCurrentPlayingMusic().artist.name;
    }
    mediaSession.setMetadata(new MediaMetadata.Builder()
            .putText(METADATA_KEY_TITLE, getCurrentPlayingMusic().title)
            .putText(METADATA_KEY_ARTIST, artist)
            .putLong(METADATA_KEY_DURATION, duration)
            .build());
  }

  public boolean removeAlbumIndex(int index) {
   return mController.removeAlbumIndex(index);
  }

  public void currentAlbumIndex(int index) {
    mController.currentPlayAlbumIndex(index);
  }

  public void appendPlayList(List<MusicData> list) {
    mController.appendPlayingList(list);
  }
}
