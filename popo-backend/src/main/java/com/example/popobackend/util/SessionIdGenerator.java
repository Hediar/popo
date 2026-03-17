package com.example.popobackend.util;

import java.util.UUID;

public class SessionIdGenerator {

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}