package com.travis.monolith.system.message.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SysMessageRequestTemplateContentTest {

    @Test
    void createRequestShouldLeaveTemplateTitleAndContentToBackendRendering() {
        var req = new SysMessageCreateReq();
        req.setTemplateId(1L);

        assertThat(req.isTitleValid()).isTrue();
        assertThat(req.isContentValid()).isTrue();
        assertThat(req.isTemplateContentValid()).isTrue();

        req.setTitle("客户端标题");
        req.setContent("客户端内容");

        assertThat(req.isTemplateContentValid()).isFalse();
    }

    @Test
    void updateRequestShouldLeaveTemplateTitleAndContentToBackendRendering() {
        var req = new SysMessageUpdateReq();
        req.setTemplateId(1L);

        assertThat(req.isTitleValid()).isTrue();
        assertThat(req.isContentValid()).isTrue();
        assertThat(req.isTemplateContentValid()).isTrue();

        req.setTitle("客户端标题");
        req.setContent("客户端内容");

        assertThat(req.isTemplateContentValid()).isFalse();
    }

    @Test
    void createRequestShouldRequireCustomTitleAndContentWithoutTemplate() {
        var req = new SysMessageCreateReq();

        assertThat(req.isTitleValid()).isFalse();
        assertThat(req.isContentValid()).isFalse();

        req.setTitle("自定义标题");
        req.setContent("自定义内容");

        assertThat(req.isTitleValid()).isTrue();
        assertThat(req.isContentValid()).isTrue();
    }

    @Test
    void updateRequestShouldRequireCustomTitleAndContentWithoutTemplate() {
        var req = new SysMessageUpdateReq();

        assertThat(req.isTitleValid()).isFalse();
        assertThat(req.isContentValid()).isFalse();

        req.setTitle("自定义标题");
        req.setContent("自定义内容");

        assertThat(req.isTitleValid()).isTrue();
        assertThat(req.isContentValid()).isTrue();
    }

    @Test
    void templateRequestShouldCollectParametersFromTitleContentAndRedirectUrl() {
        var req = new SysMessageTemplateCreateReq();
        req.setTitle("你好，{{name}}");
        req.setContent("<p>{{content}}</p>");
        req.setRedirectUrl("/message/{{id}}");
        req.setContentSchema(
                """
                {
                  "name": {"type": "text", "required": true},
                  "content": {"type": "text", "required": true},
                  "id": {"type": "number", "required": true}
                }
                """);

        assertThat(req.isTemplateParamUsageValid()).isTrue();

        req.setContentSchema(
                """
                {
                  "content": {"type": "text", "required": true}
                }
                """);
        assertThat(req.isTemplateParamUsageValid()).isFalse();
    }

    @Test
    void messageRequestShouldRejectDangerousJumpUrlProtocols() {
        var req = new SysMessageCreateReq();

        req.setJumpUrl("/system/message");
        assertThat(req.isJumpUrlValid()).isTrue();
        req.setJumpUrl("https://example.com/message");
        assertThat(req.isJumpUrlValid()).isTrue();
        req.setJumpUrl("javascript:alert(1)");
        assertThat(req.isJumpUrlValid()).isFalse();
        req.setJumpUrl("//example.com/message");
        assertThat(req.isJumpUrlValid()).isFalse();
        req.setJumpUrl("https://");
        assertThat(req.isJumpUrlValid()).isFalse();
        req.setJumpUrl("https://example.com\n");
        assertThat(req.isJumpUrlValid()).isFalse();
    }

    @Test
    void jumpUrlShouldOnlyBeAcceptedForOfficialAccountChannel() {
        var messageReq = new SysMessageCreateReq();
        messageReq.setJumpUrl("https://example.com/message");
        messageReq.setChannel("IN_APP");
        assertThat(messageReq.isJumpUrlChannelValid()).isFalse();
        messageReq.setChannel("SMS");
        assertThat(messageReq.isJumpUrlChannelValid()).isFalse();
        messageReq.setChannel("WECHAT_OA");
        assertThat(messageReq.isJumpUrlChannelValid()).isTrue();

        var templateReq = new SysMessageTemplateCreateReq();
        templateReq.setRedirectUrl("https://example.com/message");
        templateReq.setChannel("WECHAT_MP");
        assertThat(templateReq.isRedirectUrlChannelValid()).isTrue();
        templateReq.setChannel("WECHAT_OA");
        assertThat(templateReq.isRedirectUrlChannelValid()).isTrue();
    }
}
