package com.dreamgames.backendengineeringcasestudy.mapper;

import com.dreamgames.backendengineeringcasestudy.model.entity.User;
import com.dreamgames.backendengineeringcasestudy.model.enums.Country;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setLevel(rs.getInt("level"));
        user.setCoins(rs.getInt("coins"));
        user.setCountry(Country.valueOf(rs.getString("country")));
        return user;
    }
}
