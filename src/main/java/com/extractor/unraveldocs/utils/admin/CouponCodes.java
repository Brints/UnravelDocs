package com.extractor.unraveldocs.utils.admin;

public class CouponCodes {
    public String generateCouponCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder couponCode = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            couponCode.append(characters.charAt(randomIndex));
        }
        return couponCode.toString();
    }

    public String customCouponCode(String prefix) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder couponCode = new StringBuilder(prefix);
        for (int i = 0; i < 6; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            couponCode.append('-').append(characters.charAt(randomIndex));
        }
        return couponCode.toString();
    }
}
