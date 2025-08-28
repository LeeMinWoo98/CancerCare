import os
import argparse
from ultralytics import YOLO

class MRIAnalyzer:
    def __init__(self, model_path):
        if not os.path.exists(model_path):
            raise FileNotFoundError(f"파일 찾을 수 없다{model_path}")
        
        self.model = YOLO(model_path)
        print("사진 분석기 가동")
        
    def predict(self, image_path):
        
        if not os.path.exists(image_path):
            return f"에러 : {image_path} 에서 파일을 찾을 수 없습니다."
        
        try:
            results = self.model(image_path , verbose = False)
            result = results[0]
            names = result.names
            probabilties = result.probs
            top1_class_name = names[probabilties.top1]
            
            return top1_class_name
        except Exception as e:
            return f"에러 이미지 : {e}"

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="사진 이미지 분석")
    parser.add_argument("image", type=str, help="분석할 이미지 경로")
    args = parser.parse_args()
    
    MODEL_PATH = os.path.join('analyzer', 'dataset', 'best.pt')
    IMAGE_PATH = args.image
    
    try:
       
        analyzer = MRIAnalyzer(model_path=MODEL_PATH)
        
        prediction = analyzer.predict(image_path=IMAGE_PATH)
        
        print(f"Prediction Result: {prediction}")

    except FileNotFoundError as e:
        print(e)
    except Exception as e:
        print(f"An unexpected error occurred: {e}")
    