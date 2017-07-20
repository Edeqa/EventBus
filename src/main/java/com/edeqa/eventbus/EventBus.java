/*
 * EventBus - a simple event bus
 * https://github.com/Edeqa/EventBus
 *
 * Copyright (C) 2017 Edeqa <http://www.edeqa.com>
 * Created by Edward Mukhutdinov (tujger@gmail.com)
 */

package com.edeqa.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"WeakerAccess", "unused"})
public class EventBus {

    public static final String DEFAULT_NAME = "default";

    private static Map<String, List<AbstractEntityHolder>> holders = new HashMap<>();

    private static Map<String,Runner> runners = new HashMap<>();

    private String eventBusName;

    private static Map<String, List<AbstractEntityHolder>> eventsMap = new HashMap<>();

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventBus() {
        this(DEFAULT_NAME);
    }

    public EventBus(String eventBusName) {
        this.eventBusName = eventBusName;
        holders.put(eventBusName, new ArrayList<AbstractEntityHolder>());

        setRunner(mainRunner);
    }

    public void register(final AbstractEntityHolder holder) {

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
            System.out.println("EventBus: " + holder.getType() + " catches following events: " + events);
        }

        holders.get(eventBusName).add(holder);
        runners.get(eventBusName).post(new Runnable() {
            @Override
            public void run() {
                try {
                    holder.start();
                } catch (Exception e) {
                    System.err.println("with eventBusName '" + eventBusName + "' and holder " + holder);
                    e.printStackTrace();
                }
            }
        });
    }

    public void unregister(AbstractEntityHolder holder) {
        try {
            holder.finish();
        } catch (Exception e) {
            System.err.println("with eventBusName '" + eventBusName + "' and holder " + holder);
            e.printStackTrace();
        }
        holders.get(eventBusName).remove(holder);

        for(Map.Entry<String,List<AbstractEntityHolder>> entry:eventsMap.entrySet()) {
            if(entry.getValue().contains(holder)) {
                entry.getValue().remove(holder);
            }
            if(entry.getValue().size() == 0) {
                eventsMap.remove(entry);
            }
        }
    }

    public void post(String eventName) {
        post(eventName, null);
    }

    public void post(String eventName, Object eventObject) {
        post(eventBusName, eventName, eventObject);
    }

    public static void post(final String eventBusName, final String eventName, final Object eventObject) {
        //noinspection Convert2Lambda
        executor.submit(new Runnable() {
            @Override
            public void run() {
                postSync(eventBusName, eventName, eventObject);
            }
        });
    }

    public static void postSync(final String eventBusName, final String eventName, final Object eventObject) {
        runners.get(eventBusName).post(new Runnable() {
            @Override
            public void run() {
                for (AbstractEntityHolder x : holders.get(eventBusName)) {
                    try {
                        if(eventsMap.containsKey(eventName) && !eventsMap.get(eventName).contains(x)) continue;
                        if (!x.onEvent(eventName, eventObject)) {
                            break;
                        }
                    } catch (Exception e) {
                        System.err.println("with eventBusName '" + eventBusName + "', holder '" + x.getType() + "' and eventName '" + eventName + "'");
                        e.printStackTrace();
                    }
                }
            }});
    }

    /**
     * Will post event/object to each holder in eventBus entirely.
     */
    public static void postAll(String eventName, Object eventObject) {
        for(Map.Entry<String,List<AbstractEntityHolder>> x: holders.entrySet()) {
            post(x.getKey(), eventName, eventObject);
        }
    }

    public static void postAll(String eventName) {
        postAll(eventName, null);
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public void clear() {
        for(AbstractEntityHolder holder: holders.get(eventBusName)) {
            holder.finish();
            for(Map.Entry<String,List<AbstractEntityHolder>> entry:eventsMap.entrySet()) {
                if(entry.getValue().contains(holder)) {
                    entry.getValue().remove(holder);
                }
                if(entry.getValue().size() == 0) {
                    eventsMap.remove(entry);
                }
            }
        }
        holders.remove(eventBusName);
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public static void clearAll() {
        for(Map.Entry<String,List<AbstractEntityHolder>> bus: holders.entrySet()) {
            for(AbstractEntityHolder holder: bus.getValue()) {
                holder.finish();
            }
        }
        holders.clear();
        eventsMap.clear();
    }

    public void setRunner(Runner runner) {
        runners.put(eventBusName, runner);
    }

    public interface Runner {
        void post(Runnable runnable);
    }

    private static Runner mainRunner = new Runner() {
        @Override
        public void post(Runnable runnable) {
            runnable.run();
        }
    };

    public static void setMainRunner(Runner runner) {
        EventBus.mainRunner = runner;
        for(Map.Entry<String,Runner> entry: runners.entrySet()) {
            runners.put(entry.getKey(), runner);
        }
    }


}
