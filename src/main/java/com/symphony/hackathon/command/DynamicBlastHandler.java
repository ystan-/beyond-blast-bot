package com.symphony.hackathon.command;

import authentication.SymOBORSAAuth;
import authentication.SymOBOUserRSAAuth;
import clients.SymBotClient;
import clients.SymOBOClient;
import com.github.jknack.handlebars.Handlebars;
import com.symphony.hackathon.model.DistributionList;
import com.symphony.hackathon.model.HashLink;
import com.symphony.hackathon.repository.DistributionListRepository;
import com.symphony.hackathon.repository.HashLinkRepository;
import com.symphony.hackathon.service.TemplatesService;
import configuration.SymConfig;
import lombok.extern.slf4j.Slf4j;
import model.OutboundMessage;
import model.User;
import model.UserInfo;
import model.events.SymphonyElementsAction;
import org.springframework.stereotype.Service;
import javax.ws.rs.core.NoContentException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DynamicBlastHandler implements ElementsResponse {
    private String LINK_PREFIX = "http://localhost:8080/r/";
    private final SymBotClient bot;
    private final SymConfig botConfig;
    private final TemplatesService templatesService;
    private final DistributionListRepository distributionListRepository;
    private final HashLinkRepository hashLinkRepository;
    public static final String HYPERLINK = "<a href=\"%s\">%s</a>";
    public static final String LINK_REGEX = "\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";

    public DynamicBlastHandler(SymBotClient bot,
                               SymConfig botConfig,
                               TemplatesService templatesService,
                               DistributionListRepository distributionListRepository,
                               HashLinkRepository hashLinkRepository) {
        this.bot = bot;
        this.botConfig = botConfig;
        this.templatesService = templatesService;
        this.distributionListRepository = distributionListRepository;
        this.hashLinkRepository = hashLinkRepository;
    }

    @SuppressWarnings("unchecked")
    public void execute(User user, SymphonyElementsAction elementsAction) {
        String action = elementsAction.getFormValues().get("action").toString();
        Map<String, Object> formValues = elementsAction.getFormValues();

        if (action.equals("home")) {
            this.bot.getMessagesClient().sendMessage(
                elementsAction.getStreamId(),
                new OutboundMessage(templatesService.load("menu"))
            );
        } else if (action.equals("preview-blast")) {
            if (formValues.get("template").toString().trim().isEmpty()) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: No message to send")
                );
                return;
            }

            String userTemplate = formValues.get("template").toString()
                .replaceAll("\n", "<br/>");

            Matcher m = Pattern.compile(LINK_REGEX).matcher(userTemplate);
            while (m.find()) {
                String url = m.group(1);

                if (formValues.containsKey("track-urls")) {
                    String uuid = UUID.randomUUID().toString();
                    hashLinkRepository.save(HashLink.builder().id(uuid).url(url).build());
                    String newUrl = String.format(HYPERLINK, LINK_PREFIX + uuid, url);
                    userTemplate = userTemplate.replace(url, newUrl);
                } else {
                    String newUrl = String.format(HYPERLINK, url, url);
                    userTemplate = userTemplate.replace(url, newUrl);
                }
            }

            List<UserInfo> recipients = new ArrayList<>();
            if (!formValues.get("distributionList").toString().equals("none")) {
                long distributionListId = Long.parseLong(formValues.get("distributionList").toString());
                DistributionList distributionList = distributionListRepository.findById(distributionListId).orElse(null);
                if (distributionList != null) {
                    recipients.addAll(distributionList.getUsers());
                }
            }
            List<Long> additionalRecipientIds = (List<Long>) formValues.get("recipients");
            try {
                recipients.addAll(bot.getUsersClient().getUsersFromIdList(additionalRecipientIds, false));
            } catch (NoContentException ignore) {}

            if (recipients.isEmpty()) {
                this.bot.getMessagesClient().sendMessage(
                    elementsAction.getStreamId(),
                    new OutboundMessage("Error: No recipients to send to")
                );
                return;
            }

            if (userTemplate.contains("{{mention}}")) {
                String mentionML = "<mention uid=\"" + recipients.get(0).getId() + "\" />";
                userTemplate = templatesService.compileInline(
                    userTemplate,
                    Map.of("mention", new Handlebars.SafeString(mentionML))
                );
            }

            String recipientIds = recipients.stream().map(u -> u.getId().toString())
                .collect(Collectors.joining(","));

            Map<String, Object> data = Map.of(
                "message", new Handlebars.SafeString(userTemplate),
                "recipients", recipients,
                "recipientsCount", recipients.size(),
                "recipientIds", recipientIds,
                "template", formValues.get("template").toString()
            );
            String message = templatesService.compile("preview-blast", data);
            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage(message));
        } else {
            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage("Sending blast.."));

            String userTemplate = formValues.get("template").toString();
            List<Long> recipients = Arrays.stream(formValues.get("recipientIds").toString()
                .split(",")).map(Long::parseLong).collect(Collectors.toList());

            SymOBORSAAuth oboAuth = new SymOBORSAAuth(botConfig);
            oboAuth.authenticate();
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
                    thisMessage = templatesService.compileInline(userTemplate, postData);
                }

                log.info("Obtaining streamId for: {}", recipient);
                String streamId = oboClient.getStreamsClient().getUserIMStreamId(recipient);
                log.info("Sending message to stream: {}", streamId);
                oboClient.getMessagesClient().sendMessage(streamId, new OutboundMessage(thisMessage));
                log.info("Blast successful for: {}", recipient);
            }

            bot.getMessagesClient().sendMessage(elementsAction.getStreamId(), new OutboundMessage("Blast complete!"));
        }
    }
}
