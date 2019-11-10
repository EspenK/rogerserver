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
        location.href = '#camera';
    }
}

async function deleteWebhook(webhook_id) {
    let response = await fetch_secure(`/webhooks/${webhook_id}`, {method: 'DELETE'})

    if (response.status !== 200) {
        console.log(await response.json().message);
    } else {
        location.href = '#camera';
    }
}
