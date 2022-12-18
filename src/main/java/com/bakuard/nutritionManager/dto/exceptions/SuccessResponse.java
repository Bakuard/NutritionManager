package com.bakuard.nutritionManager.dto.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@Schema(description = "Представляет стандартный ответ об успешном выполнении всех типов запросов, кроме GET.")
public class SuccessResponse<T> {

    @Schema(description = "Время отправки ответа")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss(nn)")
    private LocalDateTime timestamp;
    @Schema(description = "Текст сообщения пользователю подтверждающий успешное выполнение операции")
    private String message;
    @Schema(description = "Заголовок сообщения пользователю подтверждающий успешное выполнение операции")
    private String title;
    @Schema(description = "Дополнительные данные ответа на запрос связанные с бизнес логикой")
    private T body;

    public SuccessResponse(String message, String title, T body, Clock clock) {
        timestamp = LocalDateTime.now(clock);
        this.message = message;
        this.title = title;
        this.body = body;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public T getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SuccessResponse<?> that = (SuccessResponse<?>) o;
        return Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(message, that.message) &&
                Objects.equals(title, that.title) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, message, title, body);
    }

    @Override
    public String toString() {
        return "SuccessResponse{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", body=" + body +
                '}';
    }

}
