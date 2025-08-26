// 전역 변수
let map;
let markers = [];
let currentPosition = null;
let hospitals = [];

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    initializeMap();
    setupEventListeners();
    loadHospitals();
});

// 지도 초기화
function initializeMap() {
    const mapContainer = document.getElementById('map');
    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780), // 서울시청
        level: 8
    };

    map = new kakao.maps.Map(mapContainer, mapOption);

    // 지도 컨트롤 이벤트
    document.getElementById('zoomIn').addEventListener('click', function() {
        map.setLevel(map.getLevel() - 1);
    });

    document.getElementById('zoomOut').addEventListener('click', function() {
        map.setLevel(map.getLevel() + 1);
    });

    document.getElementById('resetMap').addEventListener('click', function() {
        if (currentPosition) {
            map.setCenter(currentPosition);
            map.setLevel(6);
        } else {
            map.setCenter(new kakao.maps.LatLng(37.5665, 126.9780));
            map.setLevel(8);
        }
    });
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 현재 위치 버튼
    document.getElementById('currentLocationBtn').addEventListener('click', getCurrentLocation);

    // 검색 버튼
    document.getElementById('searchBtn').addEventListener('click', searchByAddress);

    // 필터 변경
    document.getElementById('specialtyFilter').addEventListener('change', filterHospitals);
    document.getElementById('distanceFilter').addEventListener('change', filterHospitals);

    // AI 추천 버튼
    document.getElementById('aiRecommendBtn').addEventListener('click', getAIRecommendation);

    // 주소 입력 엔터키
    document.getElementById('locationInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchByAddress();
        }
    });

    // 모달 닫기
    document.querySelector('.close').addEventListener('click', closeModal);
    window.addEventListener('click', function(e) {
        if (e.target === document.getElementById('hospitalModal')) {
            closeModal();
        }
    });
}

// 현재 위치 가져오기
function getCurrentLocation() {
    showLoading();

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;

                currentPosition = new kakao.maps.LatLng(lat, lng);
                map.setCenter(currentPosition);
                map.setLevel(6);

                // 현재 위치 마커 추가
                addCurrentLocationMarker(lat, lng);

                // 주변 병원 검색
                searchNearbyHospitals(lat, lng);

                hideLoading();
            },
            function(error) {
                console.error('위치 정보를 가져올 수 없습니다:', error);
                alert('위치 정보를 가져올 수 없습니다. 주소를 직접 입력해주세요.');
                hideLoading();
            }
        );
    } else {
        alert('이 브라우저에서는 위치 정보를 지원하지 않습니다.');
        hideLoading();
    }
}

// 현재 위치 마커 추가
function addCurrentLocationMarker(lat, lng) {
    // 기존 현재 위치 마커 제거
    markers.forEach(marker => {
        if (marker.isCurrentLocation) {
            marker.setMap(null);
        }
    });

    const marker = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(lat, lng),
        map: map
    });

    // 커스텀 마커 스타일
    const markerContent = `
        <div class="custom-marker">
            <i class="fas fa-location-arrow"></i> 현재 위치
        </div>
    `;

    const customOverlay = new kakao.maps.CustomOverlay({
        content: markerContent,
        position: new kakao.maps.LatLng(lat, lng),
        xAnchor: 0.5,
        yAnchor: 1
    });

    customOverlay.setMap(map);
    marker.isCurrentLocation = true;
    markers.push(marker);
}

// 주소로 검색
function searchByAddress() {
    const address = document.getElementById('locationInput').value.trim();

    if (!address) {
        alert('주소를 입력해주세요.');
        return;
    }

    showLoading();

    const geocoder = new kakao.maps.services.Geocoder();
    geocoder.addressSearch(address, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
            const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
            currentPosition = coords;

            map.setCenter(coords);
            map.setLevel(6);

            addCurrentLocationMarker(result[0].y, result[0].x);
            searchNearbyHospitals(result[0].y, result[0].x);
        } else {
            alert('주소를 찾을 수 없습니다. 다시 입력해주세요.');
        }
        hideLoading();
    });
}

// 주변 병원 검색
function searchNearbyHospitals(lat, lng) {
    const specialty = document.getElementById('specialtyFilter').value;

    fetch(`/hospital/api/nearby?lat=${lat}&lng=${lng}&specialty=${specialty}`)
        .then(response => response.json())
        .then(data => {
            hospitals = data;
            displayHospitals(data);
            addHospitalMarkers(data);
        })
        .catch(error => {
            console.error('병원 정보를 가져오는 중 오류가 발생했습니다:', error);
            alert('병원 정보를 가져오는 중 오류가 발생했습니다.');
        });
}

// 병원 목록 표시
function displayHospitals(hospitalList) {
    const container = document.getElementById('hospitalList');
    container.innerHTML = '';

    if (hospitalList.length === 0) {
        container.innerHTML = '<div class="hospital-item"><p>주변에 병원이 없습니다.</p></div>';
        return;
    }

    hospitalList.forEach(hospital => {
        const hospitalElement = createHospitalElement(hospital);
        container.appendChild(hospitalElement);
    });
}

// 병원 요소 생성
function createHospitalElement(hospital) {
    const div = document.createElement('div');
    div.className = 'hospital-item';
    div.onclick = () => showHospitalDetail(hospital);

    const stars = '★'.repeat(Math.floor(parseFloat(hospital.rating))) +
        '☆'.repeat(5 - Math.floor(parseFloat(hospital.rating)));

    div.innerHTML = `
        <div class="hospital-name">
            <i class="fas fa-hospital"></i>
            ${hospital.name}
        </div>
        <div class="hospital-specialty">${hospital.specialty}</div>
        <div class="hospital-address">${hospital.address}</div>
        <div class="hospital-distance">
            <i class="fas fa-map-marker-alt"></i>
            ${hospital.distance.toFixed(1)}km
        </div>
        <div class="hospital-rating">
            <span class="rating-stars">${stars}</span>
            <span class="rating-text">${hospital.rating}</span>
        </div>
    `;

    return div;
}

// 병원 마커 추가
function addHospitalMarkers(hospitalList) {
    // 기존 병원 마커 제거
    markers.forEach(marker => {
        if (!marker.isCurrentLocation) {
            marker.setMap(null);
        }
    });
    markers = markers.filter(marker => marker.isCurrentLocation);

    hospitalList.forEach(hospital => {
        const marker = new kakao.maps.Marker({
            position: new kakao.maps.LatLng(hospital.latitude, hospital.longitude),
            map: map
        });

        // 병원 정보 윈도우
        const content = `
            <div style="padding: 10px; min-width: 200px;">
                <h4 style="margin: 0 0 5px 0; color: #333;">${hospital.name}</h4>
                <p style="margin: 0 0 5px 0; color: #666; font-size: 12px;">${hospital.specialty}</p>
                <p style="margin: 0 0 5px 0; color: #667eea; font-size: 12px;">
                    <i class="fas fa-map-marker-alt"></i> ${hospital.distance.toFixed(1)}km
                </p>
                <p style="margin: 0; color: #666; font-size: 12px;">${hospital.address}</p>
            </div>
        `;

        const infowindow = new kakao.maps.InfoWindow({
            content: content
        });

        kakao.maps.event.addListener(marker, 'click', function() {
            infowindow.open(map, marker);
        });

        markers.push(marker);
    });
}

// 병원 상세 정보 표시
function showHospitalDetail(hospital) {
    const modal = document.getElementById('hospitalModal');
    const content = document.getElementById('modalContent');

    content.innerHTML = `
        <h2><i class="fas fa-hospital"></i> ${hospital.name}</h2>
        <div style="margin: 20px 0;">
            <p><strong>주소:</strong> ${hospital.address}</p>
            <p><strong>전화:</strong> <a href="tel:${hospital.phone}">${hospital.phone}</a></p>
            <p><strong>웹사이트:</strong> <a href="${hospital.website}" target="_blank">${hospital.website}</a></p>
            <p><strong>진료과목:</strong> ${hospital.specialty}</p>
            <p><strong>평점:</strong> ${hospital.rating}/5.0</p>
            <p><strong>진료시간:</strong> ${hospital.operatingHours}</p>
            <p><strong>거리:</strong> ${hospital.distance.toFixed(1)}km</p>
        </div>
        <div style="margin: 20px 0;">
            <h3>병원 소개</h3>
            <p>${hospital.description}</p>
        </div>
        <div style="text-align: center; margin-top: 30px;">
            <button onclick="openDirections(${hospital.latitude}, ${hospital.longitude})" 
                    style="background: #667eea; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer; margin-right: 10px;">
                <i class="fas fa-route"></i> 길찾기
            </button>
            <button onclick="callHospital('${hospital.phone}')" 
                    style="background: #28a745; color: white; border: none; padding: 10px 20px; border-radius: 5px; cursor: pointer;">
                <i class="fas fa-phone"></i> 전화하기
            </button>
        </div>
    `;

    modal.style.display = 'block';
}

// 길찾기 열기
function openDirections(lat, lng) {
    const url = `https://map.kakao.com/link/to/목적지,${lat},${lng}`;
    window.open(url, '_blank');
}

// 병원 전화하기
function callHospital(phone) {
    window.location.href = `tel:${phone}`;
}

// 모달 닫기
function closeModal() {
    document.getElementById('hospitalModal').style.display = 'none';
}

// 병원 필터링
function filterHospitals() {
    if (!currentPosition) {
        alert('먼저 위치를 설정해주세요.');
        return;
    }

    const specialty = document.getElementById('specialtyFilter').value;
    const maxDistance = parseFloat(document.getElementById('distanceFilter').value);

    const filteredHospitals = hospitals.filter(hospital => {
        const specialtyMatch = !specialty || hospital.specialty.includes(specialty);
        const distanceMatch = hospital.distance <= maxDistance;
        return specialtyMatch && distanceMatch;
    });

    displayHospitals(filteredHospitals);
    addHospitalMarkers(filteredHospitals);
}

// AI 추천 받기
function getAIRecommendation() {
    if (!currentPosition) {
        alert('먼저 위치를 설정해주세요.');
        return;
    }

    showLoading();

    const specialty = document.getElementById('specialtyFilter').value;
    const lat = currentPosition.getLat();
    const lng = currentPosition.getLng();

    // 서버의 AI 추천 API 호출
    fetch(`/hospital/api/ai-recommendation?lat=${lat}&lng=${lng}&specialty=${specialty}`)
        .then(response => response.json())
        .then(data => {
            displayAIRecommendation(data.recommendations, data.insights);
            hideLoading();
        })
        .catch(error => {
            console.error('AI 추천을 가져오는 중 오류가 발생했습니다:', error);
            alert('AI 추천을 가져오는 중 오류가 발생했습니다.');
            hideLoading();
        });
}



// AI 추천 결과 표시
function displayAIRecommendation(recommendations, insights) {
    const panel = document.getElementById('aiRecommendation');
    const content = document.getElementById('aiRecommendationContent');

    let html = '<div style="margin-bottom: 20px;">';
    html += '<p><strong>🤖 AI가 분석한 최적의 병원 추천:</strong></p>';
    html += '</div>';

    // 통계 정보 표시
    if (insights) {
        html += `
            <div style="background: rgba(255,255,255,0.1); padding: 15px; border-radius: 8px; margin-bottom: 20px;">
                <h5 style="margin: 0 0 10px 0; color: #ffd700;">📊 분석 통계</h5>
                <p style="margin: 0 0 5px 0; font-size: 14px;">총 병원 수: ${insights.totalHospitals}개</p>
                <p style="margin: 0 0 5px 0; font-size: 14px;">평균 거리: ${insights.averageDistance.toFixed(1)}km</p>
                <p style="margin: 0; font-size: 14px;">평균 평점: ${insights.averageRating.toFixed(1)}/5.0</p>
            </div>
        `;
    }

    recommendations.forEach((rec, index) => {
        const confidence = Math.round(rec.confidence * 100);
        const hospital = rec.hospital;

        html += `
            <div style="background: rgba(255,255,255,0.1); padding: 15px; border-radius: 8px; margin-bottom: 15px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <h5 style="margin: 0; color: #ffd700;">${index + 1}. ${rec.type} 추천</h5>
                    <span style="background: rgba(255,255,255,0.2); padding: 4px 8px; border-radius: 12px; font-size: 12px;">
                        신뢰도: ${confidence}%
                    </span>
                </div>
                <p style="margin: 0 0 8px 0; font-weight: bold; font-size: 16px;">🏥 ${hospital.name}</p>
                <p style="margin: 0 0 8px 0; font-size: 14px;">📍 ${hospital.address}</p>
                <p style="margin: 0 0 8px 0; font-size: 14px;">
                    📏 거리: ${hospital.distance.toFixed(1)}km | ⭐ 평점: ${hospital.rating}/5.0
                </p>
                <p style="margin: 0 0 10px 0; font-size: 14px; font-style: italic; color: #e8f4fd;">
                    💡 ${rec.reason}
                </p>
                <div style="display: flex; gap: 10px;">
                    <button onclick="showHospitalDetail(${JSON.stringify(hospital).replace(/"/g, '&quot;')})" 
                            style="background: rgba(255,255,255,0.2); color: white; border: 1px solid white; padding: 8px 15px; border-radius: 5px; cursor: pointer; font-size: 12px;">
                        📋 상세보기
                    </button>
                    <button onclick="openDirections(${hospital.latitude}, ${hospital.longitude})" 
                            style="background: rgba(255,255,255,0.2); color: white; border: 1px solid white; padding: 8px 15px; border-radius: 5px; cursor: pointer; font-size: 12px;">
                        🗺️ 길찾기
                    </button>
                </div>
            </div>
        `;
    });

    content.innerHTML = html;
    panel.style.display = 'block';
}

// 로딩 표시/숨김
function showLoading() {
    document.getElementById('loading').style.display = 'flex';
}

function hideLoading() {
    document.getElementById('loading').style.display = 'none';
}

// 초기 병원 데이터 로드
function loadHospitals() {
    // 페이지 로드 시 기본 병원 목록 표시
    fetch('/hospital/api/nearby?lat=37.5665&lng=126.9780')
        .then(response => response.json())
        .then(data => {
            hospitals = data;
            displayHospitals(data);
        })
        .catch(error => {
            console.error('초기 병원 데이터 로드 실패:', error);
        });
}
