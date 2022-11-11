package net.morher.house.miele;

import net.morher.house.api.context.HouseAdapter;
import net.morher.house.api.context.HouseMqttContext;
import net.morher.house.miele.config.MieleAdapterConfiguration;
import net.morher.house.miele.config.MieleAdapterConfiguration.MieleConfig;
import net.morher.house.miele.consumer.MieleConsumer;
import net.morher.house.miele.consumer.MieleConsumerImpl;
import net.morher.house.miele.consumer.auth.AuthenticatingMieleTokenManager;
import net.morher.house.miele.consumer.auth.MieleAuthenticationCodeConsumer;
import net.morher.house.miele.consumer.auth.MieleTokenManager;
import net.morher.house.miele.consumer.auth.OauthTokenConsumerImpl;
import net.morher.house.miele.controller.MieleController;

public class MieleAdapter implements HouseAdapter {

    public static void main(String[] args) {
        new MieleAdapter().run(new HouseMqttContext("miele-adapter"));
    }

    @Override
    public void run(HouseMqttContext ctx) {
        MieleConfig config = ctx.loadAdapterConfig(MieleAdapterConfiguration.class).getMiele();

        MieleTokenManager tokenManager = new AuthenticatingMieleTokenManager(
                config,
                new MieleAuthenticationCodeConsumer(config.getAuthCodeUrl(), config.getClientId()),
                new OauthTokenConsumerImpl(config.getTokenUrl(), config.getClientId(), config.getClientSecret()));
        MieleConsumer mieleConsumer = new MieleConsumerImpl(config.getApiUrl(), tokenManager);

        new MieleController(mieleConsumer, ctx.deviceManager())
                .configure(config);
    }
}
