document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('.upload-form');
    const resultDiv = document.querySelector('.result');
    const imagePreview = document.getElementById('image-preview');
    const imageFile = document.getElementById('imageFile');
    
    // ✨ [수정] 챗봇 버튼 관련 DOM 요소 추가
    const chatLinkContainer = document.getElementById('chat-link-container');
    const chatLink = document.getElementById('chat-link');
	

    // CSRF 토큰 설정
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    // 이미지 파일 선택 시 미리보기 기능
    imageFile.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                imagePreview.src = e.target.result;
                imagePreview.style.display = 'block';
            }
            reader.readAsDataURL(file);
        } else {
            imagePreview.style.display = 'none';
        }
    });

    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const formData = new FormData(form);

        // 이전 결과 및 버튼 숨기기
        resultDiv.innerHTML = '<p class="loading">AI가 MRI 이미지를 분석 중입니다. 잠시만 기다려주세요...</p>';
        chatLinkContainer.style.display = 'none'; // ✨ [수정] 버튼 숨기기

        fetch('/analyze/check', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답에 문제가 있습니다.');
            }
            return response.json();
        })
        .then(data => {
            if (data.prediction) {
                const resultText = data.prediction;
                resultDiv.innerHTML = `
                    <h3>AI 분석 결과</h3>
                    <p class="prediction">${resultText}</p>
                    <p class="disclaimer">이 분석은 참고용이며, 정확한 진단은 전문의와 상담하세요.</p>
                `;

                // ✨ [수정] 챗봇으로 이동할 링크 생성 및 버튼 표시
                const chatUrl = `/chat/chatbot?diagnosis=${encodeURIComponent(resultText)}`;
                chatLink.href = chatUrl;
                chatLinkContainer.style.display = 'block'; // 버튼 보이기

            } else {
                resultDiv.innerHTML = `<p class="error">오류: ${data.error || '분석 중 오류가 발생했습니다.'}</p>`;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            resultDiv.innerHTML = `<p class="error">분석 요청에 실패했습니다. 네트워크 연결을 확인해주세요.</p>`;
        });
    });
});