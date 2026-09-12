import json
import pandas as pd
import numpy as np

FEATURE_NAMES = [
    "aqi",
    "pm25",
    "pm10",
    "noise_db",
    "temperature",
    "humidity",
    "weather_condition",
    "distance_km",
    "place_category",
    "place_rating",
    "time_of_day",
    "day_of_week",
    "user_mood",
    "preferred_category",
    "max_distance",
    "user_previous_rating"
]

WEATHER_MAPPING = {
    "Clear": 0,
    "Partly Cloudy": 1,
    "Cloudy": 2,
    "Rainy": 3,
    "Thunderstorm": 4,
    "Mist/Fog": 5
}

CATEGORY_MAPPING = {
    "Parks": 0,
    "Lakes": 1,
    "Cafes": 2,
    "Libraries": 3,
    "Meditation": 4,
    "Fitness": 5
}

MOOD_MAPPING = {
    "Relax": 0,
    "Meditate": 1,
    "Study": 2,
    "Exercise": 3,
    "Fresh Air": 4,
    "Quiet Time": 5
}

PREFERRED_CATEGORY_MAPPING = {
    "All": 0,
    "Parks": 1,
    "Lakes": 2,
    "Cafes": 3,
    "Libraries": 4,
    "Meditation": 5,
    "Fitness": 6
}

DEFAULT_FEATURE_VALUES = {
    "aqi": 45.0,
    "pm25": 22.0,
    "pm10": 40.0,
    "noise_db": 42.0,
    "temperature": 25.0,
    "humidity": 55.0,
    "weather_condition": 0.0,
    "distance_km": 2.5,
    "place_category": 0.0,
    "place_rating": 4.2,
    "time_of_day": 12.0,
    "day_of_week": 3.0,
    "user_mood": 0.0,
    "preferred_category": 0.0,
    "max_distance": 10.0,
    "user_previous_rating": 0.0
}

def export_encoders(output_path="D:/CalmPathAI/ml_training/feature_encoders.json"):
    data = {
        "feature_names": FEATURE_NAMES,
        "weather_mapping": WEATHER_MAPPING,
        "category_mapping": CATEGORY_MAPPING,
        "mood_mapping": MOOD_MAPPING,
        "preferred_category_mapping": PREFERRED_CATEGORY_MAPPING,
        "default_values": DEFAULT_FEATURE_VALUES
    }
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
    print(f"Exported feature encoders to {output_path}")

def preprocess_dataframe(df: pd.DataFrame):
    df_clean = df.copy()
    
    # Missing value handling
    for feat, default_val in DEFAULT_FEATURE_VALUES.items():
        if feat in df_clean.columns:
            df_clean[feat] = df_clean[feat].fillna(default_val)
            
    # Categorical encoding
    df_clean["weather_condition"] = df_clean["weather_condition"].map(
        lambda x: WEATHER_MAPPING.get(str(x), 0)
    ).astype(float)
    
    df_clean["place_category"] = df_clean["place_category"].map(
        lambda x: CATEGORY_MAPPING.get(str(x), 0)
    ).astype(float)
    
    df_clean["user_mood"] = df_clean["user_mood"].map(
        lambda x: MOOD_MAPPING.get(str(x), 0)
    ).astype(float)
    
    df_clean["preferred_category"] = df_clean["preferred_category"].map(
        lambda x: PREFERRED_CATEGORY_MAPPING.get(str(x), 0)
    ).astype(float)
    
    X = df_clean[FEATURE_NAMES].values.astype(np.float32)
    y = df_clean["suitability_score"].values.astype(np.float32) if "suitability_score" in df_clean.columns else None
    
    return X, y

if __name__ == "__main__":
    export_encoders()
