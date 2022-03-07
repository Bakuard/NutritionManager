package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.model.exceptions.Validator;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class JwsBlackListPostgres implements JwsBlackListRepository {

    private JdbcTemplate statement;

    public JwsBlackListPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean addToBlackList(UUID tokenId, LocalDateTime expired) {
        Validator.create().
                notNull("tokenId", tokenId).
                notNull("expired", expired).
                validate("Fail to add token to black list");

        boolean isCorrect = LocalDateTime.now().compareTo(expired) < 0 && !inBlackList(tokenId);

        if(isCorrect) {
            statement.update(
                    """
                            INSERT INTO JwsBlackList(tokenId, expiration)
                             VALUES (?, ?);
                            """,
                    (PreparedStatement ps) -> {
                        ps.setObject(1, tokenId);
                        ps.setTimestamp(2, Timestamp.valueOf(expired));
                    }
            );
        }

        return isCorrect;
    }

    @Override
    public boolean inBlackList(UUID tokenId) {
        Validator.create().
                notNull("tokenId", tokenId).
                validate("Fail check - is there tokenId in black list");

        return statement.query(
                """
                        SELECT tokenId FROM JwsBlackList WHERE tokenId = ?;
                        """,
                (PreparedStatement ps) -> ps.setObject(1, tokenId),
                (ResultSet rs) -> rs.next()
        );
    }

    @Override
    public int removeAllExpired(LocalDateTime deadline) {
        Validator.create().
                notNull("deadline", deadline).
                validate("Fail to remove all expired tokens");

        return statement.update(
                """
                        DELETE FROM JwsBlackList WHERE expiration <= ?;
                        """,
                (PreparedStatement ps) -> ps.setTimestamp(1, Timestamp.valueOf(deadline))
        );
    }

}
