package org.mifos.loanrisk.loan.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a handler class with the single event-type it is responsible for.
 *
 * <pre>
 * @Handles(LoanEventType.CREATED)
 * public class LoanCreatedHandler implements LoanMessageHandler { … }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Handles {

    /** The enum constant this handler processes. */
    LoanEventType value();
}
