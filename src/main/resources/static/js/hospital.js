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
    const showSavedHospitalsBtn = document.getElementById('showSavedHospitalsBtn'); // 새로 추가된 버튼
    const loadingEl = document.getElementById('loading');

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
    // 저장된 병원 보기 버튼 이벤트 리스너 추가
    showSavedHospitalsBtn.addEventListener('click', showSavedHospitals);

    // --- Promisified Kakao Map Services ---
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

    function searchPlacesByKeyword(keyword, coords, radius, sort, categoryCode) {
        return new Promise((resolve, reject) => {
            places.keywordSearch(keyword, (data, status) => {
                if (status === kakao.maps.services.Status.OK) {
                    resolve(data);
                } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                    resolve([]);
                } else if (status === kakao.maps.services.Status.ERROR) {
                    reject(new Error('검색 결과가 너무 많습니다. 검색 반경을 줄이거나 진료과목을 선택해주세요.'));
                } else {
                    reject(new Error('알 수 없는 오류로 병원 검색에 실패했습니다.'));
                }
            }, {
                location: coords,
                radius: radius * 1000,
                sort: sort,
                category_group_code: categoryCode
            });
        });
    }
    // --- End Promisified Kakao Map Services ---

    // 4. 핵심 로직 함수들 (async/await 적용)
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
            await searchHospitals(coords);
        } catch (error) {
            alert(error.message);
            console.error('주소 검색 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

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
            await searchHospitals(coords);
        } catch (error) {
            alert(error.message);
            console.error('현재 위치 가져오기 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    async function searchHospitals(coords) {
        if (!coords) {
            return;
        }
        currentCoords = coords;
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
                'HP8'
            );
            displayHospitals(hospitals);
        } catch (error) {
            alert(error.message);
            console.error('병원 검색 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    async function saveSelectedHospitals() {
        const checkedItems = document.querySelectorAll('.hospital-item-checkbox:checked');

        if (checkedItems.length === 0) {
            alert('저장할 병원을 선택해주세요.');
            return;
        }

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

        const specialty = specialtyFilter.value || "전체";

        const payload = {
            specialty: specialty,
            hospitals: hospitalsToSave
        };

        try {
            loadingEl.style.display = 'flex';

            const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");

            const response = await fetch('/hospital/api/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [header]: token
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                console.error("----- 서버 응답 오류 발생 -----");
                console.error("상태 코드:", response.status);
                console.error("상태 메시지:", response.statusText);

                const errorBody = await response.text();
                console.error("서버 응답 내용:", errorBody);

                throw new Error(`서버 응답 오류: ${response.status}`);
            }

            alert(`${hospitalsToSave.length}개의 병원 정보가 성공적으로 저장되었습니다.`);

        } catch (error) {
            console.error("----- 최종 에러 처리 -----");
            console.error(error);
            alert('병원 정보 저장에 실패했습니다. 개발자 콘솔(F12)을 확인해주세요.');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // 저장된 병원 목록을 가져와 표시하는 함수
    async function showSavedHospitals() {
        loadingEl.style.display = 'flex';
        clearResults(); // 기존 검색 결과 지우기

        try {
            const response = await fetch('/hospital/api/saved');

            if (!response.ok) {
                throw new Error(`서버 응답 오류: ${response.status}`);
            }

            const savedHospitals = await response.json();

            if (savedHospitals.length === 0) {
                hospitalListEl.innerHTML = '<div class="no-result">저장된 병원 정보가 없습니다.</div>';
                return;
            }

            // 저장된 병원 정보를 지도에 표시하고 목록에 추가
            const bounds = new kakao.maps.LatLngBounds();
            savedHospitals.forEach(savedInfo => {
                const place = savedInfo.hospital; // UserSavedHospital 객체에서 hospital 정보 추출
                const specialty = savedInfo.specialty; // 저장된 진료과목 정보

                const position = new kakao.maps.LatLng(place.y, place.x);
                const marker = new kakao.maps.Marker({
                    map: map,
                    position: position,
                    title: place.name
                });
                markers.push(marker);
                bounds.extend(position);

                const itemEl = document.createElement('div');
                itemEl.className = 'hospital-item';
                itemEl.innerHTML = `
                    <div class="hospital-icon">
                        <i class="fa-solid fa-hospital"></i>
                    </div>
                    <div class="hospital-info">
                        <div class="hospital-name">${place.name}</div>
                        <div class="hospital-address">${place.address}</div>
                        <div class="hospital-phone">${place.phone || '전화번호 정보 없음'}</div>
                        <div class="hospital-specialty">저장 진료과목: ${specialty}</div>
                    </div>
                    <div class="hospital-distance"></div> <!-- 저장된 병원은 거리 정보가 없으므로 비워둠 -->
                `;

                const openInfoWindow = () => {
                    const content = `
                        <div style="padding:10px; min-width:280px; font-size:14px; line-height:1.6;">
                            <div style="font-weight:bold; margin-bottom:5px; font-size:16px;">${place.name}</div>
                            <div style="color:#333;">${place.address}</div>
                            <div style="color:#0055A3;">${place.phone || ''}</div>
                            <div style="margin-top: 8px;">
                                <a href="${place.url}" style="color:#007BFF; text-decoration:none; font-weight:bold; margin-right: 15px;" target="_blank">상세보기</a>
                                <a href="https://map.kakao.com/link/to/${place.name},${place.y},${place.x}" style="color:#007BFF; text-decoration:none; font-weight:bold;" target="_blank">길찾기</a>
                            </div>
                        </div>`;

                    infowindow.setContent(content);
                    infowindow.open(map, marker);
                    map.panTo(marker.getPosition());
                };

                kakao.maps.event.addListener(marker, 'click', openInfoWindow);
                itemEl.addEventListener('click', openInfoWindow);

                kakao.maps.event.addListener(marker, 'mouseover', () => itemEl.classList.add('active'));
                kakao.maps.event.addListener(marker, 'mouseout', () => itemEl.classList.remove('active'));
                itemEl.addEventListener('mouseover', () => marker.setZIndex(1));
                itemEl.addEventListener('mouseout', () => marker.setZIndex(0));

                hospitalListEl.appendChild(itemEl);
            });
            map.setBounds(bounds);

        } catch (error) {
            alert('저장된 병원 목록을 불러오는 데 실패했습니다. 개발자 콘솔(F12)을 확인해주세요.');
            console.error('저장된 병원 목록 조회 오류:', error);
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    // 5. 결과 표시 및 초기화 함수들
    function displayHospitals(hospitals) {
        const bounds = new kakao.maps.LatLngBounds();
        hospitalListEl.innerHTML = '';

        saveHospitalsBtn.style.display = hospitals.length > 0 ? 'inline-block' : 'none';

        if (hospitals.length === 0) {
            hospitalListEl.innerHTML = '<div class="no-result">검색 결과가 없습니다.</div>';
            return;
        }

        hospitals.forEach((place) => {
            const position = new kakao.maps.LatLng(place.y, place.x);
            const marker = new kakao.maps.Marker({
                map: map,
                position: position,
                title: place.place_name
            });
            markers.push(marker);

            bounds.extend(position);

            const itemEl = document.createElement('div');
            itemEl.className = 'hospital-item';
            itemEl.innerHTML = `
                <div class="hospital-icon">
                    <i class="fa-solid fa-hospital"></i>
                </div>
                <div class="hospital-info">
                    <div class="hospital-name">${place.place_name}</div>
                    <div class="hospital-address">${place.road_address_name || place.address_name}</div>
                    <div class="hospital-phone">${place.phone || '전화번호 정보 없음'}</div>
                </div>
                <input type="checkbox" class="hospital-item-checkbox" data-place='${JSON.stringify(place)}'>
                <div class="hospital-distance">${place.distance}m</div>
            `;

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

            // 카드 클릭 시 정보창 열기
            itemEl.addEventListener('click', openInfoWindow);

            // 체크박스 클릭 시 정보창이 열리는 것을 방지
            const checkbox = itemEl.querySelector('.hospital-item-checkbox');
            checkbox.addEventListener('click', (e) => {
                e.stopPropagation();
            });

            // 마커와 목록 아이템에 마우스 이벤트 연결
            kakao.maps.event.addListener(marker, 'click', openInfoWindow);
            kakao.maps.event.addListener(marker, 'mouseover', () => itemEl.classList.add('active'));
            kakao.maps.event.addListener(marker, 'mouseout', () => itemEl.classList.remove('active'));
            itemEl.addEventListener('mouseover', () => marker.setZIndex(1));
            itemEl.addEventListener('mouseout', () => marker.setZIndex(0));

            hospitalListEl.appendChild(itemEl);
        });

        map.setBounds(bounds);
    }

    function clearResults() {
        markers.forEach(marker => marker.setMap(null));
        saveHospitalsBtn.style.display = 'none';
        markers = [];
        hospitalListEl.innerHTML = '';
    }

    // 6. 페이지 첫 로딩 시 실행
    initMap(37.566826, 126.9786567);
});
