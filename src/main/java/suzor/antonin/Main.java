package suzor.antonin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.JDALogger;

public class Main extends ListenerAdapter
{
    static String token = "SECRET";
    static String botID = "1275120972474875995";

    static String messageContentForTrigger = "https://cdn.discordapp.com/attachments/601015141601116162/1275094133853392896/image1.gif";
    static String messageToSendOnTrigger = "Invocation de <@477133451883970581> !";

    static long pingCooldownInMilliseconds = 1800000; // 30 minutes
    static Map<String, Date> lastPingsMap = new HashMap<>();

    static long selfDeleteCooldownInMilliseconds = 1500; // 1.5 seconds

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1 || args.length > 3)
        {
            System.err.println("ERROR: wrong usage: must give command-line arguments as such:\nbot_token [bot_id (default 1275120972474875995)] [ping_cooldown_in_milliseconds (default 1800000)] [self_delete_cooldown_in_milliseconds (default 1500)]");
            System.exit(1);
        }
        token = args[0];
        if (args.length >= 2)
        {
            botID = args[1];
        }
        if (args.length >= 3)
        {
            pingCooldownInMilliseconds = Long.parseLong(args[2]);
        }
        if (args.length >= 4)
        {
            selfDeleteCooldownInMilliseconds = Long.parseLong(args[3]);
        }
        
        JDALogger.setFallbackLoggerEnabled(false);

        JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
            .addEventListeners(new Main())
            .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (!event.isFromGuild())
        {
            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw();
        MessageChannel channel = event.getChannel();
        String serverID = event.getGuild().getId();

        if (event.getAuthor().getId().equals(botID))
        {
            channel.deleteMessageById(message.getId()).queueAfter(500, TimeUnit.MILLISECONDS);
        }

        if (content.contains(messageContentForTrigger))
        {
            if (isOnCooldown(serverID))
            {
                return;
            }
            channel.sendMessage(messageToSendOnTrigger).queue();
            lastPingsMap.put(serverID, new Date());
        }
    }

    public static boolean isOnCooldown(String serverID)
    {
        if (!lastPingsMap.containsKey(serverID))
        {
            return false;
        }
        return lastPingsMap.get(serverID).getTime() + pingCooldownInMilliseconds >= (new Date()).getTime();
    }
}