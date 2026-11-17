/* 모든 화면 공통: 알림(토스트), 삭제 확인 */
(function () {
    'use strict';

    function hideLater(el, ms) {
        setTimeout(function () {
            el.classList.add('hide');
            setTimeout(function () { el.remove(); }, 400);
        }, ms);
    }

    /** 화면 위쪽에 잠깐 보이는 알림. type: ok | error | info */
    window.jebrisToast = function (message, type) {
        if (!message) return;
        var wrap = document.getElementById('toastWrap');
        if (!wrap) {
            wrap = document.createElement('div');
            wrap.id = 'toastWrap';
            wrap.className = 'toast-wrap';
            document.body.appendChild(wrap);
        }
        var el = document.createElement('div');
        el.className = 'toast toast-' + (type || 'info');
        el.setAttribute('role', 'status');
        el.textContent = message;
        wrap.appendChild(el);
        hideLater(el, 3200);
    };

    document.addEventListener('DOMContentLoaded', function () {
        // 서버에서 보낸 알림도 잠시 뒤 사라지게 한다.
        document.querySelectorAll('.toast-wrap .toast').forEach(function (el) { hideLater(el, 3200); });

        // data-confirm 이 있는 폼은 제출 전에 한 번 더 묻는다.
        document.querySelectorAll('form[data-confirm]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm(form.getAttribute('data-confirm'))) {
                    e.preventDefault();
                }
            });
        });
    });
})();
