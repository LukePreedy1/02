package knights.zerotwo.modules;

import knights.zerotwo.IActive;
import knights.zerotwo.Utils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public class EmoteConfig implements IActive {

    @Override
    public boolean test(@Nonnull MessageReceivedEvent event) {
        return Utils.isCommand(event, "emote");
    }

    @Override
    public void apply(@Nonnull MessageReceivedEvent event, @Nonnull String messageContent) {
        if (messageContent.contains("off")) {
            Utils.NON_EMOTE_USERS.add(event.getAuthor().getId());
            event.getChannel().sendMessage("Emotes off for " + event.getAuthor().getAsMention())
                    .queue();
        } else if (messageContent.contains("on")) {
            Utils.NON_EMOTE_USERS.remove(event.getAuthor().getId());
            event.getChannel().sendMessage("Emotes on for " + event.getAuthor().getAsMention())
                    .queue();
        } else {
            event.getChannel().sendMessage("Nani??").queue();
        }
    }
}
