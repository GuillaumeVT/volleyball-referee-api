package com.tonkar.volleyballreferee.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookIdToken {

    @JsonProperty("data")
    private FacebookPayload payload;
    @JsonProperty("error")
    private FacebookError   error;

    public FacebookIdToken() {}

    public FacebookPayload getPayload() {
        return payload;
    }

    public FacebookError getError() {
        return error;
    }

    public static class FacebookPayload {

        @JsonProperty("app_id")
        private String  appId;
        @JsonProperty("user_id")
        private String  userId;
        @JsonProperty("is_valid")
        private boolean isValid;

        public FacebookPayload() {}

        public String getAppId() {
            return appId;
        }

        public String getUserId() {
            return userId;
        }

        public boolean isValid() {
            return isValid;
        }
    }

    public static class FacebookError {

        @JsonProperty("type")
        private String type;
        @JsonProperty("code")
        private int    code;

        public FacebookError() {}

        public String getType() {
            return type;
        }

        public int getCode() {
            return code;
        }
    }
}
