package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.GroupDao;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupDao groupDao;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long createGroupWithAUser(long tournamentId, Long userId, Country country) {
        long groupId = groupDao.createGroup(tournamentId);
        groupDao.addUserToGroup(groupId, userId, country);
        return groupId;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long findAvailableGroup(Long userId, Country country) {
        int retry = 0;

        // retry mechanism to handle pessimistic locking (when using db transactions instead of monitors)
        Long groupId = null;
        while (retry < 5) {
            try {
                groupId = groupDao.findAvailableGroup(country);
                if (groupId == null) {
                    return null;
                }
                groupDao.addUserToGroup(groupId, userId, country);
                return groupId;
            } catch (PessimisticLockingFailureException e) {
                retry++;
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500));
                } catch (Exception ex) {
                    throw new RuntimeException("Error while sleeping: " + ex.getMessage());
                }
                groupId = null;
            }
        }
        return groupId;
    }

    public void setGroupStatus(long groupId, boolean isActive) {
        groupDao.setGroupStatus(groupId, isActive);
    }

    public void deleteAllGroups() {
        groupDao.deleteAllGroupMemberships();
        groupDao.deleteAllGroups();
    }

    public Optional<Long> getUsersGroupId(long userId) {
        return groupDao.getUsersGroupId(userId);
    }

    public List<Long> getInactiveGroups() {
        return groupDao.getInactiveGroupUserIds();
    }


}
