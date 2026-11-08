"""Regenerate only the vignette texture. Language and power JSON are maintained in src."""
import pathlib, math, struct, zlib
r = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources"

# Procedural UI gradient: fully transparent centre and smoothly shaded edges.
size=256
pixels=bytearray()
for y in range(size):
 pixels.append(0)
 for x in range(size):
  radius=math.sqrt(((x+.5)/size*2-1)**2+((y+.5)/size*2-1)**2)
  t=max(0,min(1,(radius-.25)/.7)); alpha=round(125*t*t*(3-2*t))
  pixels.extend((255,255,255,alpha))
def chunk(t,b):return struct.pack('>I',len(b))+t+b+struct.pack('>I',zlib.crc32(t+b))
png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',size,size,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
texture=r/'assets/poluten/textures/overlay/vignette.png';texture.parent.mkdir(parents=True,exist_ok=True);texture.write_bytes(png)
