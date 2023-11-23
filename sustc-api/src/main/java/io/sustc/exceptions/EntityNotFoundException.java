package io.sustc.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when an attempt to access an entity
 * (e.g., find/delete by ID) that does not exist.
 */
@StandardException
public class EntityNotFoundException extends RuntimeException {
}
