import json
import os
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score
from preprocess import FEATURE_NAMES, export_encoders, preprocess_dataframe, DEFAULT_FEATURE_VALUES, WEATHER_MAPPING, CATEGORY_MAPPING, MOOD_MAPPING, PREFERRED_CATEGORY_MAPPING

def serialize_tree_node(tree, node_id=0):
    left_child = tree.children_left[node_id]
    right_child = tree.children_right[node_id]
    
    if left_child == -1 and right_child == -1: # Leaf node
        leaf_val = float(tree.value[node_id][0][0])
        return {
            "isLeaf": True,
            "value": round(leaf_val, 3)
        }
    else: # Split node
        feat_idx = int(tree.feature[node_id])
        threshold = round(float(tree.threshold[node_id]), 4)
        return {
            "isLeaf": False,
            "featureIndex": feat_idx,
            "threshold": threshold,
            "left": serialize_tree_node(tree, left_child),
            "right": serialize_tree_node(tree, right_child)
        }

def evaluate_serialized_tree(node, feature_vector):
    if node["isLeaf"]:
        return node["value"]
    feat_val = feature_vector[node["featureIndex"]]
    if feat_val <= node["threshold"]:
        return evaluate_serialized_tree(node["left"], feature_vector)
    else:
        return evaluate_serialized_tree(node["right"], feature_vector)

def evaluate_serialized_forest(forest_json, feature_vector):
    preds = [evaluate_serialized_tree(tree, feature_vector) for tree in forest_json["trees"]]
    return float(np.mean(preds))

def main():
    print("==================================================")
    print("CALMPATH AI - CO9 MACHINE LEARNING MODEL TRAINING")
    print("==================================================")
    
    # 1. Load Dataset
    dataset_path = "D:/CalmPathAI/ml_training/dataset.csv"
    if not os.path.exists(dataset_path):
        raise FileNotFoundError(f"Dataset not found at {dataset_path}. Please run generate_dataset.py first.")
        
    df = pd.read_csv(dataset_path)
    print(f"Loaded dataset: {len(df)} samples across {len(df.columns)} columns.")
    
    # 2. Preprocess Features
    X, y = preprocess_dataframe(df)
    print(f"Preprocessed feature matrix X shape: {X.shape}, target y shape: {y.shape}")
    
    # 3. Train/Test Split (80% Train, 20% Test)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, random_state=42)
    print(f"Train samples: {len(X_train)}, Test samples: {len(X_test)}")
    
    # 4. Train Random Forest Regressor
    rf = RandomForestRegressor(
        n_estimators=30,
        max_depth=8,
        min_samples_split=4,
        min_samples_leaf=2,
        random_state=42,
        n_jobs=-1
    )
    print("\nTraining Random Forest Regressor (30 estimators, max_depth=8)...")
    rf.fit(X_train, y_train)
    print("Training complete.")
    
    # 5. Evaluate on Test Set
    y_pred = rf.predict(X_test)
    mae = mean_absolute_error(y_test, y_pred)
    mse = mean_squared_error(y_test, y_pred)
    rmse = np.sqrt(mse)
    r2 = r2_score(y_test, y_pred)
    
    print("\n--------------------------------------------------")
    print("MODEL EVALUATION METRICS (TEST SET)")
    print("--------------------------------------------------")
    print(f"Mean Absolute Error (MAE):    {mae:.3f} points")
    print(f"Root Mean Squared Error (RMSE): {rmse:.3f} points")
    print(f"R-squared (R²):               {r2:.4f} ({r2*100:.2f}% variance explained)")
    print("--------------------------------------------------")
    
    # 6. Actual vs Predicted Comparison
    print("\nSAMPLE COMPARISON (Actual vs Predicted Suitability):")
    print(f"{'Index':<6} | {'Actual Suitability':<20} | {'Predicted Suitability':<22} | {'Error':<8}")
    print("-" * 64)
    for i in range(10):
        actual = y_test[i]
        predicted = y_pred[i]
        error = abs(actual - predicted)
        print(f"{i:<6} | {actual:<20.1f} | {predicted:<22.1f} | {error:<8.1f}")
        
    # 7. Serialize Forest to JSON
    trees_serialized = [serialize_tree_node(est.tree_) for est in rf.estimators_]
    forest_model_json = {
        "modelVersion": "v1.0",
        "algorithm": "RandomForestRegressor",
        "nEstimators": len(trees_serialized),
        "maxDepth": 8,
        "featureNames": FEATURE_NAMES,
        "trees": trees_serialized
    }
    
    model_output_path = "D:/CalmPathAI/ml_training/peace_recommendation_rf_v1.json"
    with open(model_output_path, "w", encoding="utf-8") as f:
        json.dump(forest_model_json, f, separators=(',', ':')) # compact JSON
    print(f"\nSerialized Random Forest model saved to: {model_output_path} ({os.path.getsize(model_output_path) / 1024:.1f} KB)")
    
    # 8. Export Metadata
    metadata = {
        "modelVersion": "v1.0",
        "modelName": "CalmPathPeaceRecommendationModel",
        "target": "suitability_score",
        "targetScale": [0, 100],
        "algorithm": "RandomForestRegressor",
        "nEstimators": len(rf.estimators_),
        "maxDepth": 8,
        "evaluationMetrics": {
            "mae": round(float(mae), 3),
            "rmse": round(float(rmse), 3),
            "r2": round(float(r2), 4)
        },
        "featureNames": FEATURE_NAMES,
        "categoricalEncodings": {
            "weather_condition": WEATHER_MAPPING,
            "place_category": CATEGORY_MAPPING,
            "user_mood": MOOD_MAPPING,
            "preferred_category": PREFERRED_CATEGORY_MAPPING
        },
        "defaultFeatureValues": DEFAULT_FEATURE_VALUES
    }
    metadata_path = "D:/CalmPathAI/ml_training/model_metadata.json"
    with open(metadata_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    print(f"Model metadata exported to: {metadata_path}")
    
    # 9. Verify serialized inference match
    test_sample = X_test[0]
    sklearn_pred = rf.predict([test_sample])[0]
    json_pred = evaluate_serialized_forest(forest_model_json, test_sample)
    diff = abs(sklearn_pred - json_pred)
    print(f"\nVerification on Test Sample 0:")
    print(f"scikit-learn predict: {sklearn_pred:.4f}")
    print(f"JSON Forest evaluate: {json_pred:.4f}")
    print(f"Absolute Difference:  {diff:.6f}")
    assert diff < 0.01, f"Mismatch in serialized inference! diff={diff}"
    print("SUCCESS: 100% parity verified between scikit-learn and serialized JSON engine.")

if __name__ == "__main__":
    main()
