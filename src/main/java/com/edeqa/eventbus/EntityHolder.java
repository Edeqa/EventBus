/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov <tujger@gmail.com>
 */

package com.edeqa.eventbus;

import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("WeakerAccess")
public interface EntityHolder<T> {

    void setContext(T context);

    String getType();

    void start();

    void finish();

    List<String> events();

    boolean onEvent(String eventName, Object eventObject);

    void setLoggingLevel(Level level);
}
