# EventBus

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

Use:

- first, create event bus:

    eventBus = new EventBus("first");

or just:

    eventBus = new EventBus();

Make class inherited from AbstractEntityHolder and register it in the bus:

    public class SampleHolder extends AbstractEntityHolder<MainActivity> {

        public SampleHolder(MainActivity context) {
            super(context);
        }

        @Override
        public boolean onEvent(String eventName, Object eventObject) {
            System.out.println("Event: "+eventName+", object: "+eventObject);
            return true;
        }
    }

    ...

    eventBus.register(new SampleHolder(this));

Send event:

    eventBus.post("event1");
    eventBus.post("event2", "argument2");
    eventBus.postSync("event3");
    eventBus.postSync("event4", "argument4");

Send event for all registered buses:

    EventBus.postAll("event5");
    EventBus.postAll("event6", "argument6");
