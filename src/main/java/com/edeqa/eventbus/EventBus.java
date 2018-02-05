/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov <tujger@gmail.com>
 */

package com.edeqa.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"WeakerAccess", "unused", "HardCodedStringLiteral"})
public class EventBus<T extends EntityHolder> {

    public static final String DEFAULT_NAME = "default";
    public static final Runner DEFAULT_RUNNER = new Runner() {
        @Override
        public void post(final Runnable runnable) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    };

    public static final Runner RUNNER_MULTI_THREAD = DEFAULT_RUNNER;
    public static final Runner RUNNER_SINGLE_THREAD = new EventBus.Runner() {
        @Override
        public void post(Runnable runnable) {
            runnable.run();
        }
    };

    private final static Logger LOGGER = Logger.getLogger(EventBus.class.getName());
    private static Level loggingLevel = Level.WARNING;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Runner runnerDefault = DEFAULT_RUNNER;

    private static Map<String, EventBus<? extends EntityHolder>> buses = new LinkedHashMap<>();
    private static List<String> inspect = new ArrayList<>();

    private Runner runner;
    private Map<String, T> holders;
    private Map<String, Map<String, T>> events;
    private String eventBusName;

    public EventBus() throws TooManyListenersException {
        this(DEFAULT_NAME);
    }

    public EventBus(String eventBusName) throws TooManyListenersException {
        LOGGER.setLevel(getLoggingLevel());

        if (buses.containsKey(eventBusName)) {
            throw new TooManyListenersException("EventBus: <" + eventBusName + "> already defined.");
        }
        this.eventBusName = eventBusName;
        holders = new LinkedHashMap<>();
        events = new HashMap<>();

        LOGGER.info("EventBus registered: <" + eventBusName + ">");
        setRunner(runnerDefault);
        buses.put(eventBusName, this);
    }

    /**
     * Will call {@link EntityHolder#finish()} on each holder before clear eventBus.
     */
    public static void clearAll() {
        for (EventBus<? extends EntityHolder> bus : fetchEventBusesList()) {
            bus.clear();
        }
//        holders.clear();
//        events.clear();
        LOGGER.info("EventBus: all buses have been cleared.");
    }

    /**
     * Collects and returns list of all defined event buses.
     *
     * @return list of event buses
     */
    public static List<EventBus<? extends EntityHolder>> fetchEventBusesList() {
        return new ArrayList<>(buses.values());
    }

    public static EventBus<? extends EntityHolder> getEventBus(String eventBusName) {
        return buses.get(eventBusName);
    }

    public static Map<String, ? extends EntityHolder> getHolders(String eventBusName) {
        return buses.get(eventBusName).getHolders();
    }

    public static List<? extends EntityHolder> getHoldersList(String eventBusName) {
        return new ArrayList<>(getHolders(eventBusName).values());
    }

    private static Level getLoggingLevel() {
        return loggingLevel;
    }

    public static void setLoggingLevel(Level loggingLevel) {
        EventBus.loggingLevel = loggingLevel;
    }

    public static EventBus<? extends EntityHolder> getOrCreate() {
        return getOrCreate(DEFAULT_NAME);
    }

    public static EventBus<? extends EntityHolder> getOrCreate(String eventBusName) {
        if (buses.containsKey(eventBusName)) {
            return buses.get(eventBusName);
        } else {
            try {
                return new EventBus<>(eventBusName);
            } catch (TooManyListenersException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Provides the possibility for deep inspection of event specified.
     *
     * @param eventName any not empty event name, i.e. "event1"
     */
    public static void inspect(String eventName) {
        if (eventName != null) {
            LOGGER.warning("EventBus sets event for deep inspection: " + eventName);
            inspect.add(eventName);
        } else {
            LOGGER.warning("EventBus clears inspection events");
            inspect.clear();
        }
    }

    /**
     * Events poster. Events will be posted to holders using {@link Runner}.
     *
     * @param eventBusName name of event bus that must process this event
     * @param eventName    any not empty event name, i.e. "event1"
     * @param eventObject  any object that will be sent together with event name
     */
    public static void post(String eventBusName, String eventName, Object eventObject) {
        buses.get(eventBusName).post(eventName, eventObject);
    }

    /**
     * Will post event/object to each holder in each event bus defined.
     *
     * @param eventName   any not empty event name, i.e. "event1"
     * @param eventObject any object that will be sent together with event name
     */
    public static void postAll(String eventName, Object eventObject) {
        for (EventBus<? extends EntityHolder> bus : fetchEventBusesList()) {
            post(bus.getEventBusName(), eventName, eventObject);
        }
    }

    /**
     * Will post runnable to each holder in each event bus defined.
     *
     * @param eventName any not empty event name, i.e. "event1"
     */
    public static void postAll(String eventName) {
        postAll(eventName, null);
    }

    /**
     * Will post runnable to each holder in each event bus using the same queue as events.
     *
     * @param runnable redefine {@link Runnable}
     */
    public static void postAll(Runnable runnable) {
        for (EventBus<? extends EntityHolder> bus : fetchEventBusesList()) {
            postRunnable(bus.getEventBusName(), runnable);
        }
    }

    /**
     * Will post runnable in the same queue with events.
     *
     * @param eventBusName name of target event bus
     * @param runnable     redefine {@link Runnable}
     */
    public static void postRunnable(String eventBusName, Runnable runnable) {
        buses.get(eventBusName).postRunnable(runnable);
    }

    /**
     * Redefines the default runner and overrides runners in all existing buses.
     *
     * @param runner {@link Runner}, default value is {@link EventBus#DEFAULT_RUNNER}.
     */
    public static void setMainRunner(Runner runner) {
        EventBus.runnerDefault = runner;
        LOGGER.config("EventBus overrides main runner: " + runner);
        for (EventBus<? extends EntityHolder> bus : fetchEventBusesList()) {
            bus.setRunner(runner);
        }
    }

    /**
     * Will call {@link EntityHolder#finish()} on each holder before clear eventBus.
     */
    public void clear() {
        Iterator<Map.Entry<String, T>> entries = holders.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, T> holderEntry = entries.next();
            final T holder = holderEntry.getValue();
            entries.remove();

            Iterator<Map.Entry<String, Map<String, T>>> iter = events.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Map<String, T>> entry = iter.next();
                if (entry.getValue().containsKey(holderEntry.getValue().getType())) {
                    entry.getValue().remove(holderEntry.getValue().getType());
                }
                if (entry.getValue().size() == 0) {
                    iter.remove();
                }
            }
            getRunner().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        holder.finish();
                    } catch (Exception e) {
                        LOGGER.severe("with eventBus <" + eventBusName + "> and holder " + holder);
                        e.printStackTrace();
                    }
                }
            });
        }
        LOGGER.info("EventBus: <" + eventBusName + "> has been cleared.");
    }

    /**
     * Collects and returns list of all holders registered in event bus.
     *
     * @return list of holders
     */
    public List<T> fetchHoldersList() {
        return new ArrayList<>(holders.values());
    }

    public String getEventBusName() {
        return eventBusName;
    }

    public T getHolder(String type) {
        if (holders.containsKey(type)) return holders.get(type);
        return null;
    }

    /**
     * Returns the map of holders in event bus.
     */
    public Map<String, T> getHolders() {
        return holders;
    }

    public List<? extends EntityHolder> getHoldersList() {
        return getHoldersList(eventBusName);
    }

    public Runner getRunner() {
        return runner;
    }

    /**
     * Changes the runner for event bus. Redefine the runner if you want to send events to android
     * main thread. See details https://github.com/edeqa/eventbus
     *
     * @param runner {@link Runner}, default value is {@link EventBus#DEFAULT_RUNNER}.
     */
    public void setRunner(Runner runner) {
        this.runner = runner;
        LOGGER.config("EventBus: <" + eventBusName + "> set runner: " + runner);
    }

    /**
     * Events poster. Events will be posted to holders using {@link Runner}.
     *
     * @param eventName any not empty event name, i.e. "event1"
     */
    public void post(String eventName) {
        post(eventName, null);
    }

    /**
     * Events poster. Events will be posted to holders using {@link Runner}.
     *
     * @param eventName   any not empty event name, i.e. "event1"
     * @param eventObject any object that will be sent together with event name
     */
    public void post(final String eventName, final Object eventObject) {
        if (inspect.size() > 0) {
            if (inspect.contains(eventName)) {
                LOGGER.severe("EventBus: <" + eventBusName + ">, inspection for eventName " + eventName + " caught:");
                Thread.dumpStack();
            }
        }
        getRunner().post(new Runnable() {
            @Override
            public void run() {
                LOGGER.fine("EventBus: <" + eventBusName + ">, starting postSync for eventName: " + eventName + ", eventObject: " + eventObject);
                for (Map.Entry<String, ? extends EntityHolder> entry : getHolders().entrySet()) {
                    try {
                        if (events.containsKey(eventName) && !events.get(eventName).containsKey(entry.getValue().getType())) {
                            LOGGER.fine("EventBus: <" + eventBusName + "> skips holder: " + entry.getValue() + ", eventName: " + eventName + " because of holder was not adjusted for this event.");
                            continue;
                        }
                        LOGGER.fine("EventBus: <" + eventBusName + "> holder: " + entry.getValue() + ", eventName: " + eventName + ", eventObject: " + eventObject);
                        if (!entry.getValue().onEvent(eventName, eventObject)) {
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.severe("EventBus: <" + eventBusName + ">, post failed for holder: " + entry.getValue() + ", eventName: " + eventName + ", eventObject: " + eventObject + ". Use EventBus.inspect(\"" + eventName + "\"); to resolve the issue.");
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Will post runnable in the same queue with events.
     *
     * @param runnable redefine {@link Runnable}
     */
    public void postRunnable(Runnable runnable) {
        LOGGER.fine("EventBus: <" + eventBusName + ">, starting runnable: " + runnable);
        getRunner().post(runnable);
    }

    /**
     * Registers the holder in event bus.
     *
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void register(final T holder) {
        if (holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: <" + eventBusName + "> registration failed, holder is not defined or invalid.");
            return;
        }
        if (holders.containsKey(holder.getType())) {
            LOGGER.severe("EventBus: <" + eventBusName + "> registration failed, holder <" + holder.getType() + "> already defined. " + holder.getClass());
            return;
        }
        holder.setLoggingLevel(getLoggingLevel());

        //noinspection unchecked
        List<String> events = holder.events();

        if (events != null) {
            for (String event : events) {
                Map<String, T> hs;
                if (this.events.containsKey(event)) {
                    hs = this.events.get(event);
                } else {
                    hs = new HashMap<>();
                    this.events.put(event, hs);
                }
                hs.put(holder.getType(), holder);
            }
            LOGGER.config("EventBus: <" + eventBusName + "> holder " + holder.getType() + " catches following events: " + events);
        }

        holders.put(holder.getType(), holder);
        getRunner().post(new Runnable() {
            @Override
            public void run() {
                try {
                    holder.start();
                } catch (Exception e) {
                    LOGGER.severe("EventBus: <" + eventBusName + "> and holder " + holder);
                    e.printStackTrace();
                }
            }
        });
        LOGGER.info("EventBus: <" + eventBusName + "> holder registered: " + holder.getType());
    }

    /**
     * Registers the holder in event bus or updates if it already exists.
     *
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void registerOrUpdate(T holder) {
        if (getHolder(holder.getType()) != null) {
            update(holder);
        } else {
            register(holder);
        }
    }

    /**
     * Updates the holder and keeps its order in the queue.
     *
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void update(final T holder) {
        if (holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: <" + eventBusName + "> update failed, holder " + holder + " is not defined or invalid.");
            return;
        }
        if (!holders.containsKey(holder.getType())) {
            LOGGER.severe("EventBus: <" + eventBusName + "> update failed, holder " + holder.getType() + " was not registered before.");
            return;
        }
        holders.put(holder.getType(), holder);
        LOGGER.info("EventBus: <" + eventBusName + "> holder updated: " + holder.getType());
    }

    /**
     * Unregisters the holder from event bus by its type.
     *
     * @param type type
     */
    public void unregister(String type) {
        if (holders.containsKey(type)) {
            T holder = holders.get(type);
            unregister(holder);
        } else {
            LOGGER.severe("EventBus: <" + eventBusName + "> unregister failed, holder not found: " + type);
        }
    }

    /**
     * Unregisters the holder from event bus.
     *
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void unregister(final T holder) {
        if (holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: <" + eventBusName + "> unregister failed, holder is not defined.");
            return;
        }
        try {
            getRunner().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        holder.finish();
                        LOGGER.info("EventBus: <" + eventBusName + "> holder finished: " + holder);
                    } catch (Exception e) {
                        LOGGER.severe("EventBus: <" + eventBusName + "> and holder " + holder);
                        e.printStackTrace();
                    }
                }
            });
            holders.remove(holder.getType());
            LOGGER.info("EventBus: <" + eventBusName + "> holder unregistered: " + holder);

            Iterator<Map.Entry<String, Map<String, T>>> iter = events.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Map<String, T>> entry = iter.next();
                if (entry.getValue().containsKey(holder.getType())) {
                    entry.getValue().remove(holder.getType());
                }
                if (entry.getValue().size() == 0) {
                    iter.remove();
                }
            }
        } catch (Exception e) {
            LOGGER.severe("EventBus: <" + eventBusName + "> unregister failed for holder: " + holder);
            e.printStackTrace();
        }
    }

    public interface Runner {
        void post(Runnable runnable);
    }
}
