package com.extractor.unraveldocs.subscription.enums;

import lombok.Getter;

@Getter
public enum SubscriptionCurrency {
    USD("United States Dollar"),
    EUR("Euro"),
    GBP("British Pound Sterling"),
    INR("Indian Rupee"),
    JPY("Japanese Yen"),
    AUD("Australian Dollar"),
    CAD("Canadian Dollar"),
    CNY("Chinese Yuan Renminbi"),
    RUB("Russian Ruble"),
    BRL("Brazilian Real"),
    NGN("Nigerian Naira"),
    ZAR("South African Rand"),
    MXN("Mexican Peso"),
    KRW("South Korean Won"),
    CHF("Swiss Franc"),
    SEK("Swedish Krona"),
    NZD("New Zealand Dollar"),
    AED("United Arab Emirates Dirham"),
    SGD("Singapore Dollar"),
    HKD("Hong Kong Dollar"),
    TRY("Turkish Lira"),
    PLN("Polish Zloty");

    private final String fullName;

    SubscriptionCurrency(String fullName) {
        this.fullName = fullName;
    }

    public static SubscriptionCurrency fromString(String currencyName) {
        for (SubscriptionCurrency currency : SubscriptionCurrency.values()) {
            if (currency.name().equalsIgnoreCase(currencyName)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("No enum constant with name: " + currencyName);
    }
}
