package com.dobbinsoft.gus.remotesales.client.wecom.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class WxCpMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -2082278303476631708L;
    private static final String CONTENT_CONST = "content";
    private static final String TITLE_CONST = "title";
    private static final String DESCRIPTION_CONST = "description";
    private static final String MEDIA_ID_CONST = "media_id";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String toUser;
    private String toParty;
    private String toTag;
    private Integer agentId;
    private String msgType;
    private String content;
    private String mediaId;
    private String thumbMediaId;
    private String title;
    private String description;
    private String musicUrl;
    private String hqMusicUrl;
    private String safe;
    private String url;
    private String btnTxt;
    private List<NewArticle> articles = new ArrayList<>();
    private List<MpnewsArticle> mpnewsArticles = new ArrayList<>();
    private String appId;
    private String page;
    private Boolean emphasisFirstItem;
    private Map<String, String> contentItems;
    private String taskId;
    private List<TaskCardButton> taskButtons = new ArrayList<>();

    public String toJson() {
        ObjectNode messageJson = objectMapper.createObjectNode();
        if (this.getAgentId() != null) {
            messageJson.put("agentid", this.getAgentId());
        }

        if (StringUtils.isNotBlank(this.getToUser())) {
            messageJson.put("touser", this.getToUser());
        }

        messageJson.put("msgtype", this.getMsgType());
        if (StringUtils.isNotBlank(this.getToParty())) {
            messageJson.put("toparty", this.getToParty());
        }

        if (StringUtils.isNotBlank(this.getToTag())) {
            messageJson.put("totag", this.getToTag());
        }

        this.handleMsgType(messageJson);
        if (StringUtils.isNotBlank(this.getSafe())) {
            messageJson.put("safe", this.getSafe());
        }

        try {
            return objectMapper.writeValueAsString(messageJson);
        } catch (Exception e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }

    private void handleMsgType(ObjectNode messageJson) {
        switch (this.getMsgType()) {
            case "text":
            case "markdown":
                handleTextOrMarkdown(messageJson);
                break;
            case "textcard":
                handleTextCard(messageJson);
                break;
            case "image":
            case "file":
                handleImageOrFile(messageJson);
                break;
            case "voice":
                handleVoice(messageJson);
                break;
            case "video":
                handleVideo(messageJson);
                break;
            case "news":
                handleNews(messageJson);
                break;
            case "mpnews":
                handleMpNews(messageJson);
                break;
            case "taskcard":
                handleTaskCard(messageJson);
                break;
            case "miniprogram_notice":
                handleMiniProgramNotice(messageJson);
                break;
            default:
                log.error("unknown msgType");
        }
    }

    private void handleTextOrMarkdown(ObjectNode messageJson) {
        ObjectNode text = objectMapper.createObjectNode();
        text.put(CONTENT_CONST, this.getContent());
        messageJson.set(this.getMsgType(), text);
    }

    private void handleTextCard(ObjectNode messageJson) {
        ObjectNode text = objectMapper.createObjectNode();
        text.put(TITLE_CONST, this.getTitle());
        text.put(DESCRIPTION_CONST, this.getDescription());
        text.put("url", this.getUrl());
        text.put("btntxt", this.getBtnTxt());
        messageJson.set("textcard", text);
    }

    private void handleImageOrFile(ObjectNode messageJson) {
        ObjectNode image = objectMapper.createObjectNode();
        image.put(MEDIA_ID_CONST, this.getMediaId());
        messageJson.set(this.getMsgType(), image);
    }

    private void handleVoice(ObjectNode messageJson) {
        ObjectNode voice = objectMapper.createObjectNode();
        voice.put(MEDIA_ID_CONST, this.getMediaId());
        messageJson.set("voice", voice);
    }

    private void handleVideo(ObjectNode messageJson) {
        ObjectNode video = objectMapper.createObjectNode();
        video.put(MEDIA_ID_CONST, this.getMediaId());
        video.put("thumb_media_id", this.getThumbMediaId());
        video.put(TITLE_CONST, this.getTitle());
        video.put(DESCRIPTION_CONST, this.getDescription());
        messageJson.set("video", video);
    }

    private void handleNews(ObjectNode messageJson) {
        ObjectNode newsJsonObject = objectMapper.createObjectNode();
        ArrayNode articleJsonArray = objectMapper.createArrayNode();

        for (NewArticle article : this.getArticles()) {
            ObjectNode articleJson = objectMapper.createObjectNode();
            articleJson.put(TITLE_CONST, article.getTitle());
            articleJson.put(DESCRIPTION_CONST, article.getDescription());
            articleJson.put("url", article.getUrl());
            articleJson.put("picurl", article.getPicUrl());
            articleJsonArray.add(articleJson);
        }

        newsJsonObject.set("articles", articleJsonArray);
        messageJson.set("news", newsJsonObject);
    }

    private void handleMpNews(ObjectNode messageJson) {
        ObjectNode newsJsonObject = objectMapper.createObjectNode();
        if (this.getMediaId() != null) {
            newsJsonObject.put(MEDIA_ID_CONST, this.getMediaId());
        } else {
            ArrayNode articleJsonArray = objectMapper.createArrayNode();
            for (MpnewsArticle article : this.getMpnewsArticles()) {
                ObjectNode articleJson = objectMapper.createObjectNode();
                articleJson.put(TITLE_CONST, article.getTitle());
                articleJson.put("thumb_media_id", article.getThumbMediaId());
                articleJson.put("author", article.getAuthor());
                articleJson.put("content_source_url", article.getContentSourceUrl());
                articleJson.put(CONTENT_CONST, article.getContent());
                articleJson.put("digest", article.getDigest());
                articleJson.put("show_cover_pic", article.getShowCoverPic());
                articleJsonArray.add(articleJson);
            }
            newsJsonObject.set("articles", articleJsonArray);
        }
        messageJson.set("mpnews", newsJsonObject);
    }

    private void handleTaskCard(ObjectNode messageJson) {
        ObjectNode text = objectMapper.createObjectNode();
        text.put(TITLE_CONST, this.getTitle());
        text.put(DESCRIPTION_CONST, this.getDescription());
        if (StringUtils.isNotBlank(this.getUrl())) {
            text.put("url", this.getUrl());
        }
        text.put("task_id", this.getTaskId());
        
        ArrayNode buttonJsonArray = objectMapper.createArrayNode();
        for (TaskCardButton button : this.getTaskButtons()) {
            ObjectNode buttonJson = objectMapper.createObjectNode();
            buttonJson.put("key", button.getKey());
            buttonJson.put("name", button.getName());
            if (StringUtils.isNotBlank(button.getReplaceName())) {
                buttonJson.put("replace_name", button.getReplaceName());
            }
            if (StringUtils.isNotBlank(button.getColor())) {
                buttonJson.put("color", button.getColor());
            }
            if (button.getBold() != null) {
                buttonJson.put("is_bold", button.getBold());
            }
            buttonJsonArray.add(buttonJson);
        }
        text.set("btn", buttonJsonArray);
        messageJson.set("taskcard", text);
    }

    private void handleMiniProgramNotice(ObjectNode messageJson) {
        ObjectNode notice = objectMapper.createObjectNode();
        notice.put("appid", this.getAppId());
        notice.put("page", this.getPage());
        notice.put(DESCRIPTION_CONST, this.getDescription());
        notice.put(TITLE_CONST, this.getTitle());
        notice.put("emphasis_first_item", this.getEmphasisFirstItem());
        
        ArrayNode content = objectMapper.createArrayNode();
        for (Map.Entry<String, String> item : this.getContentItems().entrySet()) {
            ObjectNode articleJson = objectMapper.createObjectNode();
            articleJson.put("key", item.getKey());
            articleJson.put("value", item.getValue());
            content.add(articleJson);
        }
        notice.set("content_item", content);
        messageJson.set("miniprogram_notice", notice);
    }
}
