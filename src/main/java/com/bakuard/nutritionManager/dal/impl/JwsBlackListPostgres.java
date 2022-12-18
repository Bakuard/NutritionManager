package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.JwsBlackListRepository;
import com.bakuard.nutritionManager.validation.Validator;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.bakuard.nutritionManager.validation.Rule.*;

public class JwsBlackListPostgres implements JwsBlackListRepository {

    private JdbcTemplate statement;
    private Clock clock;

    public JwsBlackListPostgres(DataSource dataSource,
                                Clock clock) {
        statement = new JdbcTemplate(dataSource);
        this.clock = clock;
    }

    @Override
    public boolean addToBlackList(UUID tokenId, LocalDateTime expired) {
        Validator.check(
                "JwsBlackListPostgres.tokenId", notNull(tokenId),
                "JwsBlackListPostgres.expired", notNull(expired)
        );

        boolean isCorrect = LocalDateTime.now(clock).isBefore(expired) && !inBlackList(tokenId);

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
        Validator.check("JwsBlackListPostgres.tokenId", notNull(tokenId));

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
        Validator.check("JwsBlackListPostgres.deadline", notNull(deadline));

        return statement.update(
                """
                        DELETE FROM JwsBlackList WHERE expiration <= ?;
                        """,
                (PreparedStatement ps) -> ps.setTimestamp(1, Timestamp.valueOf(deadline))
        );
    }

}
