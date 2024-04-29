package com.dreamgames.backendengineeringcasestudy.jobs;

import com.dreamgames.backendengineeringcasestudy.model.entity.TournamentInfo;
import com.dreamgames.backendengineeringcasestudy.service.GroupService;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import com.dreamgames.backendengineeringcasestudy.service.cache.TournamentCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Service
public class TournamentJob {

    private final TournamentService tournamentService;
    private final GroupService groupService;
    private final TournamentCacheService tournamentCacheService;


    @Scheduled(cron = "0 0 0 * * *")
    public void startTournament() {

        Optional<TournamentInfo> currentTournamentInfo = tournamentCacheService.getTournamentInfo();
        if (currentTournamentInfo.isPresent()) {
            if (currentTournamentInfo.get().getIsActive().equals("true")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is already an active tournament.");
            }
        }

        // delete all existing tournament data
        tournamentCacheService.deleteAllTournamentData();

        tournamentCacheService.createCountryLeaderboard();

        long tournamentId = tournamentService.createTournament();

        TournamentInfo newTournamentInfo = new TournamentInfo();
        newTournamentInfo.setTournamentId(String.valueOf(tournamentId));
        newTournamentInfo.setIsActive("true");

        tournamentCacheService.setTournamentInfo(newTournamentInfo);

    }

    @Scheduled(cron = "0 0 20 * * *")
    public void endTournament() {

        TournamentInfo tournamentInfo = tournamentService.getActiveTournamentInfo();

        long tournamentId = Long.parseLong(tournamentInfo.getTournamentId());

        tournamentService.endTournament(tournamentId);

        tournamentInfo.setIsActive("false");
        tournamentInfo.setTournamentId(null);

        tournamentCacheService.setTournamentInfo(tournamentInfo);


        tournamentService.depositTournamentFeeToUsers();
        tournamentService.addTournamentRewardsToUsers();


        groupService.deleteAllGroups();

    }


}
