function validateLoginForm(role) {
    var user = document.getElementById('username').value.trim();
    var pass = document.getElementById('password').value.trim();
    if (user === '' || pass === '') {
        alert('Please enter both username and password!');
        return false;
    }
    document.getElementById('role').value = role;
    return true;
}

window.addEventListener('pageshow', function (event) {
    var navigationEntries = (window.performance && window.performance.getEntriesByType)
        ? window.performance.getEntriesByType('navigation') : null;
    var navigationType = navigationEntries && navigationEntries.length > 0
        ? navigationEntries[0].type : null;

    if (event.persisted || navigationType === 'back_forward') {
        window.location.reload();
        return;
    }

    var form = document.querySelector('form[action="./login"]');
    if (form) {
        form.reset();
    }
});
