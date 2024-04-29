package com.dreamgames.backendengineeringcasestudy.model.entity;

import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class User {
    private Long userId;
    private String username;
    private int level;
    private int coins;
    private Country country;
}
