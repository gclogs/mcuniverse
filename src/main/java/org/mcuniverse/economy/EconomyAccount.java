package org.mcuniverse.economy;

public enum EconomyAccount {
    BALANCE("balance"),
    CASH("cash");

    private final String fieldName;

    EconomyAccount(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
