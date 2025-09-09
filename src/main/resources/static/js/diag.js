// human_team6/src/main/resources/static/js/diag.js

document.addEventListener('DOMContentLoaded', function () {
    const form = document.querySelector('.upload-form');
    const imagePreview = document.getElementById('image-preview');
    const imageFile = document.getElementById('imageFile');
    const resultSection = document.getElementById('result-section');
    const chatPanel = document.getElementById('chat-panel');
    const chatMessages = document.getElementById('chatMessages');
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const chatForm = document.getElementById('chatForm');
    const closeChatBtn = document.getElementById('closeChatBtn');

    // CSRF 토큰 설정
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    let currentDiagnosisId = null;

    // 이미지 파일 선택 시 미리보기
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

    // 챗봇 메시지 추가 함수
    function addMessage(text, isUser) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isUser ? 'user' : 'ai'}`;
        const sanitizedText = text.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        messageDiv.innerHTML = `
            <div class="message-avatar">${isUser ? 'U' : 'AI'}</div>
            <div class="message-content">${sanitizedText.replace(/\n/g, '<br>')}</div>
        `;
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // 챗봇 메시지 전송
    function sendMessage() {
        const messageText = messageInput.value.trim();
        if (!messageText || !currentDiagnosisId) return;

        addMessage(messageText, true);
        messageInput.value = '';
        messageInput.style.height = 'auto';
        sendBtn.disabled = true;

        fetch('/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({
                message: messageText,
                diagnosisId: currentDiagnosisId
            })
        })
        .then(response => response.json())
        .then(data => {
            addMessage(data.success ? data.response : '오류가 발생했습니다.', false);
        })
        .catch(error => {
            console.error('Error:', error);
            addMessage('네트워크 오류가 발생했습니다.', false);
        });
    }

    // 채팅 내역 로드
    function loadChatHistory(diagnosisId) {
        fetch(`/chat/history/${diagnosisId}`)
            .then(response => response.ok ? response.json() : [])
            .then(history => {
                chatMessages.innerHTML = '';
                if (history && history.length > 0) {
                    history.forEach(msg => {
                        addMessage(msg.messageText, msg.sender === 'user');
                    });
                } else {
                    addMessage('안녕하세요! 분석 결과에 대해 궁금한 점이 있으시면 언제든지 질문해주세요.', false);
                }
            })
            .catch(error => {
                console.error('Error loading chat history:', error);
                chatMessages.innerHTML = '';
                addMessage('안녕하세요! 분석 결과에 대해 궁금한 점이 있으시면 언제든지 질문해주세요.', false);
            });
    }

    // 폼 제출 이벤트
    form.addEventListener('submit', function (e) {
        e.preventDefault();

        const formData = new FormData(form);
        if (!imageFile.files[0]) {
            alert('이미지 파일을 선택해주세요.');
            return;
        }

        resultSection.style.display = 'block';
        const resultDiv = resultSection.querySelector('.result');
        
        // ✨ 수정된 부분: 채팅창을 숨기지 않고, show 클래스만 제거합니다.
        if (chatPanel) {
            chatPanel.classList.remove('show');
        }

        if (resultDiv) {
            resultDiv.innerHTML = '<p class="loading">AI가 이미지를 분석 중입니다. 잠시만 기다려주세요...</p>';
        }

        fetch('/analyze/check', {
            method: 'POST',
            headers: { [csrfHeader]: csrfToken },
            body: formData
        })
        .then(response => {
            if (!response.ok) throw new Error(`서버 응답 오류: ${response.status}`);
            return response.json();
        })
        .then(data => {
            if (data.success && data.prediction && data.diagnosisId) {
                if (resultDiv) {
                    resultDiv.innerHTML = `
                        <div class="result-header">
                            <span class="result-icon">🔍</span>
                            AI 분석 결과
                        </div>
                        <p class="result-content">${data.prediction}</p>
                    `;
                }
                
                // ✨ 수정된 부분: 불필요한 스타일 직접 조작을 제거하고 'show' 클래스만 추가합니다.
                if (chatPanel) {
                    setTimeout(() => {
                        chatPanel.classList.add('show');
                    }, 100); // 짧은 딜레이 후 클래스 추가
                }

                currentDiagnosisId = data.diagnosisId;
                loadChatHistory(data.diagnosisId);

            } else {
                if (resultDiv) {
                    resultDiv.innerHTML = `<p class="error">오류: ${data.error || '분석 중 오류가 발생했습니다.'}</p>`;
                }
            }
        })
        .catch(error => {
            console.error('Error:', error);
            if (resultDiv) {
                resultDiv.innerHTML = `<p class="error">분석 요청에 실패했습니다. 네트워크 연결을 확인해주세요.</p>`;
            }
        });
    });

    // 챗봇 관련 이벤트 리스너들
    messageInput.addEventListener('input', function () {
        this.style.height = 'auto';
        this.style.height = `${Math.min(this.scrollHeight, 100)}px`;
        sendBtn.disabled = !this.value.trim();
    });

    messageInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    chatForm.addEventListener('submit', function (e) {
        e.preventDefault();
        sendMessage();
    });

    // 채팅창 닫기
    closeChatBtn.addEventListener('click', function () {
        if (chatPanel) {
            // ✨ 수정된 부분: display 속성 대신 'show' 클래스만 제거합니다.
            chatPanel.classList.remove('show');
        }
    });

    // ESC 키로 채팅창 닫기
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && chatPanel && chatPanel.classList.contains('show')) {
            closeChatBtn.click();
        }
    });
});