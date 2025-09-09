// 식단 추천 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 사용자 정보로 폼 프리필
    const userInfo = /*[[${userInfo}]]*/ null;
    
    if (userInfo) {
        prefillUserInfo(userInfo);
    }
    
    // 기타 입력 이벤트 리스너
    setupOtherInputs();
    
    // 폼 제출 이벤트 리스너
    document.getElementById('recommendForm').addEventListener('submit', handleFormSubmit);
});

/**
 * 사용자 정보로 폼 필드를 미리 채움
 */
function prefillUserInfo(userInfo) {
    // 암 종류 자동 선택
    if (userInfo.cancerType) {
        const cancerTypeSelect = document.getElementById('cancerType');
        const cancerValue = userInfo.cancerType.toLowerCase();
        if (cancerTypeSelect.querySelector(`option[value="${cancerValue}"]`)) {
            cancerTypeSelect.value = cancerValue;
        }
    }
    
    // 병기 자동 선택
    if (userInfo.stage) {
        document.getElementById('stage').value = userInfo.stage;
    }
    
    // 나이 자동 입력
    if (userInfo.age) {
        document.getElementById('age').value = userInfo.age;
    }
    
    // 성별 자동 선택
    if (userInfo.gender) {
        document.getElementById('sex').value = userInfo.gender;
    }
    
    // 키, 몸무게 자동 입력
    if (userInfo.height) {
        document.getElementById('height').value = userInfo.height;
    }
    if (userInfo.weight) {
        document.getElementById('weight').value = userInfo.weight;
    }
}

/**
 * 기타 입력 설정
 */
function setupOtherInputs() {
    // 알레르기 기타 처리
    const allergyOther = document.getElementById('allergyOther');
    const allergyOtherContainer = document.getElementById('allergyOtherContainer');
    
    if (allergyOther) {
        allergyOther.addEventListener('change', function() {
            if (this.checked) {
                allergyOtherContainer.style.display = 'block';
                document.getElementById('allergyOtherText').required = true;
            } else {
                allergyOtherContainer.style.display = 'none';
                document.getElementById('allergyOtherText').required = false;
                document.getElementById('allergyOtherText').value = '';
            }
        });
    }
    
    // 증상 기타 처리
    const symptomOther = document.getElementById('symptomOther');
    const symptomOtherContainer = document.getElementById('symptomOtherContainer');
    
    if (symptomOther) {
        symptomOther.addEventListener('change', function() {
            if (this.checked) {
                symptomOtherContainer.style.display = 'block';
                document.getElementById('symptomOtherText').required = true;
            } else {
                symptomOtherContainer.style.display = 'none';
                document.getElementById('symptomOtherText').required = false;
                document.getElementById('symptomOtherText').value = '';
            }
        });
    }
}

/**
 * 폼 제출 처리
 */
async function handleFormSubmit(e) {
    e.preventDefault();
    
    // 버튼 상태 변경
    const submitBtn = document.getElementById('submitBtn');
    const originalContent = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 추천 중...';
    
    // 결과 섹션 표시
    showResultSection();
    
    try {
        // 폼 데이터 수집
        const requestData = collectFormData();
        console.log('요청 데이터:', requestData);
        
        // API 호출
        const result = await callRecommendAPI(requestData);
        console.log('응답 데이터:', result);
        
        // 결과 표시
        displayResult(result);
        
    } catch (error) {
        console.error('추천 요청 실패:', error);
        alert('식단 추천 중 오류가 발생했습니다. 다시 시도해주세요.');
        hideResultSection();
    } finally {
        // 버튼 복원
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalContent;
    }
}

/**
 * 폼 데이터 수집
 */
function collectFormData() {
    const form = document.getElementById('recommendForm');
    const formData = new FormData(form);
    
    // 알레르기 수집
    const allergies = [];
    document.querySelectorAll('input[name="allergies"]:checked').forEach(cb => {
        if (cb.value === 'other') {
            const otherText = document.getElementById('allergyOtherText').value.trim();
            if (otherText) {
                allergies.push(otherText);
            }
        } else {
            allergies.push(cb.value);
        }
    });
    
    // 증상 수집
    const symptoms = [];
    document.querySelectorAll('input[name="symptoms"]:checked').forEach(cb => {
        if (cb.value === 'other') {
            const otherText = document.getElementById('symptomOtherText').value.trim();
            if (otherText) {
                symptoms.push(otherText);
            }
        } else {
            symptoms.push(cb.value);
        }
    });
    
    return {
        cancer_type: formData.get('cancer_type'),
        stage: formData.get('stage'),
        age: parseInt(formData.get('age')),
        sex: formData.get('sex'),
        height_cm: parseInt(formData.get('height_cm')),
        weight_kg: parseInt(formData.get('weight_kg')),
        allergies: allergies,
        symptoms: symptoms
    };
}

/**
 * 식단 추천 API 호출
 */
async function callRecommendAPI(requestData) {
    const response = await fetch('/food/api/recommend-json', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
            [document.querySelector('meta[name="_csrf_header"]').getAttribute('content')]: 
             document.querySelector('meta[name="_csrf"]').getAttribute('content')
        },
        body: JSON.stringify(requestData)
    });
    
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    return await response.json();
}

/**
 * 결과 섹션 표시
 */
function showResultSection() {
    const resultSection = document.getElementById('resultSection');
    const loadingDiv = document.getElementById('loadingDiv');
    const resultContent = document.getElementById('resultContent');
    
    resultSection.style.display = 'block';
    loadingDiv.style.display = 'block';
    resultContent.style.display = 'none';
    
    // 스크롤 이동
    resultSection.scrollIntoView({ behavior: 'smooth' });
}

/**
 * 결과 섹션 숨김
 */
function hideResultSection() {
    document.getElementById('resultSection').style.display = 'none';
}

/**
 * 추천 결과 표시
 */
function displayResult(data) {
    const loadingDiv = document.getElementById('loadingDiv');
    const resultContent = document.getElementById('resultContent');
    
    loadingDiv.style.display = 'none';
    resultContent.style.display = 'block';
    
    // 영양 정보 표시
    displayNutritionInfo(data);
    
    // 메뉴 정보 표시
    displayMenuInfo(data.menu || {});
    
    // 추천 이유 표시
    displayReasoningInfo(data.reasoning || []);
    
    // 주의사항 표시
    displayWarningsInfo(data.warnings || []);
}

/**
 * 영양 정보 표시
 */
function displayNutritionInfo(data) {
    document.getElementById('totalKcal').textContent = data.nutrition_estimate?.kcal_total || '-';
    document.getElementById('targetKcal').textContent = data.kcal_target || '-';
    document.getElementById('proteinEst').textContent = data.nutrition_estimate?.protein_g_est || '-';
    
    const sodiumLevel = data.nutrition_estimate?.sodium_level;
    const sodiumText = sodiumLevel === 'low' ? '저염' : 
                      sodiumLevel === 'medium' ? '보통' : 
                      sodiumLevel === 'high' ? '고염' : '-';
    document.getElementById('sodiumLevel').textContent = sodiumText;
}

/**
 * 메뉴 정보 표시
 */
function displayMenuInfo(menu) {
    // 주식
    const riceMenu = document.getElementById('riceMenu');
    if (menu.rice) {
        riceMenu.innerHTML = `
            <span class="menu-item-name">${menu.rice.name}</span>
            <span class="menu-item-kcal">${menu.rice.kcal} kcal</span>
        `;
    }
    
    // 국물
    const soupMenu = document.getElementById('soupMenu');
    if (menu.soup) {
        soupMenu.innerHTML = `
            <span class="menu-item-name">${menu.soup.name}</span>
            <span class="menu-item-kcal">${menu.soup.kcal} kcal</span>
        `;
    }
    
    // 반찬
    const sidesMenu = document.getElementById('sidesMenu');
    sidesMenu.innerHTML = '';
    if (menu.sides && Array.isArray(menu.sides)) {
        menu.sides.forEach(side => {
            const sideDiv = document.createElement('div');
            sideDiv.className = 'menu-item';
            sideDiv.innerHTML = `
                <span class="menu-item-name">${side.name}</span>
                <span class="menu-item-kcal">${side.kcal} kcal</span>
            `;
            sidesMenu.appendChild(sideDiv);
        });
    }
    
    // 간식
    const snackMenu = document.getElementById('snackMenu');
    if (menu.snack) {
        snackMenu.innerHTML = `
            <span class="menu-item-name">${menu.snack.name}</span>
            <span class="menu-item-kcal">${menu.snack.kcal} kcal</span>
        `;
    }
}

/**
 * 추천 이유 표시
 */
function displayReasoningInfo(reasoning) {
    const reasoningList = document.getElementById('reasoningList');
    reasoningList.innerHTML = '';
    
    if (reasoning && Array.isArray(reasoning)) {
        reasoning.forEach(reason => {
            const li = document.createElement('li');
            li.textContent = reason;
            reasoningList.appendChild(li);
        });
    }
}

/**
 * 주의사항 표시
 */
function displayWarningsInfo(warnings) {
    const warningsSection = document.getElementById('warningsSection');
    const warningsList = document.getElementById('warningsList');
    
    if (warnings && Array.isArray(warnings) && warnings.length > 0) {
        warningsSection.style.display = 'block';
        warningsList.innerHTML = '';
        warnings.forEach(warning => {
            const li = document.createElement('li');
            li.textContent = warning;
            warningsList.appendChild(li);
        });
    } else {
        warningsSection.style.display = 'none';
    }
}
