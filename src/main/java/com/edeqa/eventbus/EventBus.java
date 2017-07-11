package com.edeqa.eventbus;

/*
 * Created by Edward Mukhutdinov (tujger@gmail.com)
 *
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"WeakerAccess", "unused"})
public class EventBus {

    public static final String DEFAULT_NAME = "default";

    private static Map<String,ArrayList<AbstractEntityHolder>> holders = new HashMap<>();

    private static Map<String,Runner> runners = new HashMap<>();

    private String eventBusName;

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventBus() {
        this(DEFAULT_NAME);
    }

    public EventBus(String eventBusName) {
        this.eventBusName = eventBusName;
        holders.put(eventBusName, new ArrayList<AbstractEntityHolder>());

        setRunner(mainRunner);
    }

    public void register(AbstractEntityHolder holder) {
        holders.get(eventBusName).add(holder);
        try {
            holder.start();
        } catch (Exception e) {
            System.err.println("with eventBusName '" + eventBusName + "' and holder " + holder);
            e.printStackTrace();
        }
    }

    public void unregister(AbstractEntityHolder holder) {
        try {
            holder.finish();
        } catch (Exception e) {
            System.err.println("with eventBusName '" + eventBusName + "' and holder " + holder);
            e.printStackTrace();
        }
        holders.get(eventBusName).remove(holder);
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
        for(Map.Entry<String,ArrayList<AbstractEntityHolder>> x: holders.entrySet()) {
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
        }
        holders.remove(eventBusName);
    }

    /**
     * Will call holder.finish() on each holder before clear eventBus.
     */
    public static void clearAll() {
        for(Map.Entry<String,ArrayList<AbstractEntityHolder>> bus: holders.entrySet()) {
            for(AbstractEntityHolder holder: bus.getValue()) {
                holder.finish();
            }
        }
        holders = new HashMap<>();
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
