package com.bakuard.nutritionManager.dto.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExceptionResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss(nn)")
    private LocalDateTime timestamp;
    private String httpStatus;
    private int httpErrorCode;
    private String message;
    private String title;
    private List<FieldExceptionResponse> reasons;

    public ExceptionResponse(HttpStatus status,
                             String message,
                             String title) {
        timestamp = LocalDateTime.now();
        httpStatus = status.getReasonPhrase();
        httpErrorCode = status.value();
        this.message = message;
        this.title = title;
        reasons = new ArrayList<>();
    }

    public void addReason(FieldExceptionResponse reason) {
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

    public List<FieldExceptionResponse> getReasons() {
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
