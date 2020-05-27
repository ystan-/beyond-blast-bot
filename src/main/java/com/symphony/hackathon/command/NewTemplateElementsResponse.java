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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NewTemplateElementsResponse implements ElementsResponse {
    private final SymBotClient bot;
    private final SymOBORSAAuth oboAuth;
    private final SymConfig botConfig;
    private final TemplatesService template;

    public NewTemplateElementsResponse(SymBotClient bot,
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
        Map<String, Object> formValues = action.getFormValues();
        String title = formValues.get("title").toString();
        String userTemplate = formValues.get("template").toString();
        List<Long> recipients = (List<Long>) formValues.get("recipients");

        SymOBOUserRSAAuth userAuth = oboAuth.getUserAuth(user.getUserId());
        SymOBOClient oboClient = SymOBOClient.initOBOClient(botConfig, userAuth);

        Map<String, String> data = Map.of("title", title, "body", userTemplate);
        String message = template.compile("blast-template", data);

        boolean postProcess = message.contains("{{mention}}");

        for (long recipient : recipients) {
            String thisMessage = message;
            if (postProcess) {
                Map<String, Object> postData = Map.of(
                    "mention", new Handlebars.SafeString("<mention uid=\""+recipient+"\" />")
                );
                thisMessage = template.compileInline(message, postData);
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
