package com.marry.web.config;

import com.marry.system.service.props.FileStorageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Maps the configured {@code marry.file.public-base} URL prefix onto the local
 * upload directory ({@code marry.file.path}), so urls persisted on
 * {@code sys_file.url} always resolve to real files regardless of either
 * setting being overridden in yml.
 *
 * <p>Resource-handler patterns are servlet-relative: when a context-path is
 * configured (e.g. {@code /api}), the servlet receives requests with that
 * prefix already stripped. The derived pattern must therefore exclude it, or
 * the handler never matches and the request falls through to the default
 * classpath resource handler (500 "No static resource").</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final FileStorageProperties props;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public WebMvcConfig(FileStorageProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(staticPattern())
                .addResourceLocations("file:" + props.getPath() + "/");
    }

    /**
     * Derives the resource-handler pattern from {@code public-base}. Supports
     * either a path like {@code /api/static/upload/} or a full URL like
     * {@code https://cdn.example.com/uploads/} (scheme+host are stripped, only
     * the path portion is used for the local handler). The configured
     * context-path is then removed so the pattern matches the servlet-relative
     * path the DispatcherServlet actually receives.
     */
    private String staticPattern() {
        String base = props.getPublicBase();
        if (base.contains("://")) {
            int schemeEnd = base.indexOf("://") + 3;
            int slash = base.indexOf('/', schemeEnd);
            base = slash < 0 ? "/" : base.substring(slash);
        }
        if (!base.startsWith("/")) base = "/" + base;
        if (!base.endsWith("/")) base = base + "/";
        if (contextPath != null && contextPath.length() > 1 && base.startsWith(contextPath + "/")) {
            base = base.substring(contextPath.length());
            if (!base.startsWith("/")) base = "/" + base;
        }
        return base + "**";
    }
}
