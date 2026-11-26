"""Where every standoff is, how tall, and which screw (positions echoed from tank_deck_mount.scad)."""
import os
import numpy as np, trimesh
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
from matplotlib.patches import Rectangle, RegularPolygon, Circle
D = os.path.dirname(os.path.abspath(__file__)); S = os.path.join(D, "..", "stl"); OUT = os.path.join(D, "..", "images")
PL, PW = 152, 135
IBT = [[-51, 18], [-51, 58], [-11, 18], [-11, 58], [-51, 77], [-51, 117], [-11, 77], [-11, 117]]
UNO = [[212.03, 15.54], [210.76, 63.8], [159.96, 20.62], [159.96, 48.56]]
GPS = [[217.45, 77.85], [217.45, 115.95], [179.35, 77.85], [179.35, 115.95]]
CORNER = [(x, y) for x in (13.5, 138.5) for y in (7.5, 127.5)]

def outline(ax, f, mirror=False, dx=0, col="#dee2e6"):
    m = trimesh.load(os.path.join(S, f))
    t = m.triangles[:, :, :2].copy()
    if mirror: t[:, :, 0] = -t[:, :, 0]
    t[:, :, 0] += dx
    ax.add_collection(PolyCollection(t, fc=col, ec=col, lw=0.1, zorder=1))

fig = plt.figure(figsize=(20, 12))
ax = fig.add_axes([0.02, 0.30, 0.96, 0.64])
outline(ax, "deck_mount_front_tray.stl", col="#c5d0fa")
outline(ax, "deck_mount_base.stl", col="#e9ecef")
outline(ax, "deck_mount_rear_tray.stl", mirror=True, dx=PL, col="#c5d0fa")
ax.add_patch(Rectangle((0, 0), PL, PW, fill=False, ec="k", lw=1.2, ls="--", zorder=2))
GREEN, ORANGE, RED, GREY = "#2f9e44", "#e8590c", "#c92a2a", "#495057"
for p in IBT: ax.add_patch(Circle(p, 4, fc=ORANGE, ec="k", lw=1, zorder=5))
for p in UNO: ax.add_patch(Circle(p, 3.25, fc=GREEN, ec="k", lw=1, zorder=5))
for p in GPS: ax.add_patch(Circle(p, 3.5, fc=RED, ec="k", lw=1, zorder=5))
for p in CORNER: ax.add_patch(RegularPolygon(p, 6, radius=4.5, fc=GREY, ec="k", lw=1, zorder=5))
# coordinates of every point
for pts, col in ((IBT, ORANGE), (UNO, GREEN), (GPS, RED)):
    for x, y in pts:
        ax.text(x, y - 6.5, "%.0f, %.0f" % (x, y), ha="center", va="top", fontsize=8.5, color=col, zorder=6,
                bbox=dict(fc="white", ec="none", pad=0.3, alpha=0.8))
for x, y in CORNER:
    ax.text(x + 7, y, "%.1f, %.1f" % (x, y) if x < 60 else "", ha="left", va="center", fontsize=8.5, color=GREY, zorder=6)
    if x > 60: ax.text(x - 7, y, "%.1f, %.1f" % (x, y), ha="right", va="center", fontsize=8.5, color=GREY, zorder=6)
ax.text(-31, PW + 8, "FRONT TRAY\n2x IBT-2", ha="center", fontsize=13, fontweight="bold", color=ORANGE)
ax.text(PL / 2, PW + 8, "BASE on the metal plate (dashed)", ha="center", fontsize=13, fontweight="bold", color=GREY)
ax.text(200, PW + 8, "REAR TRAY\nUNO + GPS", ha="center", fontsize=13, fontweight="bold", color="#2b8a3e")
ax.annotate("", xy=(-75, PW / 2), xytext=(-62, PW / 2), arrowprops=dict(arrowstyle="-|>", lw=2))
ax.text(-75, PW / 2 + 4, "front", ha="center", fontsize=11)
ax.text(PL / 2, -14, "Positions in mm: first number from the plate's FRONT edge (negative = in front of it), second from the bottom long edge.",
        ha="center", fontsize=11, color="#333")
ax.set_xlim(-82, 240); ax.set_ylim(-22, PW + 26); ax.set_aspect("equal"); ax.axis("off")
fig.text(0.02, 0.965, "Standoffs: where they go, how tall, which screw (top view)", fontsize=20, fontweight="bold")

leg = fig.add_axes([0.02, 0.02, 0.58, 0.26]); leg.axis("off")
rows = [(ORANGE, "o", "IBT-2 drivers (8)", "Printed into the front tray. 11 mm tall above the 5 mm tray floor. Slotted so the holes line up.", "M3 x 20 down through board, standoff and floor, nut underneath"),
        (GREEN, "o", "UNO R4 (4)", "Printed into the rear tray. 11 mm tall, round.", "M3 x 20 + nut underneath"),
        (RED, "o", "GPS (4)", "Printed into the rear tray. 11 mm tall, slotted.", "M3 x 20 + nut underneath"),
        (GREY, "h", "Chassis corner standoffs (4)", "The kit's own metal standoffs under the plate. Nothing to buy or print; keep them as they are.", "Kit screw 4 mm longer (through tray tongue + base + plate)")]
for i, (c, mk, name, what, screw) in enumerate(rows):
    y = 0.85 - i * 0.25
    leg.plot([0.01], [y], marker=mk, ms=16, mfc=c, mec="k", transform=leg.transAxes)
    leg.text(0.04, y + 0.04, name, fontsize=13, fontweight="bold", transform=leg.transAxes, va="center")
    leg.text(0.04, y - 0.06, what, fontsize=11, transform=leg.transAxes, va="center")
    leg.text(0.04, y - 0.14, screw, fontsize=11, transform=leg.transAxes, va="center", color="#c92a2a")

# side view of one board standoff stack
sx = fig.add_axes([0.64, 0.02, 0.34, 0.27]); sx.axis("off")
sx.add_patch(Rectangle((0, 0), 60, 5, fc="#c5d0fa", ec="k"))                 # tray floor
sx.add_patch(Rectangle((24, 5), 12, 11, fc="#c5d0fa", ec="k"))               # standoff
sx.add_patch(Rectangle((5, 16), 50, 1.6, fc="#5c940d", ec="k"))              # board
sx.add_patch(Rectangle((28.5, -2.4), 3, 22.4 - 2.4 + 2.4, fc="#adb5bd", ec="k", alpha=0.6))   # screw shank
sx.add_patch(Rectangle((26.5, 17.6), 7, 2.2, fc="#868e96", ec="k"))          # head
sx.add_patch(Rectangle((26.5, -2.4), 7, 2.4, fc="#495057", ec="k"))          # nut
for y0, y1, t in ((0, 5, "tray floor 5"), (5, 16, "standoff 11"), (16, 17.6, "board 1.6")):
    sx.annotate("", xy=(64, y0), xytext=(64, y1), arrowprops=dict(arrowstyle="<->", lw=0.8))
    sx.text(66, (y0 + y1) / 2, t, va="center", fontsize=10)
sx.text(37, -4.5, "M3 nut", fontsize=10, va="top")
sx.text(37, 21, "M3 x 20 screw head", fontsize=10)
sx.set_title("Side view of one board standoff (mm)", fontsize=12, loc="left")
sx.set_xlim(-2, 95); sx.set_ylim(-8, 26); sx.set_aspect("equal")
fig.savefig(os.path.join(OUT, "20_standoffs.png"), dpi=100, bbox_inches="tight", facecolor="white")
print("ok")
