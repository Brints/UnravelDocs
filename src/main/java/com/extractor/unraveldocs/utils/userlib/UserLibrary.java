package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;

public class UserLibrary {
    public String capitalizeFirstLetterOfName(String name) {
        String firstLetter = name.substring(0, 1).toUpperCase();
        return  firstLetter + name.substring(1).toLowerCase();
    }

    public String generateStrongPassword(int passwordLength) {
        if (passwordLength < 8) {
            throw new BadRequestException("Password length must be at least 8 characters.");
        }

        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+<>?";

        String allChars = upperCaseChars + lowerCaseChars + numbers + specialChars;

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = (int) (Math.random() * allChars.length());
            password.append(allChars.charAt(randomIndex));
        }

        return password.toString();
    }

    public String generateStrongPassword(int passwordLength, char[] excludedChars) {
        if (passwordLength < 10) {
            throw new BadRequestException("Password length must be at least 10 characters.");
        }

        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+<>?";

        String allChars = upperCaseChars + lowerCaseChars + numbers + specialChars;

        for (char excludedChar : excludedChars) {
            allChars = allChars.replace(String.valueOf(excludedChar), "");
        }

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < passwordLength; i++) {
            int randomIndex = (int) (Math.random() * allChars.length());
            password.append(allChars.charAt(randomIndex));
        }

        return password.toString();
    }
}
