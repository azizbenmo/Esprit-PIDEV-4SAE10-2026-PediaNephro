package esprit.reclamation.security.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation à placer sur les méthodes métier pour logger automatiquement l'action.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {
    /**
     * Le nom de l'action métier exécutée (ex: "LOGIN", "CREATE_RECLAMATION").
     */
    String action();
}
