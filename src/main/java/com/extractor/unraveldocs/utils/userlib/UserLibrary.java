package com.extractor.unraveldocs.utils.userlib;

public class UserLibrary {
    public String capitalizeFirstLetterOfName(String name) {
        String firstLetter = name.substring(0, 1).toUpperCase();
        return  firstLetter + name.substring(1);
    }
}
