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

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.room.Room;

import com.blankj.utilcode.util.Utils;
import com.foxluo.resource.music.data.bean.AlbumData;
import com.foxluo.resource.music.data.bean.ArtistData;
import com.foxluo.resource.music.data.bean.MusicData;
import com.foxluo.resource.music.data.db.AppDatabase;
import com.foxluo.resource.music.player.contract.ICacheProxy;
import com.foxluo.resource.music.player.contract.IPlayController;
import com.foxluo.resource.music.player.domain.MusicDTO;
import com.foxluo.resource.music.player.domain.PlayerController;
import com.foxluo.resource.music.player.domain.PlayingInfoManager;

import java.util.List;

import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;

/**
 * Create by KunMinX at 19/10/31
 */
public class PlayerManager implements IPlayController<AlbumData, MusicData, ArtistData> {
    private final AppDatabase db = Room.databaseBuilder(Utils.getApp(), AppDatabase.class, "fox_resource_db").build();

    private InitCallback initCallback;

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
    public void init(ExoPlayer player, ICacheProxy iCacheProxy) {
        mController.init(player, iCacheProxy);
        if (initCallback!=null){
            initCallback.initialized();
        }
    }

    public void setInitCallback(InitCallback callback){
        this.initCallback = callback;
        if (mController.isInit()){
            callback.initialized();
        }
    }

    @Override
    public void loadAlbum(@NonNull AlbumData musicAlbum, boolean actionByUser) {
        int curMusicId = musicAlbum.getCurMusicId();
        int position = curMusicId;
        for (int i = 0; i < musicAlbum.getMusics().size(); i++) {
            if (musicAlbum.getMusics().get(i).getMusicId().equals(String.valueOf(curMusicId))) {
                position = i;
                break;
            }
        }
        if (actionByUser) {
            db.albumDao().updateAlbumWithMusicsJava(musicAlbum, db.musicDao());
        }
        mController.loadAlbum(musicAlbum, position);
    }

    @Override
    public void loadAlbum(AlbumData musicAlbum, int playIndex, boolean actionByUser) {
        int curMusicId = Integer.valueOf(musicAlbum.getMusics().get(playIndex).getMusicId());
        musicAlbum.setCurMusicId(curMusicId);
        loadAlbum(musicAlbum, actionByUser);
    }

    @Override
    public void playAudio() {
        mController.playAudio();
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
    @Nullable
    public MusicData getCurrentPlayingMusic() {
        return mController.getCurrentPlayingMusic();
    }

    @Override
    public MusicData removeAlbumIndex(int index) {
        return mController.removeAlbumIndex(index);
    }

    @Override
    public void appendPlayList(List<MusicData> list) {
        mController.appendPlayingList(list);
    }

    @Override
    public void clearPlayList() {
        mController.clear();
    }
}
