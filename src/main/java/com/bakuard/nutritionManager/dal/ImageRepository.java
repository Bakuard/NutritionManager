package com.bakuard.nutritionManager.dal;

import java.net.URL;
import java.util.List;
import java.util.UUID;

/**
 * Хранит URL всех загруженных изображений. Используется чтобы определять изображения, которые больше
 * не нужны пользователю.
 */
public interface ImageRepository {

    /**
     * Сохраняет URL загреженного изображения.
     * @param userId идентфикатор пользователя, который загрузил изображение.
     * @param imageHash хеш-сумма изображения рассчитаная с помощью алгоритма MD5.
     */
    public void addImageUrl(UUID userId, String imageHash, URL imageUrl);

    /**
     * Возвращает URL изображения имеющего указанную хеш-сумму и загруженного пользователем с указанным
     * идентификатором. Если такого изображения нет - возвращает null.
     * @param userId идентфикатор пользователя, который загрузил изображение.
     * @param imageHash хеш-сумма изображения рассчитаная с помощью алгоритма MD5.
     * @return URL изображения или null.
     */
    public URL getImageUrl(UUID userId, String imageHash);

    /**
     * Возвращает хеш-суммы всех изображений, которые больше не используются своими пользователями.
     * Если таких изображений нет - возвращает пустой список.
     * @return список хеш-сумм всех изображений.
     */
    public List<String> getUnusedImages();

    /**
     * Удаляет хеш-суммы всех изображений, которые больше не используются своими пользователями.
     */
    public void removeUnusedImages();

}
