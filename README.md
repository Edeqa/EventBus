# EventBus

[![](https://jitpack.io/v/edeqa/eventbus.svg)](https://jitpack.io/#edeqa/eventbus)

Simple event bus.

## How to add

Add to the project's build.gradle:

    allprojects {
        repositories {
            maven { url "https://jitpack.io" }
        }
    }


And then add to the app's build.gradle:


    dependencies {
        compile 'com.github.edeqa:eventbus:0.3'
    }

## How to use

First, create event bus:

    eventBus1 = new EventBus("first");
    eventBus2 = new EventBus("second");

or just:

    eventBus = new EventBus();

Make the class inherited from AbstractEntityHolder and implement onEvent for handle events and make some logic.

    public class SampleHolder extends AbstractEntityHolder<Context> {

        public SampleHolder(Context context) {
            super(context);
        }

        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            System.out.println("Event: "+eventName+", object: "+eventObject);
            return true;
        }

        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            switch(eventName) {
                case "event1":
                    System.out.println("EntityHolder name: " + getType());
                    break;
                case "event2":
                    System.out.println("Event with argument: " + eventObject);
                    break;
                case "event3":
                case "event4":
                    break;
            }
            return true; // if returns false then chain will be interrupted
        }
    }

Register it in the bus:

    eventBus.register(new SampleHolder(this));

Send event:

    eventBus.post("event1");
    eventBus.post("event2", "argument2");

Send event for all registered buses:

    EventBus.postAll("event3");
    EventBus.postAll("event4", "argument4");

Event will be posted in the order that holders were registered.

## Android specific

Some of Android tasks require executing in main thread. Then, set specific runner for all buses by following code:

    final Handler handler = new Handler(Looper.getMainLooper());
    EventBus.Runner runner = new EventBus.Runner() {
        @Override
        public void post(Runnable runnable) {
            handler.post(runnable);
        }
    };
    EventBus.setMainRunner(runner);

Or separately:

    eventBus.setRunner(runner);

_Note: `EventBus.setMainRunner()` overrides all previously defined runners._
