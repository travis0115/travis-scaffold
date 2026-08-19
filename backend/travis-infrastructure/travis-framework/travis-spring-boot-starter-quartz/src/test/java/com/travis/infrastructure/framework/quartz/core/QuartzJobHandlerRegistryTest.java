package com.travis.infrastructure.framework.quartz.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class QuartzJobHandlerRegistryTest {

    @Test
    void shouldExcludeBuiltinHandlersFromConfigurableNames() {
        QuartzJobHandler configurable = handler("zConfigurable", "可配置处理器", false);
        QuartzJobHandler builtin = handler("builtin", "内置处理器", true);
        var registry = new QuartzJobHandlerRegistry(List.of(configurable, builtin));

        assertThat(registry.names()).containsExactly("zConfigurable");
        assertThat(registry.descriptors())
                .containsExactly(new QuartzJobHandlerDescriptor("zConfigurable", "可配置处理器"));
        assertThat(registry.descriptors(true))
                .containsExactly(
                        new QuartzJobHandlerDescriptor("builtin", "内置处理器"),
                        new QuartzJobHandlerDescriptor("zConfigurable", "可配置处理器"));
        assertThat(registry.isBuiltin("builtin")).isTrue();
        assertThat(registry.contains("builtin")).isTrue();
    }

    @Test
    void shouldResolveHandlersAfterDependentSingletonsAreCreated() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(
                    QuartzJobHandlerRegistry.class,
                    () ->
                            new QuartzJobHandlerRegistry(
                                    context.getBeanProvider(QuartzJobHandler.class)));
            context.registerBean(RegistryDependentService.class);
            context.registerBean(RegistryDependentHandler.class);

            context.refresh();

            assertThat(context.getBean(QuartzJobHandlerRegistry.class).contains("dependent"))
                    .isTrue();
        }
    }

    private QuartzJobHandler handler(String name, String description, boolean builtin) {
        return new QuartzJobHandler() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public boolean isBuiltin() {
                return builtin;
            }

            @Override
            public void execute(String params) {}
        };
    }

    static class RegistryDependentService {
        RegistryDependentService(QuartzJobHandlerRegistry registry) {}
    }

    static class RegistryDependentHandler implements QuartzJobHandler {
        RegistryDependentHandler(RegistryDependentService service) {}

        @Override
        public String getName() {
            return "dependent";
        }

        @Override
        public void execute(String params) {}
    }
}
