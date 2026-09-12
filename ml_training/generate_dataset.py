import csv
import random
import numpy as np

# Set seed for reproducible dataset generation
random.seed(42)
np.random.seed(42)

WEATHER_CONDITIONS = ["Clear", "Partly Cloudy", "Cloudy", "Rainy", "Thunderstorm", "Mist/Fog"]
PLACE_CATEGORIES = ["Parks", "Lakes", "Cafes", "Libraries", "Meditation", "Fitness"]
USER_MOODS = ["Relax", "Meditate", "Study", "Exercise", "Fresh Air", "Quiet Time"]
PREFERRED_CATEGORIES = ["All", "Parks", "Lakes", "Cafes", "Libraries", "Meditation", "Fitness"]

NUM_SAMPLES = 2000

def generate_sample():
    # Place category
    category = random.choice(PLACE_CATEGORIES)
    
    # User profile & mood
    mood = random.choice(USER_MOODS)
    preferred_category = random.choice(PREFERRED_CATEGORIES)
    
    # Environmental telemetry (realistic Indian ranges)
    # Good air: 20-50, Moderate: 51-100, Poor: 101-220
    is_clean_location = (category in ["Parks", "Lakes", "Meditation"]) and random.random() < 0.7
    if is_clean_location:
        aqi = int(random.uniform(20, 65))
    else:
        aqi = int(random.uniform(50, 180))
        
    pm25 = round(aqi * random.uniform(0.4, 0.65), 1)
    pm10 = round(pm25 * random.uniform(1.6, 2.2), 1)
    
    # Ambient noise
    if category in ["Meditation", "Libraries"]:
        noise_db = int(random.uniform(28, 44))
    elif category in ["Parks", "Lakes"]:
        noise_db = int(random.uniform(34, 52))
    elif category == "Cafes":
        noise_db = int(random.uniform(42, 60))
    else: # Fitness
        noise_db = int(random.uniform(45, 68))
        
    # Weather & Temperature
    weather = random.choice(WEATHER_CONDITIONS)
    temperature = round(random.uniform(18.0, 36.0), 1)
    humidity = int(random.uniform(35, 85))
    
    # Distance and place properties
    distance_km = round(random.expovariate(1.0 / 3.5) + 0.3, 1)
    distance_km = min(distance_km, 25.0)
    max_distance = float(random.choice([5, 8, 10, 15, 20]))
    place_rating = round(random.uniform(3.5, 5.0), 1)
    
    # Time context
    time_of_day = random.randint(6, 22) # 6 AM to 10 PM
    day_of_week = random.randint(1, 7)  # 1 = Mon, 7 = Sun
    
    # Previous user interaction rating (0.0 means unvisited/unrated)
    has_history = random.random() < 0.4
    user_previous_rating = round(random.uniform(3.0, 5.0), 1) if has_history else 0.0

    # Calculate Ground Truth Suitability Score (0 - 100) using nonlinear domain equations
    base_score = 75.0
    
    # 1. AQI Impact
    if aqi <= 50:
        base_score += (50 - aqi) * 0.35
    elif aqi <= 100:
        base_score -= (aqi - 50) * 0.25
    else:
        base_score -= 12.5 + (aqi - 100) * 0.35
        
    # 2. Noise Impact (Mood-dependent sensitivity)
    noise_tolerance = 48
    if mood == "Meditate":
        noise_tolerance = 36
    elif mood == "Study":
        noise_tolerance = 40
    elif mood == "Relax":
        noise_tolerance = 44
    elif mood == "Exercise":
        noise_tolerance = 58
        
    if noise_db <= noise_tolerance:
        base_score += (noise_tolerance - noise_db) * 0.6
    else:
        base_score -= (noise_db - noise_tolerance) * 1.1
        
    # 3. Category & Mood Alignment
    mood_category_synergy = 0.0
    if mood == "Relax" and category in ["Parks", "Lakes"]:
        mood_category_synergy = 12.0
    elif mood == "Meditate" and category in ["Meditation", "Libraries"]:
        mood_category_synergy = 15.0
    elif mood == "Study" and category in ["Libraries", "Cafes"]:
        mood_category_synergy = 14.0
    elif mood == "Exercise" and category in ["Fitness", "Parks"]:
        mood_category_synergy = 12.0
    elif mood == "Fresh Air" and category in ["Parks", "Lakes"]:
        mood_category_synergy = 14.0
    elif mood == "Quiet Time" and category in ["Cafes", "Libraries", "Lakes"]:
        mood_category_synergy = 10.0
    else:
        mood_category_synergy = -4.0
    base_score += mood_category_synergy
    
    # 4. Preferred Category Alignment
    if preferred_category != "All":
        if category == preferred_category:
            base_score += 8.0
        else:
            base_score -= 4.0
            
    # 5. Distance and Accessibility
    if distance_km <= 2.0:
        base_score += 8.0
    elif distance_km <= max_distance:
        base_score += (max_distance - distance_km) / max_distance * 5.0
    else:
        base_score -= (distance_km - max_distance) * 2.2
        
    # 6. Thermal comfort & Weather
    if 20.0 <= temperature <= 27.0:
        base_score += 5.0
    elif temperature > 33.0 or temperature < 15.0:
        base_score -= 7.0
        
    if weather in ["Rainy", "Thunderstorm"]:
        if category in ["Parks", "Lakes", "Fitness"]: # Outdoor penalties
            base_score -= 22.0
        else: # Indoor cozy boost
            base_score += 4.0
    elif weather == "Clear":
        if category in ["Parks", "Lakes"]:
            base_score += 6.0
            
    # 7. Rating and History
    base_score += (place_rating - 4.0) * 8.0
    if user_previous_rating > 0.0:
        base_score += (user_previous_rating - 3.5) * 6.0

    # Slight stochastic noise (-2.5 to +2.5) to reflect human variability
    base_score += random.gauss(0, 1.8)
    
    final_score = int(round(max(5.0, min(99.0, base_score))))
    
    return {
        "aqi": aqi,
        "pm25": pm25,
        "pm10": pm10,
        "noise_db": noise_db,
        "temperature": temperature,
        "humidity": humidity,
        "weather_condition": weather,
        "distance_km": distance_km,
        "place_category": category,
        "place_rating": place_rating,
        "time_of_day": time_of_day,
        "day_of_week": day_of_week,
        "user_mood": mood,
        "preferred_category": preferred_category,
        "max_distance": max_distance,
        "user_previous_rating": user_previous_rating,
        "suitability_score": final_score
    }

def main():
    fieldnames = [
        "aqi", "pm25", "pm10", "noise_db", "temperature", "humidity",
        "weather_condition", "distance_km", "place_category", "place_rating",
        "time_of_day", "day_of_week", "user_mood", "preferred_category",
        "max_distance", "user_previous_rating", "suitability_score"
    ]
    
    dataset_path = "D:/CalmPathAI/ml_training/dataset.csv"
    with open(dataset_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for _ in range(NUM_SAMPLES):
            writer.writerow(generate_sample())
            
    print(f"Successfully generated {NUM_SAMPLES} training samples in {dataset_path}")

if __name__ == "__main__":
    main()
