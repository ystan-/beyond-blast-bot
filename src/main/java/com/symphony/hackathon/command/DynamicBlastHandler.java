package com.symphony.hackathon.command;

import authentication.SymOBORSAAuth;
import authentication.SymOBOUserRSAAuth;
import clients.SymBotClient;
import clients.SymOBOClient;
import com.github.jknack.handlebars.Handlebars;
import com.symphony.hackathon.service.TemplatesService;
import configuration.SymConfig;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class DynamicBlastHandler implements ElementsResponse {
    private final SymBotClient bot;
    private final SymOBORSAAuth oboAuth;
    private final SymConfig botConfig;
    private final TemplatesService template;

    public DynamicBlastHandler(SymBotClient bot,
                               SymOBORSAAuth oboAuth,
                               SymConfig botConfig,
                               TemplatesService template) {
        this.bot = bot;
        this.oboAuth = oboAuth;
        this.botConfig = botConfig;
        this.template = template;
    }

    @SuppressWarnings("unchecked")
    public void execute(User user, SymphonyElementsAction action) {
        bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage("Sending blast.."));

        Map<String, Object> formValues = action.getFormValues();

        String userTemplate = formValues.get("template").toString();
        List<Long> recipients = (List<Long>) formValues.get("recipients");

        SymOBOUserRSAAuth userAuth = oboAuth.getUserAuth(user.getUserId());
        SymOBOClient oboClient = SymOBOClient.initOBOClient(botConfig, userAuth);

        boolean postProcessMention = userTemplate.contains("{{mention}}");

        for (long recipient : recipients) {
            String thisMessage = userTemplate;

            Map<String, Object> postData = new HashMap<>();
            if (postProcessMention) {
                String mentionML = "<mention uid=\"" + recipient + "\" />";
                postData.put("mention", new Handlebars.SafeString(mentionML));
            }

            if (!postData.isEmpty()) {
                thisMessage = template.compileInline(userTemplate, postData);
            }

            log.info("Obtaining streamId for: {}", recipient);
            String streamId = oboClient.getStreamsClient().getUserIMStreamId(recipient);
            log.info("Sending message to stream: {}", streamId);
            oboClient.getMessagesClient().sendMessage(streamId, new OutboundMessage(thisMessage));
            log.info("Blast successful for: {}", recipient);
        }

        bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage("Blast complete!"));
    }
}
