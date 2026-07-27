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
}
