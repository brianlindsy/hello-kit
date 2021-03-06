package com.brianlindsey.SlackNewsletter;


import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.context.builtin.OAuthCallbackContext;
import com.slack.api.bolt.model.Installer;
import com.slack.api.bolt.model.builtin.DefaultInstaller;
import com.slack.api.bolt.request.builtin.OAuthCallbackRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.service.InstallationService;
import com.slack.api.bolt.service.builtin.oauth.OAuthSuccessHandler;
import com.slack.api.bolt.service.builtin.oauth.view.OAuthRedirectUriPageRenderer;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.oauth.OAuthAccessResponse;

import java.io.IOException;
import java.util.Arrays;

public class OAuthCustomSuccessHandler implements OAuthSuccessHandler {

    private final AppConfig appConfig;
    private final InstallationService installationService;
    private final OAuthRedirectUriPageRenderer pageRenderer;

    public OAuthCustomSuccessHandler(AppConfig appConfig, InstallationService installationService) {
        this.appConfig = appConfig;
        this.installationService = installationService;
        this.pageRenderer = appConfig.getOAuthRedirectUriPageRenderer();
    }

    @Override
    public Response handle(OAuthCallbackRequest request, Response response, OAuthAccessResponse o) {
        OAuthCallbackContext context = request.getContext();
        context.setEnterpriseId(o.getEnterpriseId());
        context.setTeamId(o.getTeamId());
        if (o.getBot() != null) {
            context.setBotUserId(o.getBot().getBotUserId());
            context.setBotToken(o.getBot().getBotAccessToken());
        }
        context.setRequestUserId(o.getUserId());
        context.setRequestUserToken(o.getAccessToken());

        DefaultInstaller.DefaultInstallerBuilder i = DefaultInstaller.builder()
                .appId(null)
                .enterpriseId(o.getEnterpriseId())
                .teamId(o.getTeamId())
                .teamName(o.getTeamName())
                .installerUserId(o.getUserId())
                .installerUserAccessToken(o.getAccessToken())
                .scope(o.getScope())
                .installedAt(System.currentTimeMillis());

        if (o.getIncomingWebhook() != null) {
            i = i.incomingWebhookChannelId(o.getIncomingWebhook().getChannelId())
                    .incomingWebhookUrl(o.getIncomingWebhook().getUrl())
                    .incomingWebhookConfigurationUrl(o.getIncomingWebhook().getConfigurationUrl());
        }

        if (o.getBot() != null) {
            i = i.botUserId(o.getBot().getBotUserId());
            i = i.botAccessToken(o.getBot().getBotAccessToken());
            try {
                AuthTestResponse authTest = context.client().authTest(r -> r);
                if (authTest.isOk()) {
                    i = i.botId(authTest.getBotId());
                } else {
                    System.out.println("Failed to call auth.test to fetch botId for the user: {} - {}");
                }
            } catch (SlackApiException | IOException e) {
            	System.out.println("Failed to call auth.test to fetch botId for the user: {}");
            }
        }
        Installer installer = i.build();

        try {
            installationService.saveInstallerAndBot(installer);
            String url = context.getOauthCompletionUrl();
            if (url != null) {
                response.setStatusCode(302);
                response.getHeaders().put("Location", Arrays.asList(url));
            } else {
                response.setStatusCode(200);
                response.setBody(pageRenderer.renderSuccessPage(installer, appConfig.getOauthCompletionUrl()));
                response.setContentType("text/html; charset=utf-8");
            }
        } catch (Exception e) {
        	System.out.println("Failed to store the installation");
            String url = context.getOauthCancellationUrl();
            if (url != null) {
                response.setStatusCode(302);
                response.getHeaders().put("Location", Arrays.asList(url));
            } else {
                String reason = e.getMessage();
                response.setStatusCode(200);
                response.setBody(pageRenderer.renderFailurePage(appConfig.getOauthInstallRequestURI(), reason));
                response.setContentType("text/html; charset=utf-8");
            }
        }
        System.out.println("success handler: " + response.getBody());
        return response;
    }

}

