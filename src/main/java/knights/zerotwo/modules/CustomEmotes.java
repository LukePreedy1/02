package knights.zerotwo.modules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import knights.zerotwo.IActive;
import knights.zerotwo.IWrap;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CustomEmotes implements IWrap {
    static class CustomEmoteDefaultAction implements IActive {
        @Override
        public boolean test(MessageReceivedEvent event) {
            return false;
        }

        @Override
        public void apply(MessageReceivedEvent event, String messageContent) {
            Guild guild = event.getGuild();

            guild.getController()
                    .setNickname(guild.getSelfMember(), event.getMessage().getAuthor().getName())
                    .complete();

            event.getChannel().sendMessage(messageContent).complete();

            guild.getController().setNickname(guild.getSelfMember(), "").complete();
        }
    }

    private static final Pattern EMOTES = Pattern.compile(":([A-Za-z0-9_\\s]+):");
    private static final IActive DEFAULT_ACTIVE = new CustomEmoteDefaultAction();

    @Override
    public boolean test(MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        Matcher m = EMOTES.matcher(raw);
        while (m.find()) {
            int start = m.start();
            if (start == 0 || raw.charAt(start - 1) != '<') {
                return true;
            }
        }
        return false;
    }

    List<Emote> emotesPendingDeletion = new ArrayList<>();
    List<Message> messsagesPendingDeletion = new ArrayList<>();

    @Override
    public WrapResult preAction(MessageReceivedEvent event) {
        String raw = event.getMessage().getContentRaw();
        Matcher m = EMOTES.matcher(raw);

        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int start = m.start();
            if (start > 0 && raw.charAt(start - 1) == '<') {
                m.appendReplacement(sb, m.group());
                continue;
            }

            String emote = m.group(1);
            emote = emote.replace(" ", "_");
            if (event.getGuild().getEmotesByName(emote, false).size() != 0) {
                List<Emote> emotes1 = event.getGuild().getEmotesByName(emote, false);
                if (emotes1.size() != 0) {
                    m.appendReplacement(sb, emotes1.get(0).getAsMention());
                }
                continue;
            }

            InputStream img = this.getClass()
                    .getResourceAsStream("/custom-emotes/" + emote + ".png");
            if (img != null) {
                try {
                    Emote result = event.getGuild().getController()
                            .createEmote(emote, Icon.from(img)).complete();

                    m.appendReplacement(sb, result.getAsMention());

                    emotesPendingDeletion.add(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                List<Emote> emotes1 = event.getGuild().getEmotesByName(emote, false);
                if (emotes1.size() != 0) {
                    m.appendReplacement(sb, emotes1.get(0).getAsMention());
                }
            }
        }
        m.appendTail(sb);

        if (sb.length() < 2000) {
            return new WrapResult(sb.toString(), DEFAULT_ACTIVE);
        } else {
            event.getChannel()
                    .sendMessage("Only my darling can handle me like that. Don't even try.")
                    .queue();
            return new WrapResult(event.getMessage().getContentRaw(), NULL_ACTIVE);
        }
    }

    @Override
    public void postAction(MessageReceivedEvent event) {
        emotesPendingDeletion.forEach(emote -> emote.delete().queue());
        emotesPendingDeletion.clear();
    }

}
