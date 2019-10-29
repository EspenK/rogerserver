import time

import argparse
import io
import picamera
import threading
from aiohttp import web, MultipartWriter


class StreamingOutput:
    def __init__(self):
        self.frame = None
        self.buffer = io.BytesIO()
        self.condition = threading.Condition()

    def write(self, buf):
        if buf.startswith(b'\xff\xd8'):
            # New frame, copy the existing buffer's content and notify all
            # clients it's available
            self.buffer.truncate()
            with self.condition:
                self.frame = self.buffer.getvalue()
                self.condition.notify_all()
            self.buffer.seek(0)
        return self.buffer.write(buf)


def log(*args, sep=''):
    print(*args, sep=sep)


routes = web.RouteTableDef()
stream = StreamingOutput()


@routes.get('/stream')
async def show_stream(request):
    return web.Response(
        text='<img src="stream.mjpg">',
        content_type='text/html'
    )


@routes.get("/stream.mjpg")
async def stream_handler(request):
    log(f'OPEN: Connection from {request.remote}')

    boundary = 'FRAME'
    response = web.StreamResponse(
        status=200,
        headers={
            'Age': '0',
            'Cache-Control': 'no-cache, private',
            'Pragma': 'no-cache',
            'Content-Type': f'multipart/x-mixed-replace; boundary={boundary}'
        }
    )
    await response.prepare(request)

    try:
        while True:
            with stream.condition:
                stream.condition.wait()
                frame = stream.frame

            with MultipartWriter('image/jpeg', boundary=boundary) as writer:
                writer.append(frame, {
                    'Content-Type': 'image/jpeg',
                    'Content-Length': len(frame)
                })

                await writer.write(response, close_boundary=False)
    finally:
        await response.write_eof()
        log(f'CLOSE: Connection from {request.remote}')


def main():
    parser = argparse.ArgumentParser(description='Execute a streaming http server using picamera')
    parser.add_argument('-r', '--resolution', default='640x480', help='Resolution of the camera')
    parser.add_argument('-f', '--framerate', type=int, default=24, help='Framerate of the camera')
    parser.add_argument('-a', '--address', default=None,
                        help='The host of the streaming server. Use 0.0.0.0 to open to all')
    parser.add_argument('-p', '--port', type=int, default=8080, help='The port of the streaming server')
    parser.add_argument('--rotation', type=int, default=180, help='Rotation of the camera in degrees')
    args = parser.parse_args()

    with picamera.PiCamera(resolution=args.resolution, framerate=args.framerate) as camera:
        camera.rotation = args.rotation

        # Warm up the camera
        camera.start_preview()
        time.sleep(2)

        camera.start_recording(stream, format='mjpeg')

        app = web.Application()
        app.add_routes(routes)

        try:
            web.run_app(app, host=args.address, port=args.port)
        finally:
            log('Cleaning up')
            camera.stop_recording()


if __name__ == '__main__':
    main()
