package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.model.entity.CountryLeaderboard;
import com.dreamgames.backendengineeringcasestudy.model.entity.GroupLeaderboard;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.NUMBER_OF_COUNTRIES;
import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.NUMBER_OF_GROUPS;

@RestController
@RequestMapping("/leaderboard/")
@RequiredArgsConstructor
public class LeaderboardController {

    private final TournamentCacheService tournamentCacheService;

    @GetMapping("/group/{groupId}")
    public GroupLeaderboard getGroupLeaderboard(@PathVariable long groupId) {
        return tournamentCacheService.getGroupLeaderboard(groupId, 0, NUMBER_OF_GROUPS);
    }

    @GetMapping("/group/by-user-id/{userId}")
    public GroupLeaderboard getGroupLeaderboardByUserId(@PathVariable long userId) {
        return tournamentCacheService.getGroupLeaderboardByUserId(userId, 0, NUMBER_OF_GROUPS);
    }


    @GetMapping("/country")
    public CountryLeaderboard getCountryLeaderboard() {
        return tournamentCacheService.getCountriesLeaderboard(0, NUMBER_OF_COUNTRIES);
    }

}
