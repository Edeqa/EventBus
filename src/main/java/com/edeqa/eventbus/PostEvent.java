/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017-18 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov <tujger@gmail.com>
 */

package com.edeqa.eventbus;

public class PostEvent<T> {
    private String eventName;
    private T eventObject;
    private int fulfillment;
    private int maxFulfillment;
    private boolean fulfilled;

    public PostEvent() {
        maxFulfillment = 1;
        fulfillment = 0;
    }

    public PostEvent(String eventName) {
        this();
        this.eventName = eventName;
    }

    public PostEvent(String eventName, T eventObject) {
        this(eventName);
        this.eventObject = eventObject;
    }

    public PostEvent(String eventName, T eventObject, int maxFulfillment) {
        this(eventName, eventObject);
        this.maxFulfillment = maxFulfillment;
    }

    public void increaseCounter() {
        fulfillment++;
        if(fulfillment >= getMaxFulfillment()) {
            setFulfilled(true);
        }
    }

    @Override
    public String toString() {
        return getEventName() + " [" + (getEventObject() == null ? "null" : getEventObject().getClass().getSimpleName()) + "]"
                + " (" + getFulfillment() + "/" + getMaxFulfillment() + ")";
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public T getEventObject() {
        return eventObject;
    }

    public void setEventObject(T eventObject) {
        this.eventObject = eventObject;
    }

    public int getMaxFulfillment() {
        return maxFulfillment;
    }

    public void setMaxFulfillment(int maxFulfillment) {
        this.maxFulfillment = maxFulfillment;
    }

    public int getFulfillment() {
        return fulfillment;
    }
}
