package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.jobs.TournamentJob;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tournament/")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentJob t;

    @GetMapping("/start")
    public void startTournament() {
        t.startTournament();
    }

    @GetMapping("/end")
    public void endTournament() {
        t.endTournament();
    }


}
