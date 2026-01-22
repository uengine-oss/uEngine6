package org.uengine.processmanager;

/**
 * Local abstraction for definition access (filesystem or remote gateway).
 *
 * This is analogous to {@link InstanceServiceLocal} used by
 * {@code TimerEventJob}:
 * uengine-core depends only on this small interface, and an app
 * (process-service)
 * provides the concrete implementation as a Spring bean.
 */
public interface DefinitionServiceLocal {

    /**
     * Returns raw definition content for a given path.
     *
     * Examples:
     * - "businessRules/<id>.rule"
     * - "map.json"
     */
    Object getRawDefinition(String defPath) throws Exception;

    /**
     * Lists definitions in a directory, returning a JSON string (HAL-like)
     * compatible with callers
     * that currently parse {@code _embedded.definitions[*].name/path}.
     */
    String listDefinitionRaw(String basePath) throws Exception;

    /**
     * Saves raw definition content.
     *
     * If {@code defPath} has no extension, implementations may treat it as a
     * directory creation request.
     */
    Object putRawDefinition(String defPath, String definition) throws Exception;
}
