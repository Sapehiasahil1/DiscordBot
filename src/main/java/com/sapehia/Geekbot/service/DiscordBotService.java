package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.CheckInResponse;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.CheckInResponseRepository;
import com.sapehia.Geekbot.repository.MemberRepository;
import com.sapehia.Geekbot.repository.ServerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class DiscordBotService extends ListenerAdapter {

    @Value("${discord.responses.channel.name:daily-responses}")
    private String responsesChannelName;

    @Autowired
    private CheckInResponseRepository checkInResponseRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ServerRepository serverRepository;

    private final ConcurrentMap<String, Integer> userQuestionState = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String[]> userResponses = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> userServerMap = new ConcurrentHashMap<>();
    private final Set<String> dailyRespondedUsers = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<String, String> adminStates = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> adminData = new ConcurrentHashMap<>();

    private final String[] DAILY_QUESTIONS = {
            "What did you complete yesterday?",
            "What are your plans for today?",
            "Are you stuck anywhere?"
    };

    private JDA jda;
    private LocalDate lastQuestionDate = LocalDate.now().minusDays(1);

    public DiscordBotService() {

    }

    public DiscordBotService(CheckInResponseRepository checkInResponseRepository) {
        this.checkInResponseRepository = checkInResponseRepository;
    }

    @Autowired
    public void setJda(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(ReadyEvent event) {
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        sendWelcomeMessage(event.getMember(), event.getGuild());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String userId = event.getAuthor().getId();

        if (event.isFromType(ChannelType.PRIVATE)) {
            if (userQuestionState.containsKey(userId)) {
                handleQuestionResponse(event);
            } else {
                handleAdminDM(event);
            }
            return;
        }

        if (userQuestionState.containsKey(userId)) {
            handleQuestionResponse(event);
        }
    }

    private void handleAdminDM(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        String messageContent = event.getMessage().getContentRaw().trim();

        if (messageContent.equalsIgnoreCase("stop")) {
            adminStates.remove(userId);
            adminData.remove(userId);
            event.getChannel().sendMessage("Operation cancelled.").queue();
            return;
        }

        List<Guild> adminGuilds = jda.getGuilds().stream()
                .filter(guild -> {
                    try {
                        Member member = guild.getMemberById(userId);
                        if (member == null) {
                            member = guild.retrieveMemberById(userId).complete();
                        }
                        return member != null && isAdmin(member);
                    } catch (Exception e) {
                        System.out.println("Error checking member in guild " + guild.getName() + ": " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        if (adminGuilds.isEmpty()) {
            event.getChannel().sendMessage("You don't have admin permissions.").queue();
            return;
        }

        String state = adminStates.get(userId);

        if (state == null) {
            adminStates.put(userId, "CHOOSING_OPTION");
            event.getChannel().sendMessage(
                    "**Admin Panel**\n" +
                            "1️⃣ Send message to a user\n" +
                            "2️⃣ Broadcast message to a server\n\n" +
                            "Type `1` or `2` to continue."
            ).queue();
            return;
        }

        switch (state) {
            case "CHOOSING_OPTION":
                if (messageContent.equals("1")) {
                    showServerSelection(event, adminGuilds, "SEND_TO_USER_SERVER_SELECT", "Select a server to find users:");
                } else if (messageContent.equals("2")) {
                    showServerSelection(event, adminGuilds, "BROADCAST_SERVER_SELECT", "Select a server to broadcast to:");
                } else {
                    event.getChannel().sendMessage("Invalid option. Please type `1` or `2`.").queue();
                }
                break;

            case "SEND_TO_USER_SERVER_SELECT":
                handleServerSelection(event, userId, messageContent, "SEND_TO_USER_MESSAGE", this::showUserList);
                break;

            case "SEND_TO_USER_MESSAGE":
                handleUserMessage(event, userId, messageContent);
                break;

            case "BROADCAST_SERVER_SELECT":
                handleServerSelection(event, userId, messageContent, "BROADCAST_MESSAGE", this::promptBroadcastMessage);
                break;

            case "BROADCAST_MESSAGE":
                handleBroadcastMessage(event, userId, messageContent);
                break;

            default:
                resetAdminState(userId);
                event.getChannel().sendMessage("Session expired. Please start again.").queue();
                break;
        }
    }

    private void showServerSelection(MessageReceivedEvent event, List<Guild> guilds, String nextState, String title) {
        StringBuilder serverList = new StringBuilder(title + "\n\n");
        for (int i = 0; i < guilds.size(); i++) {
            serverList.append("**").append(i + 1).append(".** ").append(guilds.get(i).getName()).append("\n");
        }
        serverList.append("\nType the number of the server.");

        adminStates.put(event.getAuthor().getId(), nextState);
        adminData.put(event.getAuthor().getId(), guilds.stream().map(Guild::getId).collect(Collectors.joining(",")));
        event.getChannel().sendMessage(serverList.toString()).queue();
    }

    private void handleServerSelection(MessageReceivedEvent event, String userId, String messageContent,
                                       String nextState, ServerSelectionHandler handler) {
        try {
            int serverIndex = Integer.parseInt(messageContent) - 1;
            String[] guildIds = adminData.get(userId).split(",");

            if (serverIndex < 0 || serverIndex >= guildIds.length) {
                event.getChannel().sendMessage("Invalid selection.").queue();
                return;
            }

            String selectedGuildId = guildIds[serverIndex];
            Guild selectedGuild = jda.getGuildById(selectedGuildId);

            if (selectedGuild == null) {
                event.getChannel().sendMessage("Server not found.").queue();
                resetAdminState(userId);
                return;
            }

            adminData.put(userId, selectedGuildId);
            adminStates.put(userId, nextState);
            handler.handle(event, selectedGuild);

        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Please enter a valid number.").queue();
        }
    }

    private void showUserList(MessageReceivedEvent event, Guild guild) {
        guild.loadMembers().onSuccess(members -> {
            List<Member> nonBotMembers = members.stream()
                    .filter(m -> !m.getUser().isBot())
                    .sorted((m1, m2) -> m1.getEffectiveName().compareToIgnoreCase(m2.getEffectiveName()))
                    .limit(20)
                    .collect(Collectors.toList());

            if (nonBotMembers.isEmpty()) {
                event.getChannel().sendMessage("No members found.").queue();
                resetAdminState(event.getAuthor().getId());
                return;
            }

            StringBuilder userList = new StringBuilder("**Users in " + guild.getName() + "**\n\n");
            for (Member member : nonBotMembers) {
                userList.append("• ").append(member.getEffectiveName())
                        .append(" - ID: `").append(member.getId()).append("`\n");
            }

            userList.append("\n**Type your message and mention the user:**\n");
            userList.append("Example: `@username Your message here`");

            event.getChannel().sendMessage(userList.toString()).queue();
        });
    }

    private void promptBroadcastMessage(MessageReceivedEvent event, Guild guild) {
        event.getChannel().sendMessage(
                "**Broadcasting to:** " + guild.getName() + "\n\n" +
                        "Type the message to broadcast:"
        ).queue();
    }

    private void handleUserMessage(MessageReceivedEvent event, String userId, String content) {
        List<User> mentionedUsers = event.getMessage().getMentions().getUsers();
        String targetUserId = null;
        String messageToUser;

        if (!mentionedUsers.isEmpty()) {
            targetUserId = mentionedUsers.get(0).getId();
            messageToUser = content.replaceAll("<@!?" + targetUserId + ">", "").trim();
        } else {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<@!?(\\d+)>");
            java.util.regex.Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                targetUserId = matcher.group(1);
                messageToUser = content.replaceAll("<@!?" + targetUserId + ">", "").trim();
            } else {
                event.getChannel().sendMessage("Please mention a user.").queue();
                return;
            }
        }

        if (messageToUser.isEmpty()) {
            event.getChannel().sendMessage("Message cannot be empty!").queue();
            return;
        }

        sendMessageToUser(userId, targetUserId, null, messageToUser);
        event.getChannel().sendMessage("Message sent successfully!").queue();
        resetAdminState(userId);
    }

    private void handleBroadcastMessage(MessageReceivedEvent event, String userId, String message) {
        if (message.isEmpty()) {
            event.getChannel().sendMessage("Message cannot be empty!").queue();
            return;
        }

        broadcastMessage(userId, message);
        event.getChannel().sendMessage("Broadcast sent successfully!").queue();
        resetAdminState(userId);
    }

    private void resetAdminState(String userId) {
        adminStates.remove(userId);
        adminData.remove(userId);
    }

    private void sendWelcomeMessage(Member member, Guild guild) {

        com.sapehia.Geekbot.model.Member memberEntity = new com.sapehia.Geekbot.model.Member();
        memberEntity.setDiscordUserId(member.getId());
        memberEntity.setUsername(member.getUser().getName());

        Server server = serverRepository.findById(guild.getId())
                .orElseThrow(() -> new RuntimeException("Server not found"));

        memberEntity.getServers().add(server);
        server.getServerMembers().add(memberEntity);

        memberRepository.save(memberEntity);
        serverRepository.save(server);

        String welcomeMessage = String.format(
                "🎉 Welcome to **%s**, %s! 🎉\n\n" +
                        "We're excited to have you here! Please read the rules and introduce yourself.\n" +
                        "If you have questions, ask our administrators. Enjoy your stay! 🌟",
                guild.getName(), member.getAsMention()
        );

        TextChannel channel = guild.getSystemChannel();
        if (channel == null) {
            channel = guild.getTextChannels().stream()
                    .filter(c -> c.getName().toLowerCase().contains("general"))
                    .findFirst()
                    .orElse(guild.getTextChannels().isEmpty() ? null : guild.getTextChannels().get(0));
        }

        if (channel != null && channel.canTalk()) {
            channel.sendMessage(welcomeMessage).queue();
        }
    }


    @Scheduled(cron = "0 40 9 * * ?")
    public void sendDailyQuestions() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastQuestionDate)) {
            dailyRespondedUsers.clear();
            lastQuestionDate = today;
        }

        for (Guild guild : jda.getGuilds()) {
            guild.loadMembers().onSuccess(members -> {
                for (Member member : members) {
                    if (member.getUser().isBot() || isAdmin(member)) continue;
                    String userKey = member.getId() + "_" + guild.getId();
                    if (dailyRespondedUsers.contains(userKey)) continue;
                    sendQuestionsToMember(guild, member);
                }
            });
        }
    }

    private void sendQuestionsToMember(Guild guild, Member member) {
        String questionsMessage = String.format(
                "Good morning, %s! \n\n" +
                        "Daily questions for **%s**:\n\n" +
                        "**1:** %s\n**2:** %s\n**3:** %s\n\n" +
                        "Reply with answers one by one!",
                member.getAsMention(), guild.getName(),
                DAILY_QUESTIONS[0], DAILY_QUESTIONS[1], DAILY_QUESTIONS[2]
        );

        member.getUser().openPrivateChannel().queue(channel -> {
            channel.sendMessage(questionsMessage).queue();
            userQuestionState.put(member.getId(), 0);
            userResponses.put(member.getId(), new String[3]);
            userServerMap.put(member.getId(), guild.getId());
            channel.sendMessage("**Question 1**: " + DAILY_QUESTIONS[0]).queue();
        });
    }

    private void handleQuestionResponse(MessageReceivedEvent event) {
        String userId = event.getAuthor().getId();
        int currentQuestion = userQuestionState.get(userId);

        userResponses.get(userId)[currentQuestion] = event.getMessage().getContentRaw();

        if (currentQuestion < 2) {
            userQuestionState.put(userId, currentQuestion + 1);
            event.getChannel().sendMessage(
                    "**Question " + (currentQuestion + 2) + "**: " + DAILY_QUESTIONS[currentQuestion + 1]
            ).queue();
        } else {
            completeQuestionSession(event, userId, userResponses.get(userId));
        }
    }

    private void completeQuestionSession(MessageReceivedEvent event, String userId, String[] responses) {
        String serverId = userServerMap.get(userId);

        CheckInResponse response = new CheckInResponse();
        response.setDiscordUserId(userId);
        response.setServerId(serverId);
        response.setDate(LocalDate.now());
        response.setQuestion1(responses[0]);
        response.setQuestion2(responses[1]);
        response.setQuestion3(responses[2]);
        response.setSubmittedAt(LocalDateTime.now());
        checkInResponseRepository.save(response);

        userQuestionState.remove(userId);
        userResponses.remove(userId);
        userServerMap.remove(userId);
        dailyRespondedUsers.add(userId + "_" + serverId);

        event.getChannel().sendMessage("Perfect! Your responses have been saved. Have a great day! ✨").queue();
//        shareResponseInServer(serverId, userId, responses);
    }

//    private void shareResponseInServer(String serverId, String userId, String[] responses) {
//        Guild guild = jda.getGuildById(serverId);
//        if (guild == null) return;
//
//        TextChannel channel = guild.getTextChannels().stream()
//                .filter(c -> c.getName().equalsIgnoreCase(responsesChannelName))
//                .findFirst()
//                .orElse(guild.getTextChannels().stream()
//                        .filter(c -> c.getName().toLowerCase().contains("general"))
//                        .findFirst()
//                        .orElse(guild.getTextChannels().isEmpty() ? null : guild.getTextChannels().get(0)));
//
//        if (channel == null) return;
//
//        String formattedResponse = String.format(
//                "**Daily Responses from <@%s>** - %s\n\n" +
//                        "**Q1:** %s\n%s\n\n**Q2:** %s\n%s\n\n**Q3:** %s\n%s",
//                userId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
//                DAILY_QUESTIONS[0], responses[0],
//                DAILY_QUESTIONS[1], responses[1],
//                DAILY_QUESTIONS[2], responses[2]
//        );
//
//        channel.sendMessage(formattedResponse).queue();
//    }

    public void sendMessageToUser(String adminId, String targetUserId, String channelId, String message) {
        Guild guild = jda.getGuilds().stream()
                .filter(g -> g.getMemberById(adminId) != null)
                .findFirst().orElse(null);

        if (guild == null) return;

        Member admin = guild.getMemberById(adminId);
        Member targetMember = guild.getMemberById(targetUserId);

        if (admin == null || !isAdmin(admin) || targetMember == null) return;

        String formattedMessage = targetMember.getAsMention() +
                ", message from " + admin.getAsMention() + " (Admin):\n\n" + message;

        targetMember.getUser().openPrivateChannel().queue(
                channel -> channel.sendMessage(formattedMessage).queue(),
                throwable -> {
                }
        );
    }

    public void broadcastMessage(String adminId, String message) {
        for (Guild guild : jda.getGuilds()) {
            Member admin = guild.getMemberById(adminId);
            if (admin != null && isAdmin(admin)) {
                TextChannel channel = guild.getSystemChannel();
                if (channel == null && !guild.getTextChannels().isEmpty()) {
                    channel = guild.getTextChannels().get(0);
                }
                if (channel != null) {
                    channel.sendMessage("📢 **Server Announcement** from " +
                            admin.getAsMention() + " (Admin)\n\n" + message).queue();
                }
            }
        }
    }

    private boolean isAdmin(Member member) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;

        Set<String> adminRoleNames = Set.of("Admin", "Administrator", "Owner", "Moderator");
        return member.getRoles().stream()
                .anyMatch(role -> adminRoleNames.contains(role.getName()));
    }

    @FunctionalInterface
    private interface ServerSelectionHandler {
        void handle(MessageReceivedEvent event, Guild guild);
    }
}