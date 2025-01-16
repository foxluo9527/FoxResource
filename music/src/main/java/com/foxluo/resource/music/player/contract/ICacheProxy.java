package com.foxluo.resource.music.player.contract;

import com.danikula.videocache.HttpProxyCacheServer;

/**
 * Create by KunMinX at 2021/6/11
 */
public interface ICacheProxy {
  String getCacheUrl(String url);
  HttpProxyCacheServer getHttpProxy();
}
