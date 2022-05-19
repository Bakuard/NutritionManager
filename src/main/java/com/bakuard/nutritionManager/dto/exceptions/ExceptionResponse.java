package com.bakuard.nutritionManager.dto.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "Содержит данные обо всех ошибках возникших в процессе выполнения конкретного запроса")
public class ExceptionResponse {

    @Schema(description = "Время возникновения ошибки")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss(nn)")
    private LocalDateTime timestamp;
    @Schema(description = "Http статус ошибки")
    private String httpStatus;
    @Schema(description = "Http код ошибки")
    private int httpErrorCode;
    @Schema(description = "Текст сообщения пользователю об ошибке относящийся ко всему запросу в целом")
    private String message;
    @Schema(description = "Заголовок сообщения пользователю об ошибке")
    private String title;
    @Schema(description = "Все пречины из-за которых запрос не смог завершится корректно")
    private List<ConstraintResponse> reasons;

    public ExceptionResponse(HttpStatus status) {
        timestamp = LocalDateTime.now();
        httpStatus = status.getReasonPhrase();
        httpErrorCode = status.value();
        reasons = new ArrayList<>();
    }

    public void addReason(ConstraintResponse reason) {
        reasons.add(reason);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public int getHttpErrorCode() {
        return httpErrorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public List<ConstraintResponse> getReasons() {
        return reasons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExceptionResponse that = (ExceptionResponse) o;
        return httpErrorCode == that.httpErrorCode &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(httpStatus, that.httpStatus) &&
                Objects.equals(message, that.message) &&
                Objects.equals(title, that.title) &&
                Objects.equals(reasons, that.reasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, httpStatus, httpErrorCode, message, title, reasons);
    }

    @Override
    public String toString() {
        return "ExceptionResponse{" +
                "timestamp=" + timestamp +
                ", httpStatus='" + httpStatus + '\'' +
                ", httpErrorCode=" + httpErrorCode +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", errors=" + reasons +
                '}';
    }

}
