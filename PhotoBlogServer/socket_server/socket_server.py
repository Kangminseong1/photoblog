import os
import socket
from datetime import datetime

HOST = "0.0.0.0"
PORT = 8000

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
REQUEST_DIR = os.path.join(BASE_DIR, "request")
RESPONSE_FILE = os.path.join(BASE_DIR, "response.bin")

# response.bin이 없으면 최소한의 HTTP 응답(빈 JSON 배열)을 자동 생성한다.
DEFAULT_RESPONSE = (
    b"HTTP/1.1 200 OK\r\n"
    b"Content-Type: application/json\r\n"
    b"Content-Length: 2\r\n"
    b"Connection: close\r\n"
    b"\r\n"
    b"[]"
)


def load_response():
    if not os.path.exists(RESPONSE_FILE):
        with open(RESPONSE_FILE, "wb") as f:
            f.write(DEFAULT_RESPONSE)
    with open(RESPONSE_FILE, "rb") as f:
        return f.read()


def main():
    os.makedirs(REQUEST_DIR, exist_ok=True)

    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((HOST, PORT))
    server.listen(5)
    print("Socket server listening on port %d (Ctrl+C to stop)" % PORT)

    try:
        while True:
            client, addr = server.accept()
            with client:
                data = client.recv(65535)
                name = datetime.now().strftime("%Y-%m-%d-%H-%M-%S") + ".bin"
                path = os.path.join(REQUEST_DIR, name)
                with open(path, "wb") as f:
                    f.write(data)
                print("Received %d bytes from %s -> request/%s" % (len(data), addr[0], name))
                client.sendall(load_response())
    except KeyboardInterrupt:
        print("\nServer stopped.")
    finally:
        server.close()


if __name__ == "__main__":
    main()
