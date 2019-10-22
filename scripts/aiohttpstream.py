import argparse

import picamera


def main():
    parser = argparse.ArgumentParser(description='Execute a streaming http server using picamera')
    parser.add_argument('-r', '--resolution', default='640x480', help='Resolution of the camera')
    parser.add_argument('-f', '--framerate', type=int, default=24, help='Framerate of the camera')
    parser.add_argument('-h', '--host', default='', help='The host of the streaming server. Use 0.0.0.0 to open to all')
    parser.add_argument('-p', '--port', type=int, default=8080, help='The port of the streaming server')
    args = parser.parse_args()

    with picamera.PiCamera(resolution=args.resolution, framerate=args.framerate) as camera:
        # TODO: define the streaming output
        camera.start_recording(output, format='mjpeg')
        address = (args.host, args.port)

        try:
        # TODO: setup server for streaming the output
        finally:
            camera.stop_recording()


if __name__ == '__main__':
    main()
