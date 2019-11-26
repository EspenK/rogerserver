import asyncio

import argparse
import gpiozero
import io
import picamera
from aiohttp import web, MultipartWriter
from threading import Thread

routes = web.RouteTableDef()


@routes.get('/stream')
async def show_stream(request, state):
    return web.Response(text='<img src="stream.mjpg">', content_type='text/html')


async def write_to_stream(frame, response):
    with MultipartWriter('image/jpeg', boundary='FRAME') as writer:
        writer.append(frame, {
            'Content-Type': 'image/jpeg',
            'Content-Length': len(frame)
        })

        await writer.write(response, close_boundary=False)


@routes.get("/stream.mjpg")
async def stream_handler(request, state):
    log(f'OPEN: Connection from {request.remote}')

    response = web.StreamResponse(
        status=200,
        headers={'Content-Type': f'multipart/x-mixed-replace; boundary=FRAME'}
    )
    await response.prepare(request)

    try:
        while True:
            frame = await grab_frame(state)
            await write_to_stream(frame, response)
    finally:
        await response.write_eof()
        log(f'CLOSE: Connection from {request.remote}')


def log(*args, sep=''):
    print(*args, sep=sep)


def capture(camera, state):
    log('starting camera capture')
    stream = io.BytesIO()

    for _ in camera.capture_continuous(stream, format="jpeg", use_video_port=True):
        if state['closed']:
            return

        state['frame'] = stream.getvalue()
        state['event'].clear()
        state['event'].set()

        stream.truncate(0)
        stream.seek(0)


async def alert(state):
    state["alerting"] = True

    while state["alerting"]:
        await state['buzz']()


@routes.get("/alert")
async def start_alerting(request, state):
    print("Alerting!")
    asyncio.create_task(alert(state))
    return web.Response(status=200)


@routes.get("/stop")
async def stop_alerting(request, state):
    print("Stopping alert!")
    state["alerting"] = False
    return web.Response(status=200)


async def grab_frame(state):
    await state['event'].wait()
    return state['frame']


def create_buzzer(pin, frequency, duration, cycle_duration):
    print(f"Opening buzzer on pin {pin}")
    buzzer = gpiozero.TonalBuzzer(pin, mid_tone=1024)

    async def buzz():
        buzzer.play(frequency)
        await asyncio.sleep(duration / 1000)
        buzzer.stop()
        await asyncio.sleep((cycle_duration - duration) / 1000)

    return buzz


async def main():
    parser = argparse.ArgumentParser(description='Execute a streaming http server using picamera')
    parser.add_argument('-r', '--resolution', default='640x480', help='Resolution of the camera')
    parser.add_argument('-f', '--framerate', type=int, default=24, help='Framerate of the camera')
    parser.add_argument('-a', '--address', default='0.0.0.0',
                        help='The host of the streaming server. Use 0.0.0.0 to open to all')
    parser.add_argument('-p', '--port', type=int, default=8080, help='The port of the streaming server')
    parser.add_argument('--rotation', type=int, default=180, help='Rotation of the camera in degrees')
    parser.add_argument("--frequency", type=int, default=1024, help="Frequency of the beeps in Hz")
    parser.add_argument("--duration", type=int, default=200, help="Duration of the beeps in milliseconds")
    parser.add_argument("--cycle-duration", type=int, default=1600,
                        help="The time the alert system sleeps between each beep")
    parser.add_argument("pin", type=int,
                        help="The GPIO pin number following https://gpiozero.readthedocs.io/en/stable/recipes.html#pin-numbering")
    args = parser.parse_args()

    loop = asyncio.get_running_loop()
    state = {
        'frame': None,
        'event': asyncio.Event(),
        'closed': False,
        'alerting': False,
        'buzz': create_buzzer(args.pin, args.frequency, args.duration, args.cycle_duration)
    }

    @web.middleware
    async def add_state(request, handler):
        return await handler(request, state)

    app = web.Application(middlewares=[add_state])
    app.add_routes(routes)

    with picamera.PiCamera(resolution=args.resolution, framerate=args.framerate) as camera:
        camera.rotation = args.rotation

        # Warm up the camera
        camera.start_preview()
        await asyncio.sleep(2)

        # loop.run_in_executor(None, capture, camera, state)
        capture_thread = Thread(target=capture, args=(camera, state))
        capture_thread.daemon = True
        capture_thread.start()

        runner = web.AppRunner(app)
        await runner.setup()

        log(f'starting site at {args.address}:{args.port}')
        site = web.TCPSite(runner, args.address, args.port)
        await site.start()

        try:
            while True:
                await asyncio.sleep(3600)
        finally:
            log('Cleaning up')
            state['closed'] = True
            await runner.cleanup()
            camera.close()


if __name__ == '__main__':
    asyncio.run(main())
