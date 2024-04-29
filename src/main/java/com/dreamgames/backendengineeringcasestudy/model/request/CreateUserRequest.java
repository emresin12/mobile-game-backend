package com.dreamgames.backendengineeringcasestudy.model.request;

import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private int level;
    private int coins;
    private Country country;
}
