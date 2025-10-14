window.addEventListener('pageshow', function (event) {
    var navigationEntries = performance.getEntriesByType ? performance.getEntriesByType('navigation') : [];
    var navigationEntry = navigationEntries.length ? navigationEntries[0] : null;
    var cameFromHistory = event.persisted || (navigationEntry && navigationEntry.type === 'back_forward');
    if (cameFromHistory) {
        window.location.replace('index.jsp');
    }
});
