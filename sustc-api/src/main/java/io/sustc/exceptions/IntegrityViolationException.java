package io.sustc.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when an attempt to insert or update data
 * results in violation of an integrity constraint,
 * such as a duplicated primary key or any business rule.
 */
@StandardException
public class IntegrityViolationException extends RuntimeException {
}
