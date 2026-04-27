import os
import torch
from torchvision import models, transforms
from PIL import Image
import torch.nn.functional as F

MODEL_PATH = os.path.join(os.path.dirname(__file__), "medical_model.pth")

labels = ["Autre", "IRM", "Radiographie", "Scanner"]
num_classes = len(labels)

model = models.resnet18(weights=None)
model.fc = torch.nn.Linear(model.fc.in_features, num_classes)

# Charger les poids seulement si le nombre de classes correspond
if os.path.exists(MODEL_PATH):
    try:
        model.load_state_dict(torch.load(MODEL_PATH, map_location=torch.device("cpu")))
    except RuntimeError:
        print("⚠️ Le modèle existant n'a pas le bon nombre de classes. Il sera réentraîné.")
model.eval()

transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor()
])

CONFIDENCE_THRESHOLD = 0.85

def predict_image(image_path):
    image = Image.open(image_path).convert("RGB")
    image = transform(image).unsqueeze(0)

    with torch.no_grad():
        outputs = model(image)
        probabilities = F.softmax(outputs, dim=1)

    confidence, prediction_index = torch.max(probabilities, 1)
    confidence = confidence.item()
    predicted_label = labels[prediction_index.item()]

    if confidence < CONFIDENCE_THRESHOLD or predicted_label == "Autre":
        return None, confidence

    return predicted_label, confidence
