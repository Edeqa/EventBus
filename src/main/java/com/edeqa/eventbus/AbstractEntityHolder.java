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
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
abstract public class AbstractEntityHolder implements EntityHolder {

    public static final String PRINT_HOLDER_NAME = "print_holder_name"; //NON-NLS
    protected static final Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private Level loggingLevel = Level.WARNING;

    protected AbstractEntityHolder() {
        LOGGER.setLevel(loggingLevel);
        LOGGER.info("AbstractEntityHolder:init"); //NON-NLS
    }

    @Override
    @SuppressWarnings("WeakerAccess")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Will call after holder registration.
     */
    @Override
    public void start() throws Exception {
    }

    /**
     * Will call before holder should be unregistered.
     */
    @Override
    public void finish() throws Exception {
    }

    /**
     * Exports events of this holder for process especially. Events will be posted directly to this holder
     * (and possible other holders which define the same events) and won't spreaded to others.
     *
     * @return list of event names can be performed only with this holder.
     */
    @Override
    public List<String> events() {
        return null;
    }

    @Override
    public boolean onEvent(String eventName, Object eventObject) throws Exception {
        LOGGER.info(getType() + ".onEvent performs with eventName: " + eventName + ", eventObject: " + eventObject); //NON-NLS
        switch (eventName) {
            case PRINT_HOLDER_NAME:
                System.out.println("EntityHolder name: " + getType()); //NON-NLS
                break;
        }
        return true;
    }

    @Override
    public boolean onEvent(PostEvent postEvent) throws Exception {
        LOGGER.info(getType() + ".onEvent performs with eventName: " + postEvent.getEventName() + ", eventObject: " + postEvent.getEventObject()); //NON-NLS
        switch (postEvent.getEventName()) {
            case PRINT_HOLDER_NAME:
                postEvent.increaseCounter();
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

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "type=" + getType() +
                '}';
    }
}
