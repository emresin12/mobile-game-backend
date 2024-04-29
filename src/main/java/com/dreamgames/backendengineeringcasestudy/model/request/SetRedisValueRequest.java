package com.dreamgames.backendengineeringcasestudy.model.request;

import lombok.Data;

@Data
public class SetRedisValueRequest {
    private String key;
    private String value;
}
