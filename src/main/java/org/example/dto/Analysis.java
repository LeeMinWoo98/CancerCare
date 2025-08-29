package org.example.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Analysis {

	 private String prediction; // 예측 결과
	 private String error;      // 오류 메시지
	    
	    // 성공 응답을 만들 때 사용할 정적 메소드
	 public static Analysis createSuccess(String prediction) {
	     Analysis dto = new Analysis();
	     dto.setPrediction(prediction);
	     return dto;
	 }

	    // 실패 응답을 만들 때 사용할 정적 메소드
	 public static Analysis createError(String error) {
	     Analysis dto = new Analysis();
	     dto.setError(error);
	     return dto;
	 }	
}
