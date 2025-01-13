package org.lushplugins.followers.utils;

import org.jetbrains.annotations.Nullable;

public class SkinData {
    private String value;
    private String signature;

    public SkinData(String value, @Nullable String signature) {
        this.value = value;
        this.signature = signature;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public static SkinData empty() {
        return new SkinData(null, null);
    }
}
