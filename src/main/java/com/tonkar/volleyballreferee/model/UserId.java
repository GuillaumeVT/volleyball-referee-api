package com.tonkar.volleyballreferee.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UserId {

    @NotEmpty
    private String socialId;
    @NotEmpty
    private String provider;

    public UserId() {}

    public UserId(@NotNull String socialId, @NotNull String provider) {
        this.socialId = socialId;
        this.provider = provider;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public static UserId VBR_USER_ID = new UserId("01022018", "VBR");

    @Override
    public String toString() {
        return socialId + '@' + provider;
    }
}
