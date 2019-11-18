function showCameraList() {
    location.href = '#camera';
    waitForElementFirst('cameraContainer', 50, _showCameraList)
}

async function getCameras() {
    let response = await fetch_secure('api/camera', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (response.status === 200) {
        return await response.json();
    } else {
        let data = await response.json();
        console.log(data.message);
        return null;
    }
}

async function _showCameraList(container) {
    container.innerHTML = '';

    container.innerHTML = `
        <div class="padded">
            <button class="small" onclick="location.href='#newcamera'" type="button">Add a new camera</button>
            <button class="small" onclick="location.href='#newwebhook'" type="button">Add a new webhook url</button>
            <button class="small" onclick="showWebhookList()" type="button">Show webhook urls</button>
        </div>
        </div>`;

    let cameras = await getCameras();
    cameras.forEach(showCameraListEntry);

    function showCameraListEntry(camera) {
        let section = document.createElement('section');
        section.onclick = function () {
            showCamera(camera)
        };

        section.innerHTML = `<div class="padded title clear">${camera.name}</div>`;
        container.appendChild(section);
    }

    function showCamera(camera) {
        container.innerHTML = '';

        let section;
        section = document.createElement('section');
        section.innerHTML = `<img alt="${camera.name}" src="/api/camera/${camera.id}.mjpg?token=${localStorage.token}">
        <div class="padded"><button onclick="deleteCamera(${camera.id})" type="button">Delete</button></div>
        <div class="padded title clear">${camera.name}</div><div class="padded">${camera.description}</div>`;
        container.appendChild(section);
    }
}

async function addCamera(form) {
    let camera = {
        'name': form.name.value,
        'description': form.description.value,
        'host': form.host.value,
        'port': parseInt(form.port.value)
    };
    let response = await fetch_secure('api/camera', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(camera)
    });

    if (response.status !== 200) {
        let data = await response.json();
        console.log(data.message);
    } else {
        showCameraList()
    }
}

async function deleteCamera(id) {
    let response = await fetch_secure(`api/camera/${id}`, {method: 'DELETE'});

    if (response.status === 200) {
        showCameraList()
    } else {
        console.log(await response.json().message);
    }
}