package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.model.Server;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class DiscordBotService extends ListenerAdapter {

    @Autowired
    private ServerService serverService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionAssignmentService questionAssignmentService;

    private JDA jda;

    private final ConcurrentMap<String, Integer> userQuestionState = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String[]> userResponses = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> userServerMap = new ConcurrentHashMap<>();
    private final Set<String> dailyRespondedUsers = ConcurrentHashMap.newKeySet();

    @Autowired
    public void setJda(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Bot is ready and connected!");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        Guild guild = event.getGuild();

        Server serverEntity = new Server();
        serverEntity.setServerId(guild.getId());
        serverEntity.setServerName(guild.getName());
        serverEntity.setQuestionTime(LocalTime.of(9, 30));

        final Server savedServer = serverService.addServer(serverEntity);
        createDefaultQuestions(savedServer);

        guild.loadMembers().onSuccess(members -> {
            for (Member member : members) {
                if (member.getUser().isBot()) continue;

                serverService.registerMemberToServer(
                        guild.getId(),
                        member.getId(),
                        member.getUser().getName()
                );
            }

            sendSetupInstructions(guild, savedServer);

            System.out.println("Stored new server: " + guild.getName());
        });
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Member discordMember = event.getMember();
        Guild guild = event.getGuild();

        serverService.registerMemberToServer(
                guild.getId(),
                discordMember.getId(),
                discordMember.getUser().getName()
        );

        sendWelcomeMessage(discordMember, guild);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();
        if (event.isFromType(ChannelType.PRIVATE) && userQuestionState.containsKey(userId)) {
            handleQuestionResponse(event);
        }
    }

    private void sendWelcomeMessage(Member member, Guild guild) {
        String welcomeMessage = String.format(
                "🎉 Welcome to **%s**, %s! 🎉\n\n" +
                        "Please read the rules and introduce yourself. Enjoy your stay! 🌟",
                guild.getName(), member.getAsMention()
        );

        TextChannel channel = guild.getSystemChannel();
        if (channel != null && channel.canTalk()) {
            channel.sendMessage(welcomeMessage).queue();
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    public void sendDailyQuestions() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalDate today = LocalDate.now();

        for (Server server : serverService.getAllServers()) {
            if (server.getQuestionTime() != null && server.getQuestionTime().equals(now)) {
                Guild guild = jda.getGuildById(server.getServerId());
                if (guild != null) {
                    guild.loadMembers().onSuccess(members -> {
                        for (Member member : members) {
                            if (member.getUser().isBot() || isAdmin(member)) continue;
                            String userKey = member.getId() + "_" + guild.getId();

                            if (!dailyRespondedUsers.contains(userKey)) {
                                sendQuestionsToMember(guild, member, server, today);
                            }
                        }
                    });
                }
            }
        }
    }

    private void sendQuestionsToMember(Guild guild, Member member, Server server, LocalDate today) {
        List<Question> questions = questionService.getQuestionsForServer(server.getServerId());
        if (questions.isEmpty()) return;

        member.getUser().openPrivateChannel().queue(channel -> {
            String introMessage = "Good morning, " + member.getAsMention() + "! 🌞\n\n" +
                    "Time for your daily check-in for **" + guild.getName() + "**!\n" +
                    "I'll ask you " + questions.size() + " questions one by one.\n\n" +
                    "Let's start with the first question:";

            channel.sendMessage(introMessage).queue(m -> {
                userQuestionState.put(member.getId(), 0);
                userResponses.put(member.getId(), new String[questions.size()]);
                userServerMap.put(member.getId(), guild.getId());

                channel.sendMessage("**Question 1:** " + questions.get(0).getText()).queueAfter(1, TimeUnit.SECONDS);
            });
        });
    }

    private void handleQuestionResponse(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();

        if (!userQuestionState.containsKey(userId)) {
            return;
        }

        int currentQuestionIndex = userQuestionState.get(userId);
        String serverId = userServerMap.get(userId);
        List<Question> questions = questionService.getQuestionsForServer(serverId);

        userResponses.get(userId)[currentQuestionIndex] = event.getMessage().getContentRaw();

        if (currentQuestionIndex < questions.size() - 1) {
            int nextQuestionIndex = currentQuestionIndex + 1;
            userQuestionState.put(userId, nextQuestionIndex);

            event.getChannel().sendMessage(
                    "**Question " + (nextQuestionIndex + 1) + ":** " + questions.get(nextQuestionIndex).getText()
            ).queue();
        } else {
            completeQuestionSession(event, userId, userResponses.get(userId), questions);

            event.getChannel().sendMessage(
                    "✅ Thank you! Your daily check-in is now complete. Have a great day! 🌟"
            ).queue();
        }
    }

    private void completeQuestionSession(
            MessageReceivedEvent event,
            String userId,
            String[] responses,
            List<Question> questions
    ) {
        String serverId = userServerMap.get(userId);
        com.sapehia.Geekbot.model.Member memberEntity =
                memberService.getMemberByDiscordUserId(userId);

        LocalDate today = LocalDate.now();

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);

            QuestionAssignment assignment = questionAssignmentService
                    .getByServerIdAndQuestionIdAndAssignedDate(serverId, question.getId(), today);

            Answer answer = new Answer();
            answer.setMember(memberEntity);
            answer.setAssignment(assignment);
            answer.setResponse(responses[i]);
            answer.setSubmittedAt(LocalDateTime.now());

            answerService.addAnswer(answer);
        }

        userQuestionState.remove(userId);
        userResponses.remove(userId);
        userServerMap.remove(userId);
        dailyRespondedUsers.add(userId + "_" + serverId);

        event.getChannel().sendMessage("✅ Your responses have been saved. Have a great day!").queue();
    }

    private void createDefaultQuestions(Server serverEntity) {

        List<String> defaultQuestions = List.of(
                "What did you complete yesterday?",
                "What are your plans for today?",
                "Are you stuck anywhere?"
        );

        for (String text : defaultQuestions) {
            Question question = new Question();
            question.setText(text);
            question.setServer(serverEntity);

            Question savedQuestion = questionService.createQuestion(question);

            QuestionAssignment assignment = new QuestionAssignment();
            assignment.setServer(serverEntity);
            assignment.setQuestion(savedQuestion);
            assignment.setDate(LocalDate.now());

            questionAssignmentService.save(assignment);
        }
    }

    private void sendSetupInstructions(Guild guild, Server server) {
        String setupUrl = "https://discordbot-fdr5.onrender.com/"+ server.getServerId() +"/configuration";

        String message = "👋 Thanks for adding me to **" + guild.getName() + "**!\n\n"
                + "✅ Default daily check-in questions have been set:\n"
                + "1. What did you complete yesterday?\n"
                + "2. What are your plans for today?\n"
                + "3. Are you stuck anywhere?\n\n"
                + "⏰ Default time: **09:30 AM**\n\n"
                + "You can customize questions and time anytime here:\n" + setupUrl;

        TextChannel systemChannel = guild.getSystemChannel();
        if (systemChannel != null && systemChannel.canTalk()) {
            systemChannel.sendMessage(message).queue();
        } else {
            guild.retrieveOwner().queue(owner -> {
                if (owner != null) {
                    owner.getUser().openPrivateChannel()
                            .flatMap(channel -> channel.sendMessage(message))
                            .queue();
                }
            });
        }
    }

    private boolean isAdmin(Member member) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        Set<String> adminRoleNames = Set.of("Admin", "Administrator", "Owner", "Moderator");
        return member.getRoles().stream()
                .anyMatch(role -> adminRoleNames.contains(role.getName()));
    }
}