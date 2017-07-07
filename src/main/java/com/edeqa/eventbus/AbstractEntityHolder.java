package com.edeqa.eventbus;

/*
 * Created by Edward Mukhutdinov (tujger@gmail.com)
 *
 */
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

    public boolean onEvent(String eventName, Object eventObject) {
        switch(eventName) {
            case PRINT_HOLDER_NAME:
                System.out.println("EntityHolder name: " + getType());
                break;
        }
        return true;
    }

}
