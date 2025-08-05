package com.livemotdmanager.core;

@FunctionalInterface
public interface PlaceholderProvider {
    String apply(String text, MotdContext ctx);
}
