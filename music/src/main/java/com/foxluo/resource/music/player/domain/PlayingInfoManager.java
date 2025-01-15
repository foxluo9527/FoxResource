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

import com.foxluo.resource.music.player.bean.base.BaseAlbumItem;
import com.foxluo.resource.music.player.bean.base.BaseArtistItem;
import com.foxluo.resource.music.player.bean.base.BaseMusicItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Create by KunMinX at 18/9/24
 */
public class PlayingInfoManager<B extends BaseAlbumItem<M, A>, M extends BaseMusicItem<A>, A extends BaseArtistItem> {

  private B mMusicAlbum;
  private int mPlayIndex = 0;
  private int mAlbumIndex = 0;
  private final List<M> mOriginPlayingList = new ArrayList<>();
  private final List<M> mShufflePlayingList = new ArrayList<>();
  private Enum<RepeatMode> mRepeatMode = RepeatMode.LIST_CYCLE;

  public enum RepeatMode {
    SINGLE_CYCLE,
    LIST_CYCLE,
    RANDOM
  }

  boolean isInit() {
    return mMusicAlbum != null;
  }

  private void fitShuffle() {
    mShufflePlayingList.clear();
    mShufflePlayingList.addAll(mOriginPlayingList);
    Collections.shuffle(mShufflePlayingList);
  }

  Enum<RepeatMode> changeMode() {
    if (mRepeatMode == RepeatMode.LIST_CYCLE) {
      mRepeatMode = RepeatMode.SINGLE_CYCLE;
    } else if (mRepeatMode == RepeatMode.SINGLE_CYCLE) {
      mRepeatMode = RepeatMode.RANDOM;
    } else {
      mRepeatMode = RepeatMode.LIST_CYCLE;
    }
    return mRepeatMode;
  }

  B getMusicAlbum() {
    return mMusicAlbum;
  }

  void setMusicAlbum(B musicAlbum) {
    this.mMusicAlbum = musicAlbum;
    mOriginPlayingList.clear();
    mOriginPlayingList.addAll(mMusicAlbum.musics);
    fitShuffle();
  }

  List<M> getPlayingList() {
    if (mRepeatMode == RepeatMode.RANDOM) {
      return mShufflePlayingList;
    } else {
      return mOriginPlayingList;
    }
  }

  List<M> getOriginPlayingList() {
    return mOriginPlayingList;
  }

  M getCurrentPlayingMusic() {
    if (getPlayingList().isEmpty()) {
      return null;
    }
    return getPlayingList().get(mPlayIndex);
  }

  Enum<RepeatMode> getRepeatMode() {
    return mRepeatMode;
  }

  void countPreviousIndex() {
    if (mPlayIndex == 0) {
      mPlayIndex = (getPlayingList().size() - 1);
    } else {
      --mPlayIndex;
    }
    mAlbumIndex = mOriginPlayingList.indexOf(getCurrentPlayingMusic());
  }

  void countNextIndex() {
    if (mPlayIndex == (getPlayingList().size() - 1)) {
      mPlayIndex = 0;
    } else {
      ++mPlayIndex;
    }
    mAlbumIndex = mOriginPlayingList.indexOf(getCurrentPlayingMusic());
  }

  void updateModelChangePlayIndex(){
    mPlayIndex = getPlayingList().indexOf(mOriginPlayingList.get(mAlbumIndex));
  }

  boolean removeAlbumIndex(int albumIndex) {
    boolean removeCurrentPlay = mAlbumIndex == albumIndex;
    if (removeCurrentPlay) {
      if (mPlayIndex == (getPlayingList().size() - 1)) {
        mPlayIndex = 0;
      } else {
        ++mPlayIndex;
      }
      mAlbumIndex = mOriginPlayingList.indexOf(getCurrentPlayingMusic());
      M nextMusic = mOriginPlayingList.get(mAlbumIndex);
      getOriginPlayingList().remove(albumIndex);
      fitShuffle();
      mAlbumIndex = mOriginPlayingList.indexOf(nextMusic);
    } else {
      if (albumIndex < mAlbumIndex) {
        mAlbumIndex--;
      }
      getOriginPlayingList().remove(albumIndex);
      fitShuffle();
    }
    mPlayIndex = getPlayingList().indexOf(mOriginPlayingList.get(mAlbumIndex));
    return removeCurrentPlay;
  }

  void appendPlayingList(List<M> appended){
    mOriginPlayingList.addAll(appended);
    fitShuffle();
    mPlayIndex = getPlayingList().indexOf(mOriginPlayingList.get(mAlbumIndex));
  }

  void currentAlbumIndex(int albumIndex) {
    this.mAlbumIndex = albumIndex;
    mPlayIndex = getPlayingList().indexOf(getOriginPlayingList().get(albumIndex));
  }

  int getAlbumIndex() {
    return mAlbumIndex;
  }

  void setAlbumIndex(int albumIndex) {
    mAlbumIndex = albumIndex;
    mPlayIndex = getPlayingList().indexOf(mOriginPlayingList.get(mAlbumIndex));
  }
}
