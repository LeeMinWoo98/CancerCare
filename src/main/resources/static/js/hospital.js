document.addEventListener('DOMContentLoaded', function () {

    // 안전장치: 카카오맵 API가 로드되었는지 확인
    if (typeof kakao === 'undefined' || typeof kakao.maps === 'undefined') {
        console.error('카카오맵 API가 로드되지 않았습니다. API 키가 정확한지, 서버에서 키를 제대로 전달하는지 확인해주세요.');
        const mapContainer = document.getElementById('map');
        if (mapContainer) {
            mapContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #888;">지도를 불러오는 데 실패했습니다.<br>API 키 설정을 확인해주세요.</div>';
        }
        return; // API가 없으면 스크립트 실행 중단
    }

    // 1. DOM 요소 및 변수 초기화
    const mapContainer = document.getElementById('map');
    const locationInput = document.getElementById('locationInput');
    const searchBtn = document.getElementById('searchBtn');
    const currentLocationBtn = document.getElementById('currentLocationBtn');
    const specialtyFilter = document.getElementById('specialtyFilter');
    const distanceFilter = document.getElementById('distanceFilter');
    const hospitalListEl = document.getElementById('hospitalList');
    const saveHospitalsBtn = document.getElementById('saveHospitalsBtn');
    const loadingEl = document.getElementById('loading');

    // AI 추천 관련 DOM 요소
    const aiRecommendBtn = document.getElementById('aiRecommendBtn');
    const aiPanel = document.getElementById('aiRecommendation');
    const aiRecommendationContent = document.getElementById('aiRecommendationContent');
    const closeAiPanelBtn = document.getElementById('closeAiPanelBtn');

    // Map controls
    const zoomInBtn = document.getElementById('zoomIn');
    const zoomOutBtn = document.getElementById('zoomOut');
    const resetMapBtn = document.getElementById('resetMap');

    let map;
    let places;
    let geocoder; // Geocoder 서비스 변수 추가

    let infowindow; // 정보창을 표시할 객체
    let currentCoords; // 현재 검색된 좌표
    let markers = []; // 마커들을 저장할 배열

    // 2. 지도 초기화 함수
    function initMap(lat, lng) {
        const mapOption = {
            center: new kakao.maps.LatLng(lat, lng),
            level: 5
        };
        map = new kakao.maps.Map(mapContainer, mapOption);
        places = new kakao.maps.services.Places(map);
        geocoder = new kakao.maps.services.Geocoder(); // Geocoder 서비스 초기화
        infowindow = new kakao.maps.InfoWindow({
            removable: true, // 닫기 버튼(X) 표시
            zIndex: 10
        });

        // 지도 컨트롤러 추가
        const mapTypeControl = new kakao.maps.MapTypeControl();
        map.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);
        const zoomControl = new kakao.maps.ZoomControl();
        map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

        // 지도 컨트롤 이벤트 리스너
        zoomInBtn.addEventListener('click', () => map.setLevel(map.getLevel() - 1));
        zoomOutBtn.addEventListener('click', () => map.setLevel(map.getLevel() + 1));
        resetMapBtn.addEventListener('click', () => {
            if (currentCoords) {
                map.setCenter(currentCoords);
                map.setLevel(5); // 기본 줌 레벨로 리셋
            } else {
                // 검색된 좌표가 없으면 초기 지도 위치로 리셋
                initMap(37.566826, 126.9786567);
            }
        });
    }

    // 3. 이벤트 리스너 등록
    searchBtn.addEventListener('click', handleSearch);
    currentLocationBtn.addEventListener('click', handleCurrentLocation);
    locationInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });
    // 필터 변경 시 자동으로 재검색
    specialtyFilter.addEventListener('change', () => {
        if (currentCoords) searchHospitals(currentCoords);
    });
    distanceFilter.addEventListener('change', () => {
        if (currentCoords) searchHospitals(currentCoords);
    });
    // 저장 버튼 이벤트 리스너 추가
    saveHospitalsBtn.addEventListener('click', saveSelectedHospitals);

    // --- Promisified Kakao Map Services ---
    // Geocoder.addressSearch를 Promise로 감싸는 함수
    function searchAddressToCoords(address) {
        return new Promise((resolve, reject) => {
            geocoder.addressSearch(address, (result, status) => {
                if (status === kakao.maps.services.Status.OK) {
                    resolve(new kakao.maps.LatLng(result[0].y, result[0].x));
                } else {
                    reject(new Error('주소 검색에 실패했습니다.'));
                }
            });
        });
    }

    // Places.keywordSearch를 Promise로 감싸는 함수
    function searchPlacesByKeyword(keyword, coords, radius, sort, categoryCode) {
        return new Promise((resolve, reject) => {
            places.keywordSearch(keyword, (data, status) => {
                if (status === kakao.maps.services.Status.OK) {
                    resolve(data);
                } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                    resolve([]); // 결과가 없어도 에러가 아님
                } else if (status === kakao.maps.services.Status.ERROR) {
                    reject(new Error('검색 결과가 너무 많습니다. 검색 반경을 줄이거나 진료과목을 선택해주세요.'));
                } else {
                    reject(new Error('알 수 없는 오류로 병원 검색에 실패했습니다.'));
                }
            }, {
                location: coords,
                radius: radius * 1000, // km를 미터로 변환
                sort: sort,
                category_group_code: categoryCode // 카테고리 코드를 이용해 검색 결과 제한
            });
        });
    }
    // --- End Promisified Kakao Map Services ---

    // 4. 핵심 로직 함수들 (async/await 적용)

    // 주소로 검색 처리 (async/await 사용)
    async function handleSearch() {
        const address = locationInput.value.trim();
        if (!address) {
            alert('주소를 입력해주세요.');
            return;
        }
        loadingEl.style.display = 'flex';
        try {
            const coords = await searchAddressToCoords(address);
            map.setCenter(coords);
            await searchHospitals(coords); // 병원 검색 완료까지 기다림
        } catch (error) {
            alert(error.message);
            console.error('주소 검색 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // 현재 위치로 검색 처리 (async/await 사용)
    async function handleCurrentLocation() {
        loadingEl.style.display = 'flex';
        try {
            const position = await new Promise((resolve, reject) => {
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition(resolve, reject);
                } else {
                    reject(new Error('이 브라우저에서는 현재 위치 기능을 지원하지 않습니다.'));
                }
            });
            const coords = new kakao.maps.LatLng(position.coords.latitude, position.coords.longitude);
            map.setCenter(coords);
            await searchHospitals(coords); // 병원 검색 완료까지 기다림
        } catch (error) {
            alert(error.message);
            console.error('현재 위치 가져오기 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // 카카오맵 API로 병원 검색 (async/await 사용)
    async function searchHospitals(coords) {
        if (!coords) {
            // 좌표가 없으면 검색하지 않음 (페이지 첫 로딩 시)
            return;
        }
        currentCoords = coords; // 현재 검색 좌표 저장
        loadingEl.style.display = 'flex';
        clearResults();

        const specialty = specialtyFilter.value;
        const distance = distanceFilter.value;
        const keyword = specialty ? `${specialty}` : '병원';

        try {
            const hospitals = await searchPlacesByKeyword(
                keyword,
                coords,
                distance,
                kakao.maps.services.SortBy.DISTANCE,
                'HP8' // 병원(HP8) 카테고리 내에서만 검색하도록 제한
            );
            displayHospitals(hospitals);
        } catch (error) {
            alert(error.message);
            console.error('병원 검색 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // AI 추천 요청 처리 (async/await 사용)
    async function handleAiRecommendation() {
        if (!currentCoords) {
            alert('먼저 위치를 검색해주세요.');
            return;
        }

        loadingEl.style.display = 'flex';
        aiPanel.style.display = 'none'; // 이전 결과 숨기기

        try {
            const lat = currentCoords.getLat();
            const lng = currentCoords.getLng();
            const specialty = specialtyFilter.value;

            // URL 쿼리 파라미터 구성
            const params = new URLSearchParams({
                lat: lat,
                lng: lng,
            });
            if (specialty) {
                params.append('specialty', specialty);
            }
            // TODO: 진단명(diagnosis) 파라미터가 필요하다면, 해당 값을 가져오는 로직 추가
            // params.append('diagnosis', 'some_diagnosis');

            const response = await fetch(`/hospital/api/ai-recommendation?${params.toString()}`);

            if (!response.ok) {
                throw new Error('AI 추천을 받아오는 데 실패했습니다.');
            }

            const result = await response.json();
            displayAiRecommendations(result);

        } catch (error) {
            alert(error.message);
            console.error('AI 추천 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // AI 추천 결과를 화면에 표시
    function displayAiRecommendations(result) {
        // TODO: 받아온 result.recommendations와 result.insights를 사용하여
        // aiRecommendationContent 내부에 HTML을 동적으로 생성하는 로직을 구현합니다.
        aiRecommendationContent.innerHTML = `<pre>${JSON.stringify(result, null, 2)}</pre>`; // 임시로 JSON 데이터 표시
        aiPanel.style.display = 'block'; // 패널 보이기
    }

    // 선택된 병원을 저장하는 함수
    async function saveSelectedHospitals() {
        // 1. 체크된 병원 목록 가져오기
        const checkedItems = document.querySelectorAll('.hospital-item-checkbox:checked');

        if (checkedItems.length === 0) {
            alert('저장할 병원을 선택해주세요.');
            return;
        }

        // 2. 서버로 보낼 데이터 구성
        const hospitalsToSave = [];
        checkedItems.forEach(checkbox => {
            const placeData = JSON.parse(checkbox.dataset.place);
            hospitalsToSave.push({
                id: placeData.id,
                name: placeData.place_name,
                address: placeData.road_address_name || placeData.address_name,
                phone: placeData.phone,
                url: placeData.place_url,
                x: placeData.x,
                y: placeData.y
            });
        });

        const specialty = specialtyFilter.value || "전체"; // 선택된 진료과목

        const payload = {
            specialty: specialty,
            hospitals: hospitalsToSave
        };

        // 3. fetch를 사용하여 서버에 데이터 전송
        try {
            loadingEl.style.display = 'flex';

            // 1. HTML에 숨겨둔 CSRF 토큰(허가증)을 찾습니다.
            const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

            const response = await fetch('/hospital/api/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // 2. 요청 헤더에 찾은 토큰을 정확한 이름으로 추가합니다.
                    [header]: token
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                // 서버 응답이 실패(4xx, 5xx 에러)했을 때, 더 자세한 정보를 담은 에러를 생성합니다.
                console.error("----- 서버 응답 오류 발생 -----");
                console.error("상태 코드:", response.status); // 예: 403
                console.error("상태 메시지:", response.statusText); // 예: Forbidden

                const errorBody = await response.text(); // 서버가 보낸 오류 페이지(HTML)나 메시지(JSON)를 확인합니다.
                console.error("서버 응답 내용:", errorBody);

                throw new Error(`서버 응답 오류: ${response.status}`);
            }

            alert(`${hospitalsToSave.length}개의 병원 정보가 성공적으로 저장되었습니다.`);

        } catch (error) {
            // 네트워크 연결 오류 또는 위에서 발생시킨 에러를 여기서 잡습니다.
            console.error("----- 최종 에러 처리 -----");
            console.error(error);
            alert('병원 정보 저장에 실패했습니다. 개발자 콘솔(F12)을 확인해주세요.');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // 5. 결과 표시 및 초기화 함수들

    // 검색 결과를 화면에 표시
    function displayHospitals(hospitals) {
        const bounds = new kakao.maps.LatLngBounds();
        hospitalListEl.innerHTML = ''; // 이전 목록 아이템 초기화

        // 검색 결과가 있으면 '선택 저장' 버튼 표시, 없으면 숨김
        saveHospitalsBtn.style.display = hospitals.length > 0 ? 'inline-block' : 'none';

        if (hospitals.length === 0) {
            hospitalListEl.innerHTML = '<div class="no-result">검색 결과가 없습니다.</div>';
            return;
        }

        hospitals.forEach((place) => {
            // 마커 생성
            const position = new kakao.maps.LatLng(place.y, place.x);
            const marker = new kakao.maps.Marker({
                map: map,
                position: position,
                title: place.place_name // 마커에 마우스 오버 시 병원 이름 표시
            });
            markers.push(marker);

            bounds.extend(position);

            // 목록 아이템 생성
            const itemEl = document.createElement('div');
            itemEl.className = 'hospital-item';
            itemEl.innerHTML = `
                <div class="item-checkbox">
                    <input type="checkbox" class="hospital-item-checkbox" data-place='${JSON.stringify(place)}'>
                </div>
                <div class="item-info">
                    <h4>${place.place_name}</h4>
                    <p>${place.road_address_name || place.address_name}</p>
                    <p class="tel">${place.phone || '전화번호 정보 없음'}</p>
                </div>
                <div class="item-distance">
                    <span>${place.distance}m</span>
                </div>
            `;

            // 공통으로 사용할 인포윈도우 열기 함수
            const openInfoWindow = () => {
                const content = `
                    <div style="padding:10px; min-width:280px; font-size:14px; line-height:1.6;">
                        <div style="font-weight:bold; margin-bottom:5px; font-size:16px;">${place.place_name}</div>
                        <div style="color:#333;">${place.road_address_name || place.address_name}</div>
                        <div style="color:#0055A3;">${place.phone || ''}</div>
                        <div style="margin-top: 8px;">
                            <a href="${place.place_url}" style="color:#007BFF; text-decoration:none; font-weight:bold; margin-right: 15px;" target="_blank">상세보기</a>
                            <a href="https://map.kakao.com/link/to/${place.place_name},${place.y},${place.x}" style="color:#007BFF; text-decoration:none; font-weight:bold;" target="_blank">길찾기</a>
                        </div>
                    </div>`;

                infowindow.setContent(content);
                infowindow.open(map, marker);
                map.panTo(marker.getPosition());
            };

            // 마커와 목록 아이템에 클릭 이벤트 등록
            kakao.maps.event.addListener(marker, 'click', openInfoWindow);
            itemEl.addEventListener('click', openInfoWindow);

            // 마커와 목록 아이템에 마우스 이벤트 연결
            kakao.maps.event.addListener(marker, 'mouseover', () => itemEl.classList.add('active'));
            kakao.maps.event.addListener(marker, 'mouseout', () => itemEl.classList.remove('active'));
            itemEl.addEventListener('mouseover', () => marker.setZIndex(1)); // 마커를 최상단으로
            itemEl.addEventListener('mouseout', () => marker.setZIndex(0)); // 마커 z-index 리셋

            hospitalListEl.appendChild(itemEl);
        });

        map.setBounds(bounds);
    }

    // 이전 검색 결과(마커, 목록) 지우기
    function clearResults() {
        markers.forEach(marker => marker.setMap(null)); // 지도에서 마커 제거
        saveHospitalsBtn.style.display = 'none'; // 저장 버튼 숨기기
        markers = [];
        hospitalListEl.innerHTML = '';
    }

    // 6. 페이지 첫 로딩 시 실행
    // 기본 위치(예: 서울 시청)로 지도 초기화
    initMap(37.566826, 126.9786567);
});