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
public class EventBus<T extends AbstractEntityHolder> {

    public static final String DEFAULT_NAME = "default";

    public static Level LOGGING_LEVEL = Level.WARNING;

    private final static Logger LOGGER = Logger.getLogger(EventBus.class.getName());

    private static Map<String, Map<String, AbstractEntityHolder>> holders = new LinkedHashMap<>();

    private static Map<String, Runner> runners = new HashMap<>();

    private static Map<String, List<AbstractEntityHolder>> eventsMap = new LinkedHashMap<>();

    protected Map<String, AbstractEntityHolder> holdersMap;

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
        holders.put(eventBusName, new LinkedHashMap<String, AbstractEntityHolder>());
        holdersMap = new HashMap<>();

        LOGGER.info("EventBus registered: " + eventBusName);

        setRunner(RUNNER_DEFAULT);

        buses.put(eventBusName, this);
    }

    public void register(final AbstractEntityHolder holder) {

        if(holdersMap.containsKey(holder.getType())) {
            LOGGER.severe("EventBus: " + eventBusName + " registration failed, holder already defined.");
            return;
        }
        holder.setLoggingLevel(LOGGING_LEVEL);

        //noinspection unchecked
        List<String> events = holder.events();
        if(events != null && events.size() > 0) {
            for(String event: events) {
                List<AbstractEntityHolder> hs = new ArrayList<>();
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
    }

    public void update(final AbstractEntityHolder holder) {
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

    public void unregister(AbstractEntityHolder holder) {
        if(holder == null || holder.getType() == null || holder.getType().length() == 0) {
            LOGGER.severe("EventBus: " + eventBusName + " update failed, holder is not defined.");
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

        Iterator<Map.Entry<String, List<AbstractEntityHolder>>> iter = eventsMap.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, List<AbstractEntityHolder>> entry = iter.next();
            if(entry.getValue().contains(holder)) {
                entry.getValue().remove(holder);
            }
            if(entry.getValue().size() == 0) {
                iter.remove();
            }
        }
    }

    public Map<String, AbstractEntityHolder> getHolders() {
        return getHolders(eventBusName);
    }

    public static Map<String, AbstractEntityHolder> getHolders(String eventBusName) {
        return holders.get(eventBusName);
    }

    public List<AbstractEntityHolder> getHoldersList() {
        return getHoldersList(eventBusName);
    }

    public static List<AbstractEntityHolder> getHoldersList(String eventBusName) {
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
     * Will post runnable in the queue with events.
     */
    public static void postRunnable(String eventBusName, Runnable runnable) {
        LOGGER.fine("EventBusName: " + eventBusName + ", starting runnable: " + runnable);
        runners.get(eventBusName).post(runnable);
    }

    public static void postSync(final String eventBusName, final String eventName, final Object eventObject) {
        runners.get(eventBusName).post(new Runnable() {
            @Override
            public void run() {
                LOGGER.fine("EventBusName: " + eventBusName + ", starting postSync for eventName: " + eventName + ", eventObject: " + eventObject);
                for (Map.Entry<String, AbstractEntityHolder> entry : holders.get(eventBusName).entrySet()) {
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
     */
    public static void postAll(String eventName, Object eventObject) {
        for(Map.Entry<String,Map<String, AbstractEntityHolder>> x: holders.entrySet()) {
            post(x.getKey(), eventName, eventObject);
        }
    }

    public static void postAll(String eventName) {
        postAll(eventName, null);
    }

    public static void postAll(Runnable runnable) {
        for(Map.Entry<String,Map<String, AbstractEntityHolder>> x: holders.entrySet()) {
            postRunnable(x.getKey(), runnable);
        }
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public void clear() {
        for(Map.Entry<String, AbstractEntityHolder> holder: holders.get(eventBusName).entrySet()) {
            holder.getValue().finish();
            Iterator<Map.Entry<String, List<AbstractEntityHolder>>> iter = eventsMap.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, List<AbstractEntityHolder>> entry = iter.next();
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

    public static void setMainRunner(Runner runner) {
        EventBus.RUNNER_DEFAULT = runner;
        LOGGER.config("EventBus overrides main runner: " + runner);
        for(Map.Entry<String,Runner> entry: runners.entrySet()) {
            runners.put(entry.getKey(), runner);
        }
    }

    public AbstractEntityHolder getHolder(String type) {
        if(holdersMap.containsKey(type)) return holdersMap.get(type);
        return null;
    }

    public static void inspect(String eventName) {
        LOGGER.warning("EventBus sets event for deep inspection: " + eventName);
        inspect.add(eventName);
    }

}
