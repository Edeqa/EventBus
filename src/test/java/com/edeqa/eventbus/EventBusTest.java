package com.edeqa.eventbus;

import com.edeqa.eventbus.EventBus.Runner;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.edeqa.eventbus.AbstractEntityHolder.PRINT_HOLDER_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Created 9/20/17.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventBusTest {

    private String context1;
    private StringBuffer context2;
    private StringBuilder context3;
    private EventBus eventBus1;
    private EventBus eventBus2;
    private SampleHolder holder1;
    private SampleHolder2 holder2;
    private SampleHolder3 holder3;

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

        context1 = new String();
        context2 = new StringBuffer();
        context3 = new StringBuilder();

        holder1 = new SampleHolder(context1);
        holder2 = new SampleHolder2(context2);
        holder3 = new SampleHolder3(context3);

        eventBus1 = EventBus.getOrCreateEventBus();
        eventBus2 = EventBus.getOrCreateEventBus("second");

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
            eventBus1.clearAll();
            synchronized (context2) {
                context2.wait();
            }
        }
        if(eventBus2.getHoldersList().size() > 0) {
            eventBus2.clearAll();
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
        holder1.setContext(context2);
        eventBus1.update(holder1);

        assertEquals(true, false);
    }

    @Test
    public void unregister() throws Exception {
        eventBus1.unregister(holder2.getType());
        synchronized (context2) {
            context2.wait();
        }
        assertEquals(1, eventBus1.getHoldersList().size());

        eventBus2.unregister(holder2.getType());
        synchronized (context2) {
            context2.wait();
        }
        assertEquals(2, eventBus2.getHoldersList().size());

        assertEquals(1, eventBus1.getHoldersList().size());
        eventBus1.getHolders().put(holder1.getType(), null);
        eventBus1.unregister(holder1.getType());
        assertEquals(0, eventBus1.getHoldersList().size());

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
    }

    @Test
    public void getHolders() throws Exception {
        assertEquals("SampleHolder", ((SampleHolder) eventBus1.getHolders().get(holder1.getType())).getType());
        assertEquals("SampleHolder2", ((SampleHolder) eventBus1.getHolders().get(holder2.getType())).getType());
        assertEquals("SampleHolder", ((SampleHolder) eventBus2.getHolders().get(holder1.getType())).getType());
        assertEquals("SampleHolder2", ((SampleHolder) eventBus2.getHolders().get(holder2.getType())).getType());
        assertEquals("SampleHolder3", ((SampleHolder) eventBus2.getHolders().get(holder3.getType())).getType());
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
        assertEquals(true, false);
        /*eventBus1.postRunnable(new Runnable() {
            @Override
            public void run() {
                assertEquals(true,false);
                synchronized (context3) {
                    context3.notify();
                }
            }
        });
        synchronized (context3) {
            context3.wait();
        }*/
    }

    @Test
    public void postRunnable1() throws Exception {
        assertEquals(true, false);
/*eventBus1.postRunnable(new Runnable() {
            @Override
            public void run() {
                assertEquals(true,false);
                synchronized (context3) {
                    context3.notify();
                }
            }
        });
        synchronized (context3) {
            context3.wait();
        }*/
    }

    @Test
    public void postSync() throws Exception {
        EventBus.postSync(eventBus1.getEventBusName(),"test_event", "test object");
        synchronized (context2) {
            context2.wait();
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
        assertEquals(true, false);
        /*EventBus.postAll(runnable);
        synchronized (context3) {
            context3.wait();
        }*/
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
        eventBus1.setRunner(runner);
    }

    @Test
    public void setMainRunner() throws Exception {
        EventBus.setMainRunner(runner);
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
        eventBus1.post(PRINT_HOLDER_NAME);
        synchronized (context2) {
            context2.wait();
        }
        eventBus2.post(PRINT_HOLDER_NAME);
        synchronized (context3) {
            context3.wait();
        }
        EventBus.inspect(null);
    }

    public class SampleHolder extends AbstractEntityHolder<Object> {

        public SampleHolder(Object context) {
            super(context);
        }

        @Override
        public List<String> events() {
            super.events();
            List<String> events = new ArrayList<>();
            events.add(PRINT_HOLDER_NAME);
            return events;
        }

        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            System.out.println("EVENT " + eventName +":"+ eventObject+ ":"+context.getClass().getSimpleName());
            super.onEvent(eventName, eventObject);
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
            super.start();
            System.out.println("START "+this.getClass().getSimpleName() + ":"+context.getClass().getSimpleName());
            synchronized (context) {
                context.notify();
            }
        }

        @Override
        public void finish() {
            super.finish();
            System.out.println("FINISH "+this.getClass().getSimpleName()+ ":"+context.getClass().getSimpleName());
            synchronized (context) {
                context.notify();
            }
        }
    }
    public class SampleHolder2 extends SampleHolder {
        public SampleHolder2(Object context) {
            super(context);
        }
    }
    public class SampleHolder3 extends SampleHolder {
        public SampleHolder3(Object context) {
            super(context);
        }
    }

    @Test
    public void getEventBuses() throws Exception {
        assertEquals(2, EventBus.getEventBuses().size());
    }

    @Test
    public void getEventBus() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME, EventBus.getEventBus(eventBus1.getEventBusName()).getEventBusName());
    }

    @Test
    public void getOrCreateEventBus() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME, EventBus.getOrCreateEventBus().getEventBusName());
    }

    @Test
    public void getOrCreateEventBus1() throws Exception {
        assertEquals("second", EventBus.getOrCreateEventBus("second").getEventBusName());
        assertEquals("third", EventBus.getOrCreateEventBus("third").getEventBusName());
        assertEquals(3, EventBus.getEventBuses().size());
    }

    @Test
    public void getRunner() throws Exception {
        assertEquals(runner, eventBus1.getRunner());
    }

    @Test
    public void getEventBusName() throws Exception {
        assertEquals(EventBus.DEFAULT_NAME,eventBus1.getEventBusName());
    }
}