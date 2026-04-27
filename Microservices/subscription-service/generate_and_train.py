import pandas as pd
import numpy as np
import random

# Configuration
n_samples = 700
random.seed(42)
np.random.seed(42)

def generate_data():
    data = []
    for _ in range(n_samples):
        age_enfant = random.randint(1, 18)
        mois_depuis_greffe = random.randint(0, 120)
        a_eu_episode_rejet = random.randint(0, 1)
        nombre_hospitalisations_an = random.randint(0, 8)
        prend_immunosuppresseurs = 1 if random.random() < 0.90 else 0
        nombre_medicaments_quotidiens = random.randint(1, 12)
        presence_complication_active = random.randint(0, 1)
        
        # Logique métier
        if mois_depuis_greffe < 6 or a_eu_episode_rejet == 1 or nombre_hospitalisations_an >= 3 or prend_immunosuppresseurs == 0 or presence_complication_active == 1:
            plan_id = 3 # PRO
        elif (6 <= mois_depuis_greffe <= 24) or nombre_medicaments_quotidiens >= 4 or (1 <= nombre_hospitalisations_an < 3) or age_enfant >= 12:
            plan_id = 2 # PREMIUM
        else:
            plan_id = 1 # BASIQUE
            
        # Ajout de bruit (10%)
        if random.random() < 0.10:
            plan_id = random.randint(1, 3)
            
        data.append([
            age_enfant, 
            mois_depuis_greffe, 
            a_eu_episode_rejet, 
            nombre_hospitalisations_an, 
            prend_immunosuppresseurs, 
            nombre_medicaments_quotidiens, 
            presence_complication_active, 
            plan_id
        ])
        
    return pd.DataFrame(data, columns=[
        'age_enfant', 'mois_depuis_greffe', 'a_eu_episode_rejet', 
        'nombre_hospitalisations_an', 'prend_immunosuppresseurs', 
        'nombre_medicaments_quotidiens', 'presence_complication_active', 'plan_id'
    ])

# 1. Générer
df = generate_data()

# 2. Exporter au format ARFF (pour Weka)
def save_as_arff(df, filename):
    with open(filename, 'w') as f:
        f.write("@RELATION RecommendationRelation\n\n")
        f.write("@ATTRIBUTE age_enfant NUMERIC\n")
        f.write("@ATTRIBUTE mois_depuis_greffe NUMERIC\n")
        f.write("@ATTRIBUTE a_eu_episode_rejet NUMERIC\n")
        f.write("@ATTRIBUTE nombre_hospitalisations_an NUMERIC\n")
        f.write("@ATTRIBUTE prend_immunosuppresseurs NUMERIC\n")
        f.write("@ATTRIBUTE nombre_medicaments_quotidiens NUMERIC\n")
        f.write("@ATTRIBUTE presence_complication_active NUMERIC\n")
        f.write("@ATTRIBUTE plan_id {1,2,3}\n\n")
        f.write("@DATA\n")
        for _, row in df.iterrows():
            f.write(f"{int(row['age_enfant'])},{int(row['mois_depuis_greffe'])},{int(row['a_eu_episode_rejet'])},{int(row['nombre_hospitalisations_an'])},{int(row['prend_immunosuppresseurs'])},{int(row['nombre_medicaments_quotidiens'])},{int(row['presence_complication_active'])},{int(row['plan_id'])}\n")

save_as_arff(df, 'subscription_model.arff')
print("Fichier subscription_model.arff généré avec succès.")
print("\n=== INSTRUCTIONS WEKA GUI ===")
print("1. Ouvrez Weka Explorer et chargez le fichier subscription_model.arff via 'Open file...'")
print("2. Allez dans l'onglet 'Classify'")
print("3. Cliquez sur 'Choose' -> 'trees' -> 'RandomForest'")
print("4. Assurez-vous que l'attribut cible est '(Nom) plan_id' (en dessous de Start)")
print("5. Cliquez sur 'Start'")
print("6. Dans la liste des résultats en bas à gauche, faites un clic droit sur le dernier résultat")
print("7. Choisissez 'Save model'")
print("8. Enregistrez le fichier sous 'src/main/resources/ml/subscription_model.model'")

