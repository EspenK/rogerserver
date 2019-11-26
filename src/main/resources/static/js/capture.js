function showCaptureList() {
    location.href = '#capture';
    waitForElementFirst('captureContainer', 50, _showCaptureList)
}

async function getCaptures() {
    let response = await fetch_secure('api/capture', {method: 'GET'});

    if (response.status === 200) {
        return await response.json();
    } else {
        let data = await response.json();
        console.log(data.message);
        return null;
    }
}

async function _showCaptureList(container) {
    container.innerHTML = '';

    let cameras = await getCaptures();
    cameras.forEach(showCaptureListEntry);

    function showCaptureListEntry(capture) {
        let section = document.createElement('section');

        section.innerHTML = `<img alt="${capture.cameraName}" src="/api/capture/${capture.id}.jpg">
        <div class="padded title clear">${capture.cameraName} | ${capture.timestamp}</div>`;
        container.appendChild(section);
    }
}