package org.mifos.loanrisk.document.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a handler class with the single event-type it is responsible for.
 *
 * <pre>
 * @Handles(DocumentEventType.CREATED)
 * public class DocumentCreatedHandler implements DocumentMessageHandler { … }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handles {

    /** The enum constant this handler processes. */
    DocumentEventType value();
}
