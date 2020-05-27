package com.symphony.hackathon.command;

import authentication.SymOBORSAAuth;
import authentication.SymOBOUserRSAAuth;
import clients.SymBotClient;
import clients.SymOBOClient;
import com.symphony.hackathon.service.TemplatesService;
import configuration.SymConfig;
import model.OutboundMessage;
import model.User;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

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
        String preMessage = template.compile("blast-template", data);

        for (long recipient : recipients) {
            String streamId = oboClient.getStreamsClient().getUserIMStreamId(recipient);
            oboClient.getMessagesClient().sendMessage(streamId, new OutboundMessage(preMessage));
        }

        bot.getMessagesClient().sendMessage(action.getStreamId(), new OutboundMessage("Blast complete!"));
    }
}
