package com.travis.infrastructure.framework.web.core.xss;

import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;
import tools.jackson.databind.module.SimpleModule;

/** 为 {@link SanitizeHtml} 字段绑定 HTML 清洗反序列化器。 */
public class SanitizeHtmlJacksonModule extends SimpleModule {

    private final HtmlSanitizer htmlSanitizer;

    public SanitizeHtmlJacksonModule(HtmlSanitizer htmlSanitizer) {
        super("SanitizeHtmlModule");
        this.htmlSanitizer = htmlSanitizer;
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new SanitizeHtmlAnnotationIntrospector(htmlSanitizer));
    }

    private static class SanitizeHtmlAnnotationIntrospector extends NopAnnotationIntrospector {

        private final HtmlSanitizer htmlSanitizer;

        private SanitizeHtmlAnnotationIntrospector(HtmlSanitizer htmlSanitizer) {
            this.htmlSanitizer = htmlSanitizer;
        }

        @Override
        public Object findDeserializer(MapperConfig<?> config, Annotated annotated) {
            return annotated.hasAnnotation(SanitizeHtml.class)
                    ? new SanitizeHtmlDeserializer(htmlSanitizer)
                    : null;
        }
    }
}
