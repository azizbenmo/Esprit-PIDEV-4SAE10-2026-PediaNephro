package esprit.reclamation.entities;

/**
 * Priorité SLA / escalade, distincte de {@link Priorite} (échelle legacy).
 */
public enum PrioriteReclamation {
    BASSE,
    NORMALE,
    HAUTE,
    CRITIQUE
}
