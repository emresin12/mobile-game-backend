package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.model.entity.GroupLeaderboard;
import com.dreamgames.backendengineeringcasestudy.model.entity.User;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import com.dreamgames.backendengineeringcasestudy.model.request.CreateUserRequest;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.STARTING_COINS;
import static com.dreamgames.backendengineeringcasestudy.constants.AppConstants.STARTING_LEVEL;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final TournamentService tournamentService;

    @GetMapping("/get/{userId}")
    public User getUserById(@PathVariable int userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/create/{username}")
    public User createUser(@PathVariable String username) {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername(username);
        createUserRequest.setLevel(STARTING_LEVEL);
        createUserRequest.setCoins(STARTING_COINS);
        createUserRequest.setCountry(Country.getRandomCountry());
        return userService.createUser(createUserRequest);
    }

    @GetMapping("/enter-tournament/{userId}")
    public GroupLeaderboard enterTournament(@PathVariable long userId) {
        return userService.enterTournament(userId);
    }

    @PostMapping("/create-test")
    public User createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @GetMapping("/progress/{userId}")
    public User progressUserLevel(@PathVariable long userId) {
        return userService.progressUserLevel(userId);
    }

    @GetMapping("/claim-reward/{userId}")
    public User claimReward(@PathVariable long userId) {
        return userService.claimReward(userId);
    }

    @GetMapping("/get-user-rank/{userId}")
    public int getUserRank(@PathVariable long userId) {
        return tournamentService.getUserRank(userId);
    }


}
