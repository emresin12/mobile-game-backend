package com.dreamgames.backendengineeringcasestudy.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserScore {

    private String username;
    private int score;
}
