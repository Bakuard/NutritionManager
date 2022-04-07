package com.bakuard.nutritionManager.dal.impl;

import com.bakuard.nutritionManager.dal.ImageRepository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageRepositoryPostgres implements ImageRepository {

    private JdbcTemplate statement;

    public ImageRepositoryPostgres(DataSource dataSource) {
        statement = new JdbcTemplate(dataSource);
    }

    @Override
    public void addImageUrl(UUID userId, String imageHash, URL imageUrl) {
        try {
            statement.update(
                    """
                            INSERT INTO UsedImages(userId, imageHash, imageUrl) VALUES (?, ?, ?);
                            """,
                    (PreparedStatement ps) -> {
                        ps.setObject(1, userId);
                        ps.setString(2, imageHash);
                        ps.setString(3, imageUrl.toString());
                    }
            );
        } catch(DuplicateKeyException e) {
            //if the image is already added - do nothing
        }
    }

    @Override
    public URL getImageUrl(UUID userId, String imageHash) {
        return statement.query(
                """
                        SELECT * FROM UsedImages
                            WHERE userId=? AND imageHash=?;
                        """,
                (PreparedStatement ps) -> {
                    ps.setObject(1, userId);
                    ps.setString(2, imageHash);
                },
                (ResultSet rs) -> {
                    URL url = null;

                    try {
                        if(rs.next()) {
                            url = new URL(rs.getString("imageUrl"));
                        }
                    } catch(MalformedURLException e) {
                        throw new RuntimeException(e);
                    }

                    return url;
                }
        );
    }

    @Override
    public List<String> getUnusedImages() {
        return statement.query(
                """
                        SELECT * FROM UsedImages
                            WHERE imageUrl NOT IN (
                                SELECT imagePath FROM Products
                                UNION
                                SELECT imagePath FROM Dishes
                                UNION
                                SELECT imagePath FROM Menus
                            );
                        """,
                (ResultSet rs) -> {
                    List<String> userImages = new ArrayList<>();

                    while(rs.next()) {
                        userImages.add(rs.getString("imageHash"));
                    }

                    return userImages;
                }
        );
    }

    @Override
    public void removeUnusedImages() {
        statement.update(
                """
                        DELETE FROM UsedImages
                            WHERE imageUrl NOT IN (
                                SELECT imagePath FROM Products
                                UNION
                                SELECT imagePath FROM Dishes
                                UNION
                                SELECT imagePath FROM Menus
                            );
                        """
        );
    }

}
