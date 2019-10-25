let myCamera = {
    'name': 'rogercam1',
    'description': 'Heftig kamera på stua til Geir'
};

function showCamera() {
    waitForElementFirst('cameraContainer', 50, _showCamera)
}

function _showCamera(container) {
    container.innerHTML = '';

    let section = document.createElement('section');
    section.innerHTML = `
    <img alt="${myCamera}" src="/api/video/${myCamera.name}">
    <div class="padded title">${myCamera.name}</div>
    <div class="padded">${myCamera.description}</div>
    `;
    container.appendChild(section);
}