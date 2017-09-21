package com.edeqa.eventbus;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.edeqa.eventbus.AbstractEntityHolder.PRINT_HOLDER_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Created 9/20/17.
 */
public class AbstractEntityHolderTest {
    private Object context1;
    private Object context2;
    private SampleHolder holder;

    @Before
    public void setUp() throws Exception {

        context1 = new Object();
        context2 = new Object();
        holder = new SampleHolder(context1);
    }

    @Test
    public void setContext() throws Exception {
        holder.setContext(context2);
    }

    @Test
    public void getType() throws Exception {
        assertEquals("SampleHolder", holder.getType());
    }

    @Test
    public void start() throws Exception {
        holder.start();
    }

    @Test
    public void finish() throws Exception {
        holder.finish();
    }

    @Test
    public void events() throws Exception {
        assertEquals(PRINT_HOLDER_NAME, holder.events().get(0));
    }

    @Test
    public void onEvent() throws Exception {
        holder.onEvent(PRINT_HOLDER_NAME, null);
        holder.onEvent("test_event", "test object");
    }

    @Test
    public void setLoggingLevel() throws Exception {
        holder.setLoggingLevel(Level.ALL);
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
            super.onEvent(eventName, eventObject);
            switch(eventName) {
                case PRINT_HOLDER_NAME:
                    assertEquals(null, eventObject);
                    break;
                case "test_event":
                    assertEquals("test object", eventObject);
                    System.out.println("Event with argument: " + eventObject);
                    break;
                default:
                    assertEquals("", eventName);
            }
            return true; // if returns false then chain will be interrupted
        }
    }

    @Test
    public void toStringTest() throws Exception {

        assertEquals("EntityHolder{type=SampleHolder}", holder.toString());
    }


}