let cameras = [
    {
        'name': 'rogercam1',
        'description': 'Heftig kamera i stua til Geir'
    },
    {
        'name': 'rogercam2',
        'description': 'se den kule utsikten da'
    }
];

function showCameraList() {
    waitForElementFirst('cameraContainer', 50, _showCameraList)
}

function _showCameraList(container) {
    cameras.forEach(showCameraListEntry);

    function showCameraListEntry(camera) {
        let section = document.createElement('section');
        section.onclick = function () {
            showCamera(camera)
        };

        section.innerHTML = `
        <div class="padded title clear">${camera.name}</div>
        `;
        container.appendChild(section);
    }

    function showCamera(camera) {
        container.innerHTML = '';

        let section = document.createElement('section');
            section.innerHTML = `
        <img alt="${camera.name}" src="/api/video/${camera.name}">
        <div class="padded title clear">${camera.name}</div>
        <div class="padded">${camera.description}</div>
        `;
        container.appendChild(section);
    }
}