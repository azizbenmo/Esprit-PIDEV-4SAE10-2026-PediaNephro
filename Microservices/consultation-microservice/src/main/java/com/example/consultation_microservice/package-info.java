/**
 * <h2>Scénario d’intégration Consultation ↔ User (recommandé)</h2>
 *
 * <p><b>Source de vérité</b> pour les personnes : base <em>User</em> ({@code users}, {@code doctors},
 * {@code patients}, {@code parents}). La base <em>consultation</em> ne les remplace pas : elle garde des
 * lignes locales avec une <b>PK propre</b> et une colonne {@code user_id} = {@code users.id} côté User
 * (pour un médecin, {@code doctors.id} = {@code users.id} grâce à {@code @MapsId}).
 *
 * <h3>Médecin ({@link com.example.consultation_microservice.entities.Medecin})</h3>
 * <ul>
 *   <li>Création / mise à jour après <b>acceptation</b> admin du doctor : le MS User appelle
 *       {@code POST /apiConsultation/internal/medecin/sync}.</li>
 *   <li>Refus : même endpoint avec {@code disponible=false} si une fiche existe.</li>
 *   <li>Rattrapage : {@code POST .../mic1/doctors/consultation-sync} (admin User) pour tous les
 *       doctors {@code ACCEPTED}.</li>
 * </ul>
 *
 * <h3>Patient ({@link com.example.consultation_microservice.entities.Patient})</h3>
 * <ul>
 *   <li>Pas de sync systématique à l’inscription : résolution à la <b>demande de consultation</b>
 *       (recherche par id local ou par {@code userId}) puis création locale si absent.</li>
 *   <li>L’historique peut joindre par id local ou {@code user_id}.</li>
 * </ul>
 *
 * <h3>Frontend</h3>
 * <ul>
 *   <li>Annuaire / demande : données profil depuis User ; créneaux et enregistrement RDV depuis
 *       ce MS en utilisant la fiche {@code Medecin} liée.</li>
 *   <li>Envoyer {@code medecin.userId} (id User) avec la PK locale pour que le backend résolve le bon praticien.</li>
 * </ul>
 */
package com.example.consultation_microservice;
