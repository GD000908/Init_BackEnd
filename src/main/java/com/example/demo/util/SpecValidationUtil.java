package com.example.demo.util;

import com.example.demo.exception.SpecValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class SpecValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$");
    
    private static final Pattern URL_PATTERN = 
        Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static void validateEmail(String email) {
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw SpecValidationException.invalidEmail(email);
        }
    }
    
    public static void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw SpecValidationException.invalidPhone(phone);
        }
    }
    
    public static void validateUrl(String url) {
        if (url != null && !url.trim().isEmpty() && !URL_PATTERN.matcher(url).matches()) {
            throw SpecValidationException.invalidUrl(url);
        }
    }
    
    public static void validateSkillLevel(Integer level) {
        if (level != null && (level < 1 || level > 5)) {
            throw SpecValidationException.invalidSkillLevel(level);
        }
    }
    
    public static void validateDateRange(String startDate, String endDate) {
        if (startDate != null && endDate != null && 
            !startDate.trim().isEmpty() && !endDate.trim().isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
                LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
                
                if (start.isAfter(end)) {
                    throw SpecValidationException.invalidDateRange(startDate, endDate);
                }
            } catch (DateTimeParseException e) {
                throw new SpecValidationException("잘못된 날짜 형식입니다. yyyy-MM-dd 형식을 사용해주세요.");
            }
        }
    }
    
    public static void validateRequiredField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw SpecValidationException.emptyRequiredField(fieldName);
        }
    }
    
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
    
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new SpecValidationException("잘못된 날짜 형식입니다: " + dateString + ". yyyy-MM-dd 형식을 사용해주세요.");
        }
    }
}
