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

package com.foxluo.resource.music.player.contract;

import android.app.PendingIntent;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.media3.exoplayer.ExoPlayer;

import com.foxluo.resource.music.player.bean.base.BaseAlbumItem;
import com.foxluo.resource.music.player.bean.base.BaseArtistItem;
import com.foxluo.resource.music.player.bean.base.BaseMusicItem;
import com.foxluo.resource.music.player.domain.MusicDTO;

/**
 * Create by KunMinX at 18/9/24
 */
public interface IPlayController<
        B extends BaseAlbumItem<M, A>,
        M extends BaseMusicItem<A>,
        A extends BaseArtistItem>
        extends IPlayInfoManager<B, M, A> {

    void init(ExoPlayer player, ICacheProxy iCacheProxy);

    void loadAlbum(B musicAlbum, boolean actionByUser);

    void loadAlbum(B musicAlbum, int playIndex, boolean actionByUser);

    void playAudio();

    void playAudio(int albumIndex);

    void playNext();

    void playPrevious();

    void playAgain();

    void togglePlay();

    void pauseAudio();

    void resumeAudio();

    void changeMode();

    boolean isPlaying();

    boolean isPaused();

    boolean isInit();

    void setSeek(int progress);

    String getTrackTime(int progress);

    LiveData<MusicDTO<B, M, A>> getUiStates();
}
