package com.marry.system.service.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "marry.file")
public class FileStorageProperties {

    /** Local root directory for uploaded files. */
    private String path = "/var/marry/upload";

    /** Bucket/folder name within the path. */
    private String bucket = "default";

    /** Root directory for business-grouped uploads (e.g. events). */
    private String eventRoot = "events";

    /** URL prefix for serving files via the backend (Spring static resource handler). */
    private String publicBase = "/api/static/upload/";
}