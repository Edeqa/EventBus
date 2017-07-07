# EventBus

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
        compile 'com.github.edeqa:eventbus:master-SNAPSHOT'
    }

## How to use

First, create event bus:

    eventBus = new EventBus("first");

or just:

    eventBus = new EventBus();

Make class inherited from AbstractEntityHolder and implement onEvent for handle events and make some logic.

    public class SampleHolder extends AbstractEntityHolder<MainActivity> {

        public SampleHolder(MainActivity context) {
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