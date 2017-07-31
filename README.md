# EventBus

[![](https://jitpack.io/v/edeqa/eventbus.svg)](https://jitpack.io/#edeqa/eventbus)

Simple event bus.

## How to add

### Gradle

Step 1. Add the JitPack repository in your root build.gradle at the end of repositories:

    allprojects {
        repositories {
            maven { url "https://jitpack.io" }
        }
    }

Step 2. Add the dependency in the app's build.gradle:

    dependencies {
        compile 'com.github.edeqa:eventbus:0.4'
    }

### Maven

Step 1. Add the JitPack repository to your build file:

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    
Step 2. Add the dependency:

    <dependency>
        <groupId>com.github.edeqa</groupId>
        <artifactId>eventbus</artifactId>
        <version>0.4</version>
    </dependency>

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

### Posting event

Post event:

    eventBus.post("event1");
    eventBus.post("event2", "argument2");

Post event to all registered buses:

    EventBus.postAll("event3");
    EventBus.postAll("event4", "argument4");

Event will be posted to all holders in the order that holders were registered.

You can use `fire` instead of `post` - it's a synonym:

    eventBus.fire("event1");

### Limit events spreading

If some `AbstractEntityHolder#onEvent` returns `false` then next holders in the queue will not be called.

You can also limit the spreading of events using `AbstractEntityHolder#events`:

    @Override
    public List<String> events() {
        List<String> list = new ArrayList<>();
        list.add("event1);
        list.add("event2);
        return list;
    }

Events `event1` and `event2` will be posted only to specific holder (or holders).

## Android UI specific

Some of Android tasks (i.e UI interaction) require performing in the main thread. Then, set specific runner for all buses by following code:

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

Note that `EventBus.setMainRunner` overrides all previously defined runners.

## What's new

0.5 - `fire` - synonym for `post`; fixes

0.4 - limit events

0.3 - fixes

0.2 - fixes

0.1 - initial
