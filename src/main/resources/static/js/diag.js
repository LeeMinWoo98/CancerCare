document.addEventListener('DOMContentLoaded', function() {
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

            // --- 👇 [수정된 부분] ---
            // form을 감싸고 있는 부모(.anal-sec)를 먼저 찾고,
            const parentSection = this.closest('.anal-sec'); 
            // 그 안에서 .result 클래스를 가진 요소를 찾습니다.
            const resultDiv = parentSection.querySelector('.result'); 

            if (!resultDiv) {
                console.error("Error: Could not find the result div for this form.");
                return;
            }

            resultDiv.innerHTML = '<p>분석 중입니다...</p>';

            fetch(actionUrl, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    resultDiv.innerHTML = `<p style="color: red;"><strong>오류:</strong> ${data.error}</p>`;
                } else {
                    resultDiv.innerHTML = `<p><strong>예측 결과:</strong> ${data.prediction}</p>`;
                }
            })
            .catch(error => {
                console.error('Error:', error);
                resultDiv.innerHTML = `<p style="color: red;">클라이언트 측 오류가 발생했습니다.</p>`;
            });
        });
    });
});