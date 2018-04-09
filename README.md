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
        compile 'com.github.edeqa:eventbus:3'
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
        <version>3</version>
    </dependency>

## How to use

First, create event bus:

    eventBus1 = new EventBus("first");
    eventBus2 = new EventBus("second");

or just using `DEFAULT_NAME`:

    eventBus = new EventBus();
    
or as a singleton:

    eventBusDefault = EventBus.getOrCreate(); // uses DEFAULT_NAME
    eventBus1 = EventBus.getOrCreate("first");

Make the class implementing `EntityHolder`:

    public class SampleHolder implements EntityHolder {
        ...
    }

But much better is to inherit the class from `AbstractEntityHolder` and implement `onEvent` for handle events and make some logic.

    public class SampleHolder extends AbstractEntityHolder {
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

    eventBus.register(new SampleHolder());

### Posting event

Post event:

    eventBus.post("event1");
    eventBus.post("event2", "argument2");

Post event to all registered buses:

    EventBus.postAll("event3");
    EventBus.postAll("event4", "argument4");

Event will be posted to all holders in the order that holders were registered.

### Specific task in queue

If you want to run some specific task in the same queue as events then use `post#Runnable`:

    eventBus.post(new Runnable() {
        @Override
        public void run() {
            LOGGER.severe("EventBus specific task.");
        }
    });

### Limit events spreading

If some `AbstractEntityHolder#onEvent` returns `false` then next holders in the queue will not be called.

You can also limit the spreading of events using `AbstractEntityHolder#events`:

    @Override
    public List<String> events() {
        List<String> list = new ArrayList<>();
        list.add("event1");
        list.add("event2");
        return list;
    }

Events `event1` and `event2` will be posted only to specific holder (or holders).

### Updating holder

You can update the holder without losing its position in the queue:

    eventBus.update(new SampleHolder());

## Runner

Methods `start`, `finish` and `onEvent` are processing through the same `Runner`. `Runner` is a simple class that wraps the `Runnable` into a specific mode. There are two runners predefined in the package: `RUNNER_MULTI_THREAD` and `RUNNER_SINGLE_THREAD`. The `DEFAULT_RUNNER` is the same as the `RUNNER_MULTI_THREAD`. If your environment doesn't allow to use multiple threads then call for each event bus:

    eventBus.setRunner(EventBus.RUNNER_SINGLE_THREAD);
    
Or, commonly:

    EventBus.setMainRunner(EventBus.RUNNER_SINGLE_THREAD);

Note that `EventBus.setMainRunner` overrides all previously defined runners.

## Android UI specific

Some of Android tasks (i.e UI interaction) require fulfillment in the main thread. Then, set specific runner for all buses by following code:

    final Handler handler = new Handler(Looper.getMainLooper());
    EventBus.Runner runner = new EventBus.Runner() {
        @Override
        public void post(Runnable runnable) {
            handler.post(runnable);
        }
    };
    EventBus.setMainRunner(runner);

Or, separately:

    eventBus1.setRunner(runner);
    eventBus2.setRunner(runner);
    eventBus3.setRunner(EventBus.RUNNER_SINGLE_THREAD);

Note that `EventBus.setMainRunner` overrides all previously defined runners.

## Troubleshooting

You can switch log details:

    EventBus.LOGGING_LEVEL = Level.ALL;
    
Possible values: `ALL`, `FINE`, `CONFIG`, `INFO`, `WARNING`, `SEVERE`. Default is `WARNING`.

### Inspect events

Deep inspection for specific events can be set next way:

    EventBus.inspect("event1");
    EventBus.inspect("event2");
    
This will throw the stacktrace when these events happen.

Cancel inspection:

    EventBus.inspect(null);

## Javadoc

See the [Javadoc](https://edeqa.github.io/EventBus/) for more details on API.

## History

3 - eventBus#registerIfAbsent

2 - throwing exceptions for EntityHolder#start, EntityHolder#finish, EntityHolder#onEvent; EventBus.RUNNER_MULTI_THREAD, EventBus.RUNNER_SINGLE_THREAD; -entityHolder#setContext; -EntityHolder(Context); removed T from EntityHolder<T>, so now it's just EntityHolder; EventBus#getOrCreateEventBus renamed to EventBus#getOrCreate

1.0 - EventBus#getEventBuses; EventBus#getEventBus; EventBus#getOrCreateEventBus; eventBus#getEventBusName; EventBus#setLoggingLevel; -EventBus#postSync; tests

0.8 - eventBus#registerOrUpdate; eventBus#unregister; javadoc

0.7 - refactoring to interface

0.6 - eventBus#postRunnable; docs

0.5 - update#holder; post#Runnable; EventBus#inspect; debug

0.4 - limit events

0.1 - initial

## License

EventBus is licensed under an MIT license. See the `LICENSE` file for specifics.
