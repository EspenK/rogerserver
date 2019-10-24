let myCamera = "rogercam1";

function showCamera() {
    waitForElementFirst('cameraContainer', 50, _showCamera)
}

function _showCamera(container) {
    container.innerHTML = '';

    let section = document.createElement('section');
    section.innerHTML = `
    <h1>${myCamera}</h1>
    <img src="/api/camera/${myCamera}">
    `;
    container.appendChild(section);
}