package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;

public class UserLibrary {
    public String capitalizeFirstLetterOfName(String name) {
        String firstLetter = name.substring(0, 1).toUpperCase();
        return  firstLetter + name.substring(1);
    }

    public String generateStrongPassword(int length) {
        if (length < 8) {
            throw new BadRequestException("Password length must be at least 8 characters.");
        }

        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+<>?";

        String allChars = upperCaseChars + lowerCaseChars + numbers + specialChars;

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * allChars.length());
            password.append(allChars.charAt(randomIndex));
        }

        return password.toString();
    }
}
