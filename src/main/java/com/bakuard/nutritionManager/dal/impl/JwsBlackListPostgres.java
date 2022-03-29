package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.validation.Rule;
import com.bakuard.nutritionManager.validation.ValidateException;

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
        ValidateException.check(
                Rule.of("JwsBlackListPostgres.tokenId").notNull(tokenId),
                Rule.of("JwsBlackListPostgres.expired").notNull(expired)
        );

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
        ValidateException.check(
                Rule.of("JwsBlackListPostgres.tokenId").notNull(tokenId)
        );

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
        ValidateException.check(
                Rule.of("JwsBlackListPostgres.deadline").notNull(deadline)
        );

        return statement.update(
                """
                        DELETE FROM JwsBlackList WHERE expiration <= ?;
                        """,
                (PreparedStatement ps) -> ps.setTimestamp(1, Timestamp.valueOf(deadline))
        );
    }

}
