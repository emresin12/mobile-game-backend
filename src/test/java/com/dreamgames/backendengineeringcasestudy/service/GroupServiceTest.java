package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dao.GroupDao;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupDao groupDao;

    @InjectMocks
    private GroupService groupService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void createGroupWithAUser_shouldCreateGroupAndAddUser() {
        long tournamentId = 1L;
        long userId = 1L;
        Country country = Country.TURKEY;
        long groupId = 1L;

        when(groupDao.createGroup(tournamentId)).thenReturn(groupId);

        long result = groupService.createGroupWithAUser(tournamentId, userId, country);

        assertEquals(groupId, result);
        verify(groupDao).createGroup(tournamentId);
        verify(groupDao).addUserToGroup(groupId, userId, country);
    }

    @Test
    void findAvailableGroup_shouldFindAndAddUserToGroup() {
        long userId = 1L;
        Country country = Country.TURKEY;
        Long groupId = 1L;

        when(groupDao.findAvailableGroup(country)).thenReturn(groupId);

        Long result = groupService.findAvailableGroup(userId, country);

        assertEquals(groupId, result);
        verify(groupDao).findAvailableGroup(country);
        verify(groupDao).addUserToGroup(groupId, userId, country);
    }

    @Test
    void findAvailableGroup_shouldReturnNullWhenNoGroupAvailable() {
        Long userId = 1L;
        Country country = Country.TURKEY;

        when(groupDao.findAvailableGroup(country)).thenReturn(null);

        Long result = groupService.findAvailableGroup(userId, country);

        assertNull(result);
        verify(groupDao).findAvailableGroup(country);
        verify(groupDao, never()).addUserToGroup(anyLong(), anyLong(), any());
    }

    @Test
    void findAvailableGroup_shouldRetryWhenPessimisticLockingFailureException() {
        long userId = 1L;
        Country country = Country.TURKEY;
        Long groupId = 1L;

        when(groupDao.findAvailableGroup(country))
                .thenThrow(new PessimisticLockingFailureException("Lock failed"))
                .thenReturn(groupId);

        Long result = groupService.findAvailableGroup(userId, country);

        assertEquals(groupId, result);
        verify(groupDao, times(2)).findAvailableGroup(country);
        verify(groupDao).addUserToGroup(groupId, userId, country);
    }

    @Test
    void setGroupStatus_shouldSetGroupStatus() {
        long groupId = 1L;
        boolean isActive = true;

        groupService.setGroupStatus(groupId, isActive);

        verify(groupDao).setGroupStatus(groupId, isActive);
    }

    @Test
    void deleteAllGroups_shouldDeleteAllGroupsAndMemberships() {
        groupService.deleteAllGroups();

        verify(groupDao).deleteAllGroupMemberships();
        verify(groupDao).deleteAllGroups();
    }

    @Test
    void getUsersGroupId_shouldReturnUsersGroupId() {
        long userId = 1L;
        Long groupId = 1L;

        when(groupDao.getUsersGroupId(userId)).thenReturn(Optional.of(groupId));

        Optional<Long> result = groupService.getUsersGroupId(userId);

        assertTrue(result.isPresent());
        assertEquals(groupId, result.get());
        verify(groupDao).getUsersGroupId(userId);
    }

    @Test
    void getUsersGroupId_shouldReturnEmptyOptionalWhenUserHasNoGroup() {
        long userId = 1L;

        when(groupDao.getUsersGroupId(userId)).thenReturn(Optional.empty());

        Optional<Long> result = groupService.getUsersGroupId(userId);

        assertFalse(result.isPresent());
        verify(groupDao).getUsersGroupId(userId);
    }

    @Test
    void getInactiveGroups_shouldReturnInactiveGroupUserIds() {
        List<Long> inactiveGroupUserIds = Arrays.asList(1L, 2L, 3L);

        when(groupDao.getInactiveGroupUserIds()).thenReturn(inactiveGroupUserIds);

        List<Long> result = groupService.getInactiveGroups();

        assertEquals(inactiveGroupUserIds, result);
        verify(groupDao).getInactiveGroupUserIds();
    }
}