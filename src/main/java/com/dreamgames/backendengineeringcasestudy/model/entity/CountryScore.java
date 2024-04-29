package com.dreamgames.backendengineeringcasestudy.model.entity;

import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CountryScore {
    Country country;
    int score;
}
