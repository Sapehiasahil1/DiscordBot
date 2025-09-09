package com.sapehia.Geekbot.configuration;

import com.sapehia.Geekbot.service.DiscordBotService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfiguration {

    @Value("${discord.bot.token}")
    private String botToken;

    @Bean
    public JDA jda() throws LoginException {
        System.out.println("🚀 Creating JDA instance...");
        return JDABuilder.createDefault(botToken)
                .setActivity(Activity.playing("Managing your server!"))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.DIRECT_MESSAGES
                )
                .enableCache(CacheFlag.MEMBER_OVERRIDES)
                .setMemberCachePolicy(net.dv8tion.jda.api.utils.MemberCachePolicy.ALL)
                .build();
    }

    @Bean
    public DiscordBotService discordBotService(JDA jda) {
        System.out.println("🤖 Creating DiscordBotService...");
        DiscordBotService service = new DiscordBotService();
        service.setJda(jda);
        jda.addEventListener(service);
        return service;
    }
}