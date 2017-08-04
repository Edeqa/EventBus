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
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
abstract public class AbstractEntityHolder<T> implements EntityHolder<T> {

    public static final String PRINT_HOLDER_NAME = "print_holder_name"; //NON-NLS
    protected static Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    protected T context;

    private Level loggingLevel = Level.WARNING;

    protected AbstractEntityHolder() {
        LOGGER.setLevel(loggingLevel);
        LOGGER.info("AbstractEntityHolder:init"); //NON-NLS
    }

    protected AbstractEntityHolder(T context) {
        this();
        this.context = context;
    }

    @Override
    public void setContext(T context){
        this.context = context;
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Will call after holder was registered.
     */
    @Override
    public void start() {}

    /**
     * Will call before holder will be unregistered.
     */
    @Override
    public void finish() {}

    /**
     * Exports events of this holder for process especially. Events will be posted directly to this holder
     * (and possible other holders which define the same events) and won't spreaded to others.
     * @return list of event names can be performed only with this holder.
     */
    @Override
    public List<String> events() {
        return null;
    }

    @Override
    public boolean onEvent(String eventName, Object eventObject) {
        LOGGER.info("onEvent performs with eventName: " + eventName + ", eventObject: " + eventObject); //NON-NLS
        switch(eventName) {
            case PRINT_HOLDER_NAME:
                System.out.println("EntityHolder name: " + getType()); //NON-NLS
                break;
        }
        return true;
    }

    @Override
    public void setLoggingLevel(Level level) {
        loggingLevel = level;
        LOGGER.setLevel(loggingLevel);
    }

}
