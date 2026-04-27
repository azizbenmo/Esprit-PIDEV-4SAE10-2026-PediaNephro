import random
import pandas as pd
import os

# Configuration des features et labels
# score_base = (jours_sans_connexion * 2.5) + (bilans_en_retard * 20) + (rappels_ignores * 8) + (rendez_vous_annules * 12) + (medicaments_non_confirmes * 15)

def get_risk_level(score):
    if score < 30: return "FAIBLE"
    if score < 70: return "MOYEN"
    return "ÉLEVÉ"

def generate_data(num_rows=600):
    data = []
    for _ in range(num_rows):
        # Génération de valeurs réalistes
        jours_sans_connexion = random.randint(0, 30)
        bilans_en_retard = random.randint(0, 4)
        rappels_ignores = random.randint(0, 10)
        rendez_vous_annules = random.randint(0, 3)
        medicaments_non_confirmes = random.randint(0, 5)
        
        # Calcul du score théorique
        score_base = (jours_sans_connexion * 2.5) + (bilans_en_retard * 20) + (rappels_ignores * 8) + (rendez_vous_annules * 12) + (medicaments_non_confirmes * 15)
        score_final = min(score_base, 100)
        
        # Détermination du label
        label = get_risk_level(score_final)
        
        # Ajout d'un peu de bruit (10% de chances de changer le label)
        if random.random() < 0.10:
            label = random.choice(["FAIBLE", "MOYEN", "ÉLEVÉ"])
            
        data.append({
            "jours_sans_connexion": jours_sans_connexion,
            "bilans_en_retard": bilans_en_retard,
            "rappels_ignores": rappels_ignores,
            "rendez_vous_annules": rendez_vous_annules,
            "medicaments_non_confirmes": medicaments_non_confirmes,
            "risk_level": label
        })
    return pd.DataFrame(data)

def export_to_arff(df, filename):
    with open(filename, 'w') as f:
        f.write("@RELATION RiskRelation\n\n")
        f.write("@ATTRIBUTE jours_sans_connexion NUMERIC\n")
        f.write("@ATTRIBUTE bilans_en_retard NUMERIC\n")
        f.write("@ATTRIBUTE rappels_ignores NUMERIC\n")
        f.write("@ATTRIBUTE rendez_vous_annules NUMERIC\n")
        f.write("@ATTRIBUTE medicaments_non_confirmes NUMERIC\n")
        f.write("@ATTRIBUTE risk_level {FAIBLE, MOYEN, ÉLEVÉ}\n\n")
        f.write("@DATA\n")
        for index, row in df.iterrows():
            f.write(f"{row['jours_sans_connexion']},{row['bilans_en_retard']},{row['rappels_ignores']},{row['rendez_vous_annules']},{row['medicaments_non_confirmes']},{row['risk_level']}\n")

if __name__ == "__main__":
    print("Génération des données simulées pour le risque de désengagement...")
    df = generate_data(600)
    
    output_path = "risk_data.arff"
    export_to_arff(df, output_path)
    
    print(f"Fichier ARFF généré avec succès : {os.path.abspath(output_path)}")
    print("\n--- ÉTAPES SUIVANTES DANS WEKA GUI ---")
    print("1. Ouvrez Weka Explorer.")
    print(f"2. Cliquez sur 'Open file' et sélectionnez '{output_path}'.")
    print("3. Allez dans l'onglet 'Classify'.")
    print("4. Choisissez le classifier 'trees > RandomForest'.")
    print("5. Cliquez sur 'Start' pour entraîner le modèle.")
    print("6. Faites un clic droit sur le résultat dans 'Result list' > 'Save model'.")
    print("7. Enregistrez-le sous le nom 'risk_model.model' dans 'src/main/resources/ml/'.")
