document.addEventListener('DOMContentLoaded', function() {

    // --- 이미지 미리보기 기능 시작 ---
    const imageInput = document.getElementById('imageFile');
    const imagePreview = document.getElementById('image-preview');

    imageInput.addEventListener('change', function() {
        const file = this.files[0]; // 사용자가 선택한 첫 번째 파일

        if (file) {
            const reader = new FileReader(); // 파일을 읽기 위한 객체 생성

            reader.onload = function(e) {
                // 파일 읽기가 성공적으로 끝나면,
                imagePreview.src = e.target.result; // img 태그의 src를 읽은 파일 데이터로 설정
                imagePreview.style.display = 'block'; // 숨겨져 있던 img 태그를 보여줌
            };

            reader.readAsDataURL(file); // 파일 읽기 시작
        }
    });
    // --- 이미지 미리보기 기능 끝 ---


    // 분석 종류 선택 버튼 로직 (동일)
    const analysisButtons = document.querySelectorAll('.anal_btn');
    const analysisSections = document.querySelectorAll('.anal-sec');
    analysisButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.dataset.target;
            analysisSections.forEach(section => section.style.display = 'none');
            const targetSection = document.getElementById(targetId);
            if (targetSection) targetSection.style.display = 'block';
        });
    });

    // AJAX 폼 제출 로직
    const analysisForms = document.querySelectorAll('.anal-sec form');
    analysisForms.forEach(form => {
        form.addEventListener('submit', function(event) {
            event.preventDefault();

            const formData = new FormData(this);
            const actionUrl = this.action;

            const parentSection = this.closest('.anal-sec');
            const resultDiv = parentSection.querySelector('.result');

            if (!resultDiv) {
                console.error("Error: Could not find the result div for this form.");
                return;
            }

            resultDiv.innerHTML = '<p>분석 중입니다...</p>';

            const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
            const headerName = document.querySelector("meta[name='_csrf_header']").getAttribute("content");


            fetch(actionUrl, {
                method: 'POST',
                headers: {
                    // 👇 여기에 CSRF 토큰을 추가합니다
                    [headerName]: token
                },
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        // 서버에서 4xx, 5xx 에러 응답을 받았을 때 처리
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.error) {
                        resultDiv.innerHTML = `<p style="color: red;"><strong>오류:</strong> ${data.error}</p>`;
                    } else {
                        resultDiv.innerHTML = `<p><strong>예측 결과:</strong> ${data.prediction}</p>`;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    resultDiv.innerHTML = `<p style="color: red;">클라이언트 측 오류가 발생했습니다. 콘솔을 확인해주세요.</p>`;
                });
        });
    });
});

