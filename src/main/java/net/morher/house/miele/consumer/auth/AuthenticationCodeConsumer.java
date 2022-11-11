package net.morher.house.miele.consumer.auth;

public interface AuthenticationCodeConsumer {

    String getAuthCode(String username, String password, String location);

}