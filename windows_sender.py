"""Kablosuz Ekran Windows 10/11 sender. pip install pillow mss
Run: python windows_sender.py
Windows Firewall: allow TCP 48621 and UDP broadcast 48620 on Private networks.
This is mirror-only; extend requires a Windows indirect display driver.
"""
import io, socket, struct, threading, time
from PIL import Image
import mss

PORT=48621
clients=set()
lock=threading.Lock()
def accept_loop(server):
    while True:
        sock,addr=server.accept()
        sock.settimeout(3)
        with lock: clients.add(sock)
        print("Connected:",addr)
def discovery():
    s=socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET,socket.SO_BROADCAST,1)
    while True:
        try: s.sendto(b"KABLOSUZ_EKRAN_V1:48621",("255.255.255.255",48620))
        except OSError: pass
        time.sleep(2)
def main():
    server=socket.socket()
    server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
    server.bind(("0.0.0.0",PORT))
    server.listen(32)
    threading.Thread(target=accept_loop,args=(server,),daemon=True).start()
    threading.Thread(target=discovery,daemon=True).start()
    with mss.mss() as screen:
        monitor=screen.monitors[1]
        while True:
            start=time.monotonic()
            frame=screen.grab(monitor)
            img=Image.frombytes("RGB",frame.size,frame.rgb)
            img.thumbnail((1280,720))
            output=io.BytesIO()
            img.save(output,format="JPEG",quality=65)
            data=output.getvalue()
            packet=struct.pack(">I",len(data))+data
            with lock:
                for client in list(clients):
                    try: client.sendall(packet)
                    except OSError:
                        clients.discard(client)
                        client.close()
            time.sleep(max(0,0.1-(time.monotonic()-start)))
if __name__=="__main__": main()
