/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov <tujger@gmail.com>
 */

package com.edeqa.eventbus;

import java.util.List;

abstract public class AbstractEntityHolder<T> {

    public static final String PRINT_HOLDER_NAME = "print_holder_name";

    protected T context;

    protected AbstractEntityHolder(T context) {
        this.context = context;
    }

    public void setContext(T context){
        this.context = context;
    }

    @SuppressWarnings("WeakerAccess")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Will call after holder was registered.
     */
    public void start() {}

    /**
     * Will call before holder will be unregistered.
     */
    public void finish() {}

    /**
     * Exports events this holder processes especially. Events will be posted directly to this holder
     * (and possible other holders which define the same events) and won't spreaded to others.
     */
    public List<String> events() {
        return null;
    }

    public boolean onEvent(String eventName, Object eventObject) {
        switch(eventName) {
            case PRINT_HOLDER_NAME:
                System.out.println("EntityHolder name: " + getType());
                break;
        }
        return true;
    }



}
