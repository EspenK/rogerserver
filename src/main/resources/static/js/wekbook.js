function showWebhookList() {
    location.href = '#webhook';
    waitForElementFirst('webhookContainer', 50, _showWebhook);
}

async function _showWebhook(container) {
    container.innerHTML = '';

    let webhooks = await getWebhooks();
    webhooks.forEach(showWebhookEntry);

    function showWebhookEntry(webhook) {
        let section = document.createElement('section');

        section.innerHTML = `
        <div class="padded">${webhook.url} | 
        <i onclick="deleteWebhook(${webhook.id})" class="red">delete</i></div>`;

        container.appendChild(section);
    }
}

async function addWebhook(form) {
    let webhook = {
        'url': form.url.value
    };
    let response = await fetch_secure('/webhooks', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(webhook)
    });

    if (response.status !== 200) {
        let data = await response.json();
        console.log(data.message);
    } else {
        showWebhookList();
    }
}

async function deleteWebhook(webhook_id) {
    let response = await fetch_secure(`/webhooks/${webhook_id}`, {method: 'DELETE'})

    if (response.status !== 200) {
        console.log(await response.json().message);
    } else {
        showWebhookList();
    }
}

async function getWebhooks() {
    let response = await fetch_secure('/webhooks', {method: 'GET'});

    if (response.status !== 200) {
        console.log(await response.json().message);
        return null;
    } else {
        return await response.json();
    }
}
