#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""生成氮气装甲 mod 的 16x16 物品贴图。"""
import os
from PIL import Image, ImageDraw

OUT = r"D:\dev\code\Offense Armer\src\main\resources\assets\offense_armer\textures\item"
os.makedirs(OUT, exist_ok=True)


def new_canvas():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def draw_flask(path, liquid, highlight=(255, 255, 255, 200), cork=(107, 74, 43, 255)):
    img = new_canvas()
    d = ImageDraw.Draw(img)
    glass = (208, 226, 240, 255)
    glass_dark = (150, 178, 196, 255)
    # 瓶塞
    d.rectangle([7, 1, 9, 3], fill=cork)
    # 瓶颈玻璃
    d.rectangle([7, 3, 9, 5], fill=glass)
    # 瓶身（圆底烧瓶）
    d.ellipse([3, 4, 12, 15], fill=glass, outline=glass_dark)
    # 液体（瓶身下半）
    d.ellipse([5, 9, 10, 14], fill=liquid)
    # 高光
    d.ellipse([4, 6, 6, 8], fill=highlight)
    img.save(path)


def draw_bottle(path, liquid, body=(150, 162, 178, 255), cap=(92, 98, 108, 255)):
    img = new_canvas()
    d = ImageDraw.Draw(img)
    # 瓶盖
    d.rectangle([5, 1, 11, 3], fill=cap)
    # 金属瓶身
    d.rounded_rectangle([4, 3, 12, 15], radius=2, fill=body)
    # 液氮观察窗
    d.rectangle([5, 6, 11, 12], fill=liquid)
    # 高光
    d.rectangle([5, 6, 6, 12], fill=(255, 255, 255, 160))
    # 瓶身描边
    d.rounded_rectangle([4, 3, 12, 15], radius=2, outline=(110, 120, 134, 255))
    img.save(path)


# 5 阶能力开发药，颜色逐级从浅白到深冰蓝
draw_flask(os.path.join(OUT, "ability_potion_1.png"), (235, 240, 248, 255))
draw_flask(os.path.join(OUT, "ability_potion_2.png"), (135, 195, 245, 255))
draw_flask(os.path.join(OUT, "ability_potion_3.png"), (95, 210, 235, 255))
draw_flask(os.path.join(OUT, "ability_potion_4.png"), (70, 140, 225, 255))
draw_flask(os.path.join(OUT, "ability_potion_5.png"), (205, 232, 255, 255))

# 液氮瓶
draw_bottle(os.path.join(OUT, "liquid_nitrogen_bottle.png"), (110, 200, 240, 255))

print("textures generated ->", OUT)
for f in sorted(os.listdir(OUT)):
    print("  ", f)
