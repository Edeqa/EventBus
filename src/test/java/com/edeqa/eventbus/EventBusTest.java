package com.edeqa.eventbus;

import com.edeqa.eventbus.EventBus.Runner;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.edeqa.eventbus.AbstractEntityHolder.PRINT_HOLDER_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Created 9/20/17.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventBusTest {

    private final static String TEST_EVENT2 = "test_event2";
    private final static String TEST_EVENT_ERROR = "test_event_error";

    private final String context1 = "1";
    private final String context2 = "2";
    private final String context3 = "3";
    private final String context4 = "4";
    private EventBus<SampleHolder> eventBus1;
    private EventBus<SampleHolder> eventBus2;
    private SampleHolder holder1;
    private SampleHolder2 holder2;
    private SampleHolder3 holder3;
    private SampleHolder4 holder4;

    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    Runner runner = new Runner() {
        @Override
        public void post(final Runnable runnable) {
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }, 10, TimeUnit.MILLISECONDS);
        }
    };

    @Before
    public void setUp() throws Exception {
        System.out.println("=== INITIALIZING");

        EventBus.setMainRunner(runner);

        holder1 = new SampleHolder(context1);
        holder2 = new SampleHolder2(context2);
        holder3 = new SampleHolder3(context3);
        holder4 = new SampleHolder4(context4);

        if(EventBus.getEventBus(EventBus.DEFAULT_NAME) == null) {
            eventBus1 = new EventBus<>();
        }

        //noinspection unchecked
        eventBus1 = (EventBus<SampleHolder>) EventBus.getOrCreate();
        //noinspection unchecked
        eventBus2 = (EventBus<SampleHolder>) EventBus.getOrCreate("second");

        assertEquals(0, eventBus1.getHoldersList().size());
        assertEquals(0, eventBus2.getHoldersList().size());

        eventBus1.register(holder1);
        eventBus1.register(holder2);
        eventBus2.register(holder1);
        eventBus2.register(holder2);
        eventBus2.register(holder3);
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(2, eventBus1.getHoldersList().size());
        assertEquals(3, eventBus2.getHoldersList().size());
        System.out.println(">>> BEGIN TEST");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("<<< END TEST");
        if(eventBus1.getHoldersList().size() > 0) {
            EventBus.clearAll();
            synchronized (context2) {
                context2.wait();
            }
        }
        if(eventBus2.getHoldersList().size() > 0) {
            EventBus.clearAll();
            synchronized (context3) {
                context3.wait();
            }
        }
        synchronized (context1) {
            context1.notify();
        }
        synchronized (context2) {
            context2.notify();
        }
        synchronized (context3) {
            context3.notify();
        }
        System.out.println("=== FINALIZING");
    }

    @Test
    public void register() throws Exception {

        eventBus1.register(holder3);
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(3, eventBus1.getHoldersList().size());

        eventBus1.register(holder3);
        assertEquals(3, eventBus1.getHoldersList().size());

        eventBus1.register(null);
        assertEquals(3, eventBus1.getHoldersList().size());

        EventBus eventBus = null;
        try {
            eventBus = new EventBus("second");
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        assertEquals(null, eventBus);

        eventBus1.register(holder4);
        assertEquals(4, eventBus1.getHoldersList().size());
    }

    @Test
    public void registerOrUpdate() throws Exception {
        eventBus1.registerOrUpdate(holder1);
        eventBus1.registerOrUpdate(holder3);
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(3, eventBus1.getHoldersList().size());

    }

    @Test
    public void update() throws Exception {
        eventBus1.update(null);
        assertEquals(2, eventBus1.getHoldersList().size());

        holder1 = new SampleHolder(context2);
        eventBus1.update(holder1);
        assertEquals(2, eventBus1.getHoldersList().size());

        eventBus1.update(holder3);
        assertEquals(2, eventBus1.getHoldersList().size());

    }

    @Test
    public void unregister() throws Exception {
        eventBus1.unregister(holder2.getType());
        synchronized (context2) {
            context2.wait();
        }
        assertEquals(1, eventBus1.getHoldersList().size());

        eventBus2.unregister(holder1.getType());
        synchronized (context1) {
            context1.wait();
        }
        assertEquals(2, eventBus2.getHoldersList().size());
        eventBus2.unregister(holder1.getType());
        assertEquals(2, eventBus2.getHoldersList().size());

        assertEquals(1, eventBus1.getHoldersList().size());
        eventBus1.unregister(holder1.getType());
        assertEquals(0, eventBus1.getHoldersList().size());

        assertEquals(0, eventBus1.getHoldersList().size());
        eventBus1.register(holder4);
        assertEquals(1, eventBus1.getHoldersList().size());

        eventBus1.unregister(holder4);
        assertEquals(0, eventBus1.getHoldersList().size());

        assertEquals(2, eventBus2.getHoldersList().size());
        eventBus2.unregister((SampleHolder) null);
        assertEquals(2, eventBus2.getHoldersList().size());

    }

    @Test
    public void unregister1() throws Exception {
        eventBus1.unregister(holder1);
        synchronized (context1) {
            context1.wait();
        }
        assertEquals(1, eventBus1.getHoldersList().size());
        eventBus1.unregister(holder1);
        eventBus2.unregister(holder2);
        synchronized (context2) {
            context2.wait();
        }
        assertEquals(2, eventBus2.getHoldersList().size());

        eventBus2.unregister(holder2);
        assertEquals(2, eventBus2.getHoldersList().size());

    }

    @Test
    public void getHolders() throws Exception {
        assertEquals("SampleHolder", eventBus1.getHolders().get(holder1.getType()).getType());
        assertEquals("SampleHolder2", eventBus1.getHolders().get(holder2.getType()).getType());
        assertEquals("SampleHolder", eventBus2.getHolders().get(holder1.getType()).getType());
        assertEquals("SampleHolder2", eventBus2.getHolders().get(holder2.getType()).getType());
        assertEquals("SampleHolder3", eventBus2.getHolders().get(holder3.getType()).getType());
    }

    @Test
    public void getHolders1() throws Exception {
        assertEquals("SampleHolder", EventBus.getHolders(eventBus1.getEventBusName()).get(holder1.getType()).getType());
        assertEquals("SampleHolder2", EventBus.getHolders(eventBus1.getEventBusName()).get(holder2.getType()).getType());
        assertEquals("SampleHolder", EventBus.getHolders(eventBus2.getEventBusName()).get(holder1.getType()).getType());
        assertEquals("SampleHolder2", EventBus.getHolders(eventBus2.getEventBusName()).get(holder2.getType()).getType());
        assertEquals("SampleHolder3", EventBus.getHolders(eventBus2.getEventBusName()).get(holder3.getType()).getType());
    }

    @Test
    public void getHoldersList() throws Exception {
        assertEquals(2, eventBus1.getHoldersList().size());
        assertEquals(3, eventBus2.getHoldersList().size());
    }

    @Test
    public void getHoldersList1() throws Exception {
        assertEquals(2, EventBus.getHoldersList(eventBus1.getEventBusName()).size());
        assertEquals(3, EventBus.getHoldersList(eventBus2.getEventBusName()).size());
    }

    @Test
    public void post() throws Exception {
        eventBus1.post(PRINT_HOLDER_NAME);
        synchronized (context2) {
            context2.wait();
        }
        eventBus2.post(PRINT_HOLDER_NAME);
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void post1() throws Exception {
        eventBus1.post("test_event", "test object");
        synchronized (context2) {
            context2.wait();
        }
        eventBus2.post("test_event", "test object");
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void post2() throws Exception {
        EventBus.post(eventBus1.getEventBusName(),"test_event", "test object");
        synchronized (context2) {
            context2.wait();
        }
        EventBus.post(eventBus2.getEventBusName(),"test_event", "test object");
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void postRunnable() throws Exception {
        eventBus1.postRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println("RUNNABLE");
                synchronized (context3) {
                    context3.notify();
                }
            }
        });
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void postRunnable1() throws Exception {
        EventBus.postRunnable(eventBus2.getEventBusName(), new Runnable() {
            @Override
            public void run() {
                System.out.println("RUNNABLE");
                synchronized (context3) {
                    context3.notify();
                }
            }
        });
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void postAll() throws Exception {
        EventBus.postAll(PRINT_HOLDER_NAME);
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void postAll1() throws Exception {
        EventBus.postAll("test_event", "test object");
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void postAll2() throws Exception {
        EventBus.postAll(new Runnable() {
            @Override
            public void run() {
                System.out.println("RUNNABLE");
                synchronized (context3) {
                    context3.notify();
                }
            }
        });
        synchronized (context3) {
            context3.wait();
        }
    }

    @Test
    public void clear() throws Exception {
        assertEquals(2, eventBus1.getHoldersList().size());
        eventBus1.clear();
        synchronized (context2) {
            context2.wait();
        }
        assertEquals(0, eventBus1.getHoldersList().size());
        assertEquals(3, eventBus2.getHoldersList().size());
    }

    @Test
    public void clearAll() throws Exception {
        EventBus.clearAll();
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(0, eventBus1.getHoldersList().size());
        assertEquals(0, eventBus2.getHoldersList().size());
    }

    @Test
    public void setRunner() throws Exception {
        eventBus1.setRunner(EventBus.DEFAULT_RUNNER);
        eventBus1.post(PRINT_HOLDER_NAME);
        synchronized (context2) {
            context2.wait();
        }
    }

    @Test
    public void setMainRunner() throws Exception {
        EventBus.setMainRunner(EventBus.DEFAULT_RUNNER);
        eventBus1.post(PRINT_HOLDER_NAME);
        synchronized (context2) {
            context2.wait();
        }
    }

    @Test
    public void getHolder() throws Exception {
        assertEquals("SampleHolder", eventBus1.getHolder(holder1.getType()).getType());
        assertEquals("SampleHolder2", eventBus1.getHolder(holder2.getType()).getType());
        assertEquals("SampleHolder", eventBus2.getHolder(holder1.getType()).getType());
        assertEquals("SampleHolder2", eventBus2.getHolder(holder2.getType()).getType());
        assertEquals("SampleHolder3", eventBus2.getHolder(holder3.getType()).getType());
    }

    @Test
    public void inspect() throws Exception {
        EventBus.inspect(PRINT_HOLDER_NAME);
        EventBus.inspect(TEST_EVENT2);
        eventBus1.post(PRINT_HOLDER_NAME);
        synchronized (context2) {
            context2.wait();
        }
        EventBus.inspect(null);
    }

    public class SampleHolder extends AbstractEntityHolder {

        protected Object context;

        SampleHolder(Object context) {
            super();
            this.context = context;
        }

        @Override
        public List<String> events() {
            super.events();
            return new ArrayList<>();
        }

        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            System.out.println("EVENT " + eventName +":"+ eventObject+ ":"+context.getClass().getSimpleName());
            try {
                super.onEvent(eventName, eventObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch(eventName) {
                case PRINT_HOLDER_NAME:
                    assertEquals(null, eventObject);
                    break;
                case "test_event":
                    assertEquals("test object", eventObject);
                    break;
                default:
                    assertEquals("", eventName);
            }
            synchronized (context) {
                context.notify();
            }
            System.out.println("POSTED " + eventName +":"+ eventObject+ ":"+context.getClass().getSimpleName());
            return true; // if returns false then chain will be interrupted
        }

        @Override
        public void start() {
            try {
                super.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("START "+this.getClass().getSimpleName() + ":"+context.getClass().getSimpleName());
            synchronized (context) {
                context.notify();
            }
        }

        @Override
        public void finish() {
            try {
                super.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("FINISH "+this.getClass().getSimpleName()+ ":"+context.getClass().getSimpleName());
            synchronized (context) {
                context.notify();
            }
        }
    }
    public class SampleHolder2 extends SampleHolder {
        SampleHolder2(Object context) {
            super(context);
        }
        @Override
        public List<String> events() {
            super.events();
            List<String> events = new ArrayList<>();
            events.add(TEST_EVENT2);
            return events;
        }
        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            switch(eventName) {
                case TEST_EVENT2:
                    assertEquals(null, eventObject);
                    synchronized (context) {
                        context.notify();
                    }
                    return false;
            }
            super.onEvent(eventName, eventObject);
            return true;
        }
    }
    public class SampleHolder3 extends SampleHolder {
        SampleHolder3(Object context) {
            super(context);
        }
    }

    public class SampleHolder4 extends SampleHolder {
        String text;
        SampleHolder4(Object context) {
            super(context);
        }

        @Override
        public void start() {
            super.start();
            System.out.println(text.length());
        }
        @Override
        public void finish() {
            super.finish();
            System.out.println(text.length());
        }
        @Override
        public List<String> events() {
            List<String> events = new ArrayList<>();
            events.add(TEST_EVENT2);
            events.add(TEST_EVENT_ERROR);
            return events;
        }
        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            switch(eventName) {
                case TEST_EVENT_ERROR:
                    System.out.println(text.length());
                    return true;
            }
            super.onEvent(eventName, eventObject);
            return true;
        }
    }

    @Test
    public void getEventBuses() throws Exception {
        assertEquals(2, EventBus.fetchEventBusesList().size());
    }

    @Test
    public void getEventBus() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME, EventBus.getEventBus(eventBus1.getEventBusName()).getEventBusName());
    }

    @Test
    public void getOrCreateEventBus() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME, EventBus.getOrCreate().getEventBusName());
    }

    @Test
    public void getOrCreateEventBus1() throws Exception {
        assertEquals("second", EventBus.getOrCreate("second").getEventBusName());
        assertEquals("third", EventBus.getOrCreate("third").getEventBusName());
        assertEquals(3, EventBus.fetchEventBusesList().size());
    }

    @Test
    public void getRunner() throws Exception {
        assertEquals(runner, eventBus1.getRunner());
    }

    @Test
    public void getEventBusName() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME,eventBus1.getEventBusName());
    }

    @Test
    public void fetchEventBusesList() throws Exception {
        assertEquals(2,EventBus.fetchEventBusesList().size());
    }

    @Test
    public void fetchHoldersList() throws Exception {
        assertEquals(2,eventBus1.fetchHoldersList().size());
        assertEquals(3,eventBus2.fetchHoldersList().size());

    }

    @Test
    public void errors() throws Exception {
        eventBus2.post(PRINT_HOLDER_NAME);
        synchronized (context3) {
            context3.wait();
        }
        eventBus1.post(TEST_EVENT2);
        synchronized (context2) {
            context2.wait();
        }

        eventBus2.register(holder4);
        assertEquals(4, eventBus2.getHoldersList().size());

        assertEquals(2, eventBus1.getHoldersList().size());
        eventBus1.register(holder3);
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(3, eventBus1.getHoldersList().size());

        eventBus2.post(TEST_EVENT_ERROR);

        assertEquals(4, eventBus2.getHoldersList().size());
        eventBus2.unregister(holder3);
        synchronized (context3) {
            context3.wait();
        }
        assertEquals(3, eventBus2.getHoldersList().size());

    }

    @Test
    public void setLoggingLevel() throws Exception {
        EventBus.setLoggingLevel(Level.OFF);
    }

}