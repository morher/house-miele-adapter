package net.morher.house.miele.consumer.auth;

public interface OauthTokenConsumer {

    OauthToken fetchToken(String code);

    OauthToken refreshToken(OauthToken oldToken);

}