(function () {
  'use strict';

  var form = document.querySelector('.contact .email-form');
  if (!form) return;

  var loading = form.querySelector('.loading');
  var errorBox = form.querySelector('.error-message');
  var sentBox = form.querySelector('.sent-message');
  var button = form.querySelector('button[type=submit]');

  function hideAll() {
    loading.style.display = 'none';
    errorBox.style.display = 'none';
    sentBox.style.display = 'none';
  }

  function showError(msg) {
    loading.style.display = 'none';
    errorBox.textContent = msg;
    errorBox.style.display = 'block';
  }

  form.addEventListener('submit', function (e) {
    e.preventDefault();
    hideAll();
    loading.style.display = 'block';
    button.disabled = true;

    fetch(form.getAttribute('action'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: new URLSearchParams(new FormData(form)).toString()
    })
      .then(function (res) {
        return res.text().then(function (text) {
          if (!res.ok) throw new Error(text || 'Something went wrong.');
          return text;
        });
      })
      .then(function () {
        loading.style.display = 'none';
        sentBox.style.display = 'block';
        form.reset();
      })
      .catch(function (err) {
        showError(err.message || 'Something went wrong. Please try again.');
      })
      .then(function () {
        button.disabled = false;
      });
  });
})();