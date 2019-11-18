import asyncio

import argparse
import gpiozero
from aiohttp import web


def main():
    parser = argparse.ArgumentParser(description="Start a HTTP server for activating the alert system")
    parser.add_argument('-a', '--address', default=None,
                        help='The host of the buzzer server. Use 0.0.0.0 to open to all')
    parser.add_argument("-p", "--port", type=int, default=8081, help="The port to serve the socket on")
    parser.add_argument("--frequency", type=int, default=1024, help="Frequency of the beeps in Hz")
    parser.add_argument("--duration", type=int, default=200, help="Duration of the beeps in milliseconds")
    parser.add_argument("--cycle-duration", type=int, default=1600,
                        help="The time the alert system sleeps between each beep")
    parser.add_argument("pin", type=int,
                        help="The GPIO pin number following https://gpiozero.readthedocs.io/en/stable/recipes.html#pin-numbering")
    args = parser.parse_args()

    print(f"Opening buzzer on pin {args.pin}")
    buzzer = gpiozero.TonalBuzzer(args.pin, mid_tone=1024)
    routes = web.RouteTableDef()
    state = {"alerting": False}

    async def alert():
        state["alerting"] = True

        while state["alerting"]:
            buzzer.play(args.frequency)
            await asyncio.sleep(args.duration / 1000)
            buzzer.stop()
            await asyncio.sleep((args.cycle_duration - args.duration) / 1000)

    @routes.get("/alert")
    async def start_alerting(request):
        print("Alerting!")
        asyncio.create_task(alert())
        return web.Response(status=200)

    @routes.get("/stop")
    async def stop_alerting(request):
        print("Stopping alert!")
        state["alerting"] = False
        return web.Response(status=200)

    app = web.Application()
    app.add_routes(routes)

    try:
        web.run_app(app, host=args.address, port=args.port)
    finally:
        print("Closing buzzer")
        buzzer.close()


if __name__ == '__main__':
    main()
