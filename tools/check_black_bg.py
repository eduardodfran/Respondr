from pathlib import Path
from PIL import Image

p = Path(r"C:/Users/Wakin/Respondr/respondr-logo.png")
img = Image.open(p).convert("RGBA")
px = img.load()
w, h = img.size
opaque = 0
near_black = 0

for y in range(h):
    for x in range(w):
        r, g, b, a = px[x, y]
        if a > 0:
            opaque += 1
            if r < 20 and g < 20 and b < 20 and a > 200:
                near_black += 1

print("size", img.size)
print("opaque", opaque)
print("near_black", near_black)
print("near_black%", (near_black / opaque if opaque else 0.0))
