package com.marry.log.enums;

/**
 * Operation business type for {@code @Log} annotation.
 */
public enum BusinessType {
    OTHER,
    CREATE,
    UPDATE,
    DELETE,
    GRANT,
    EXPORT,
    IMPORT,
    FORCE,
    GENCODE,
    CLEAN
}