from pathlib import Path
from PIL import Image

paths = [
    Path(r"C:/Users/Wakin/Respondr/respondr-logo.png"),
    Path(r"C:/Users/Wakin/Respondr/app/src/main/res/drawable/respondr_logo.png"),
]

for p in paths:
    img = Image.open(p)
    rgba = img.convert("RGBA")
    w, h = rgba.size
    px = rgba.load()
    corners = [px[0,0], px[w-1,0], px[0,h-1], px[w-1,h-1]]
    alpha0 = sum(1 for y in range(h) for x in range(w) if px[x,y][3] == 0)
    total = w*h
    print("\n==", p)
    print("mode:", img.mode, "size:", img.size)
    print("corners RGBA:", corners)
    print("transparent pixels:", alpha0, f"({alpha0/total:.1%})")
