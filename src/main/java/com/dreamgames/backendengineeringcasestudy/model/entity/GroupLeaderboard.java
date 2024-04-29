package com.dreamgames.backendengineeringcasestudy.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class GroupLeaderboard {
    List<UserScore> userScores;
}
