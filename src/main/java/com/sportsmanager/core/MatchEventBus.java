package com.sportsmanager.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MatchEventBus {

    private final List<Consumer<MatchEvent>> listeners = new ArrayList<>();

    public void subscribe(Consumer<MatchEvent> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<MatchEvent> listener) {
        listeners.remove(listener);
    }

    public void publish(MatchEvent event) {
        for (Consumer<MatchEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
