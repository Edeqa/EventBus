/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017-18 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov <tujger@gmail.com>
 */

package com.edeqa.eventbus;

import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("WeakerAccess")
public interface EntityHolder {

    String getType();

    List<String> events();

    void start() throws Exception;

    void finish() throws Exception;

    boolean onEvent(String eventName, Object eventObject) throws Exception;

    boolean onEvent(PostEvent postEvent) throws Exception;

    void setLoggingLevel(Level level);
}
