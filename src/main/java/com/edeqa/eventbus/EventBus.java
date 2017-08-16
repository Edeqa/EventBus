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

    public static Level LOGGING_LEVEL = Level.WARNING;

    private final static Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private static Map<String, Map<String, EntityHolder>> holders = new LinkedHashMap<>();

    private static Map<String, Runner> runners = new HashMap<>();

    private static Map<String, List<EntityHolder>> eventsMap = new LinkedHashMap<>();

    private Map<String, T> holdersMap;

    private String eventBusName;

    private static Map<String, EventBus> buses = new HashMap<>();

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    private static List<String> inspect = new ArrayList<>();

    public EventBus() throws TooManyListenersException {
        this(DEFAULT_NAME);
    }

    public EventBus(String eventBusName) throws TooManyListenersException {
        LOGGER.setLevel(LOGGING_LEVEL);

        if(holders.containsKey(eventBusName)) {
            throw new TooManyListenersException("EventBus " + eventBusName + " already defined.");
        }

        this.eventBusName = eventBusName;
        holders.put(eventBusName, new LinkedHashMap<String, EntityHolder>());
        holdersMap = new HashMap<>();

        LOGGER.info("EventBus registered: " + eventBusName);

        setRunner(RUNNER_DEFAULT);

        buses.put(eventBusName, this);
    }

    public void register(final T holder) {

        if(holdersMap.containsKey(holder.getType())) {
            LOGGER.severe("EventBus: " + eventBusName + " registration failed, holder already defined.");
            return;
        }
        holder.setLoggingLevel(LOGGING_LEVEL);


        //noinspection unchecked
        List<String> events = holder.events();
        if(events != null && events.size() > 0) {
            for(String event: events) {
                List<EntityHolder> hs = new ArrayList<>();
                if(eventsMap.containsKey(event)) {
                    hs = eventsMap.get(event);
                }
                if(!hs.contains(holder)) hs.add(holder);
                eventsMap.put(event, hs);
            }
            LOGGER.config("EventBus: " + holder.getType() + " catches following events: " + events);
        }

        holdersMap.put(holder.getType(), holder);
        holders.get(eventBusName).put(holder.getType(), holder);
        runners.get(eventBusName).post(new Runnable() {
            @Override
            public void run() {
                try {
                    holder.start();
                } catch (Exception e) {
                    LOGGER.severe("with eventBusName '" + eventBusName + "' and holder " + holder);
                    e.printStackTrace();
                }
            }
        });
        LOGGER.info("EventBus: " + eventBusName + " holder registered: " + holder.getType());
    }

    /**
     * Registers the holder in event bus or updates it if it already exists.
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void registerOrUpdate(T holder) {
        if(getHolder(holder.getType()) != null) {
            update(holder);
        } else {
            register(holder);
        }
    }

    /**
     * Updates the holder and keeps its order in the queue.
     * @param holder must implement {@link EntityHolder}, may be an instance of {@link AbstractEntityHolder}
     */
    public void update(final T holder) {
        if(holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: " + eventBusName + " update failed, holder is not defined.");
            return;
        }
        if(!holdersMap.containsKey(holder.getType())) {
            LOGGER.severe("EventBus: " + eventBusName + " update failed, holder " + holder.getType() + " was not registered before.");
            return;
        }
        holdersMap.put(holder.getType(), holder);
        holders.get(eventBusName).put(holder.getType(), holder);
        LOGGER.info("EventBus: " + eventBusName + " holder updated: " + holder.getType());
    }

    public void unregister(String type) {
        if(holdersMap.containsKey(type)) {
            unregister(holdersMap.get(type));
        } else {
            LOGGER.severe("EventBus: " + eventBusName + " unregister failed, holder not found: " + type);
        }
    }

    public void unregister(T holder) {
        if(holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: " + eventBusName + " unregister failed, holder is not defined.");
            return;
        }
        try {
            holder.finish();
            LOGGER.info("EventBusName: " + eventBusName + " holder finished: " + holder);
        } catch (Exception e) {
            LOGGER.severe("EventBusName: " + eventBusName + " unregister failed for holder: " + holder);
            e.printStackTrace();
        }
        holders.get(eventBusName).remove(holder.getType());
        holdersMap.remove(holder.getType());
        LOGGER.info("EventBusName: " + eventBusName + " holder unregistered: " + holder);

        Iterator<Map.Entry<String, List<EntityHolder>>> iter = eventsMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, List<EntityHolder>> entry = iter.next();
            if(entry.getValue().contains(holder)) {
                entry.getValue().remove(holder);
            }
            if(entry.getValue().size() == 0) {
                iter.remove();
            }
        }
    }

    public Map<String, T> getHolders() {
        //noinspection unchecked
        return (Map<String, T>) getHolders(eventBusName);
    }

    public static Map<String, ? extends EntityHolder> getHolders(String eventBusName) {
        return holders.get(eventBusName);
    }

    public List<EntityHolder> getHoldersList() {
        return getHoldersList(eventBusName);
    }

    public static List<EntityHolder> getHoldersList(String eventBusName) {
        return new ArrayList<>(holders.get(eventBusName).values());
    }

    public void post(String eventName) {
        post(eventName, null);
    }

    public void post(Runnable runnable) {
        post(eventBusName, runnable);
    }

    public void post(String eventName, Object eventObject) {
        post(eventBusName, eventName, eventObject);
    }

    public static void post(String eventBusName, String eventName, Object eventObject) {
        if(inspect.size() > 0) {
            if(inspect.contains(eventName)) {
                LOGGER.severe("EventBusName: " + eventBusName + ", inspection for eventName " + eventName + " caught:");
                Thread.dumpStack();
            }
        }
        postSync(eventBusName, eventName, eventObject);
    }

    /**
     * Will post runnable in the same queue with events.
     * @param runnable redefine {@link Runnable}
     */
    public void postRunnable(Runnable runnable) {
        postRunnable(eventBusName, runnable);
    }

    /**
     * Will post runnable in the queue same with events.
     * @param eventBusName name of target event bus
     * @param runnable redefine {@link Runnable}
     */
    public static void postRunnable(String eventBusName, Runnable runnable) {
        LOGGER.fine("EventBusName: " + eventBusName + ", starting runnable: " + runnable);
        runners.get(eventBusName).post(runnable);
    }

    /**
     * The main events poster.
     * @param eventBusName a target event bus
     * @param eventName any not empty event name, i.e. "event1"
     * @param eventObject any object that will be sent together with event name
     */
    public static void postSync(final String eventBusName, final String eventName, final Object eventObject) {
        runners.get(eventBusName).post(new Runnable() {
            @Override
            public void run() {
                LOGGER.fine("EventBusName: " + eventBusName + ", starting postSync for eventName: " + eventName + ", eventObject: " + eventObject);
                for (Map.Entry<String, ? extends EntityHolder> entry : holders.get(eventBusName).entrySet()) {
                    try {
                        if(eventsMap.containsKey(eventName) && !eventsMap.get(eventName).contains(entry.getValue())) {
                            LOGGER.fine("EventBusName: " + eventBusName + " skips holder: " + entry.getValue() + ", eventName: " + eventName + " because of holder was not adjusted for this event.");
                            continue;
                        }
                        LOGGER.fine("EventBusName: " + eventBusName + " holder: " + entry.getValue() + ", eventName: " + eventName + ", eventObject: " + eventObject);
                        if (!entry.getValue().onEvent(eventName, eventObject)) {
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.severe("EventBusName: " + eventBusName + ", postSync failed for holder: " + entry.getValue() + ", eventName: " + eventName + ", eventObject: " + eventObject + ". Use EventBus.inspect(\""+eventName+"\"); to resolve the issue.");
                        e.printStackTrace();
                    }
                }
            }});
    }

    /**
     * Will post event/object to each holder in eventBus entirely.
     * @param eventName any not empty event name, i.e. "event1"
     * @param eventObject any object that will be sent together with event name
     */
    public static void postAll(String eventName, Object eventObject) {
        for(Map.Entry<String, Map<String, EntityHolder>> x: holders.entrySet()) {
            post(x.getKey(), eventName, eventObject);
        }
    }

    public static void postAll(String eventName) {
        postAll(eventName, null);
    }

    /**
     * Will post runnable to each holder in eventBus entirely using the same queue as events.
     * @param runnable redefine {@link Runnable}
     */
    public static void postAll(Runnable runnable) {
        for(Map.Entry<String, Map<String, EntityHolder>> x: holders.entrySet()) {
            postRunnable(x.getKey(), runnable);
        }
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public void clear() {
        for(Map.Entry<String, ? extends EntityHolder> holder: holders.get(eventBusName).entrySet()) {
            holder.getValue().finish();
            Iterator<Map.Entry<String, List<EntityHolder>>> iter = eventsMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, List<EntityHolder>> entry = iter.next();
                if(entry.getValue().contains(holder.getValue())) {
                    entry.getValue().remove(holder.getValue());
                }
                if(entry.getValue().size() == 0) {
                    iter.remove();
                }
            }
        }
        holdersMap.clear();
        LOGGER.info("EventBus: " + eventBusName + " has been cleared.");
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public static void clearAll() {
        for(Map.Entry<String,EventBus> bus: buses.entrySet()) {
            bus.getValue().clear();
        }
        holders.clear();
        eventsMap.clear();
        LOGGER.info("EventBus: all buses have been cleared.");
    }

    /**
     * Changes the runner for event bus. Redefine the runner if you want to send events to android
     * main thread. See details https://github.com/edeqa/eventbus
     * @param runner {@link Runner}, default value is RUNNER_DEFAULT
     */
    public void setRunner(Runner runner) {
        runners.put(eventBusName, runner);
        LOGGER.config("EventBus: " + eventBusName + " set runner: " + runner);
    }

    public interface Runner {
        void post(Runnable runnable);
    }

    public static Runner RUNNER_DEFAULT = new Runner() {
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

    /**
     * Redefines the default runner and overrides runners in all existing buses.
     * @param runner {@link Runner}, default value is RUNNER_DEFAULT
     */
    public static void setMainRunner(Runner runner) {
        EventBus.RUNNER_DEFAULT = runner;
        LOGGER.config("EventBus overrides main runner: " + runner);
        for(Map.Entry<String,Runner> entry: runners.entrySet()) {
            runners.put(entry.getKey(), runner);
        }
    }

    public EntityHolder getHolder(String type) {
        if(holdersMap.containsKey(type)) return holdersMap.get(type);
        return null;
    }

    /**
     * Provides the possibility for deep inspection of event specified.
     * @param eventName any not empty event name, i.e. "event1"
     */
    public static void inspect(String eventName) {
        LOGGER.warning("EventBus sets event for deep inspection: " + eventName);
        inspect.add(eventName);
    }

}
