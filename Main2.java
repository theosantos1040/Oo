package com.stresser.model;

import lombok.Data;

@Data
public class AttackRequest {
    private String target;
    private int threads;
    private int connections;
    private int duration;
    private String method;
}
