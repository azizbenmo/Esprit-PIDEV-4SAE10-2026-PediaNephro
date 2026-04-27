import pandas as pd
from chronos import Chronos2Pipeline

pipeline = Chronos2Pipeline.from_pretrained(
    "amazon/chronos-2",
    device_map="cpu"
)

def predict_next_value(values):

    if len(values) < 3:
        raise ValueError("Minimum 3 valeurs nécessaires")

    df = pd.DataFrame({
        "id": ["patient_1"] * len(values),
        "timestamp": pd.date_range("2026-01-01", periods=len(values), freq="h"),
        "target": values
    })

    prediction = pipeline.predict_df(
        df,
        prediction_length=1,
        quantile_levels=[0.5],
        id_column="id",
        timestamp_column="timestamp",
        target="target"
    )

    next_value = prediction["0.5"].iloc[-1]

    return float(next_value)
