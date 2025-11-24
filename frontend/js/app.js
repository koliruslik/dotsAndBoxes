document.getElementById('checkBtn').addEventListener('click', () => {
  fetch('http://localhost:8080/api/hello')  // "backend" = имя сервиса в docker-compose
    .then(response => response.text())
    .then(data => {
      document.getElementById('response').textContent = data;
    })
    .catch(err => {
      document.getElementById('response').textContent = 'Error: ' + err;
    });
});
