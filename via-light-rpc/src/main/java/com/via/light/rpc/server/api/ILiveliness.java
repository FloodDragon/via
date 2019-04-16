package com.via.light.rpc.server.api;

import java.util.Collection;

/**
 * Created by liuj-ai on 2019/1/21.
 */
public interface ILiveliness<O> {

    Collection<O> living();
}
