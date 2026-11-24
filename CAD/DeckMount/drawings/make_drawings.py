"""Dimensioned 2-D drawings of the tank mounting plate and the battery fit.

Run:  python3 make_drawings.py   (needs matplotlib)  ->  writes PNGs next to this file.
All numbers mirror the variables in ../tank_deck_mount.scad.
"""
import os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle, Circle

OUT = os.path.dirname(os.path.abspath(__file__))

# --- plate (John, 2026-11-24) -------------------------------------------
PL, PW = 140.0, 120.0            # X = 140 side, Y = 120 side
# Corner holes and cut-outs are traced from Images/BasePlate.png and scaled
# to John's 140 x 120 outline, so they are approximate.
SX, SY = PL / 151.6, PW / 135.3
HX = [13.0 * SX, 138.8 * SX]
HY = [7.9 * SY, 127.3 * SY]
HOLE_D = 3.4
# scan blobs: (cx, cy, w, h) in scan mm, y measured down from the top edge
SCAN = [(49.4, 6.8, 51.0, 2.9), (104.6, 6.8, 45.6, 2.8), (106.7, 20.2, 21.9, 2.7),
        (141.9, 41.2, 4.0, 42.6), (28.0, 67.6, 4.2, 85.0), (71.4, 67.8, 4.2, 85.3),
        (51.1, 33.7, 34.1, 2.8), (107.4, 33.7, 51.1, 2.7), (118.4, 50.3, 3.4, 19.4),
        (88.1, 67.4, 20.7, 51.7), (59.6, 47.8, 15.6, 6.3), (60.0, 53.3, 16.3, 2.6),
        (109.8, 68.2, 12.0, 2.7), (57.8, 68.3, 12.0, 2.6), (12.4, 71.8, 6.1, 6.0),
        (143.3, 93.8, 4.0, 42.6), (120.7, 75.3, 20.7, 2.6), (52.2, 75.5, 25.2, 2.7),
        (112.6, 80.5, 17.7, 2.7), (52.2, 81.2, 25.2, 2.8), (60.0, 95.3, 16.3, 2.6),
        (60.2, 101.5, 16.7, 6.0), (30.6, 104.2, 6.3, 5.9), (114.3, 113.7, 36.1, 13.5),
        (60.0, 123.0, 16.2, 2.7), (49.4, 128.6, 51.1, 2.8), (104.6, 128.6, 45.5, 2.7)]

# --- battery + adapter ----------------------------------------------------
BL, BW = 135.0, 80.0             # P108 body footprint (Archive/Battery+VR.f3d)
TOTAL_H = 150.0                  # battery + adapter, measured by John
AD_L, AD_W, AD_H = 84.0, 75.0, 90.0   # Power Wheel adapter (listing photo)
BODY_H = TOTAL_H - AD_H          # battery top surface (where the adapter base sits)
FLOOR = 3.0                      # cradle floor
WALL, WALL_H, END_WALL, END_H = 2.4, 22.0, 1.6, 12.0
BAY_L, BAY_W = BL + 1.5, BW + 2.0
POST_XS, POST_X, POST_Y = [35.0, 105.0], 14.0, 9.6
GAP_TOP, DECK_T = 20.0, 3.0

BLUE, GREY, ORANGE, YEL, RED = "#3b5bdb", "#868e96", "#f08c00", "#c0d000", "#e03131"


def dim(ax, p0, p1, text, off=8, side=1, fs=10, color="k"):
    """Dimension line between p0 and p1, offset perpendicular by off."""
    (x0, y0), (x1, y1) = p0, p1
    dx, dy = x1 - x0, y1 - y0
    L = (dx * dx + dy * dy) ** 0.5
    nx, ny = -dy / L * side, dx / L * side
    a = (x0 + nx * off, y0 + ny * off)
    b = (x1 + nx * off, y1 + ny * off)
    ax.plot([x0, a[0]], [y0, a[1]], color=color, lw=0.6)
    ax.plot([x1, b[0]], [y1, b[1]], color=color, lw=0.6)
    ax.annotate("", xy=a, xytext=b, arrowprops=dict(arrowstyle="<->", lw=0.9, color=color))
    mx, my = (a[0] + b[0]) / 2 + nx * 3.5, (a[1] + b[1]) / 2 + ny * 3.5
    rot = 0 if abs(dx) >= abs(dy) else 90
    ax.text(mx, my, text, ha="center", va="center", fontsize=fs, rotation=rot, color=color,
            bbox=dict(fc="white", ec="none", pad=0.6))


def plate_outline(ax, x0=0.0, y0=0.0, fc="#dee2e6", cutouts=True):
    ax.add_patch(FancyBboxPatch((x0, y0), PL, PW, boxstyle="round,pad=0,rounding_size=4",
                                fc=fc, ec="k", lw=1.2))
    if cutouts:
        for cx, cy, w, h in SCAN:
            X, Y, W, H = cx * SX, (135.3 - cy) * SY, w * SX, h * SY
            ax.add_patch(FancyBboxPatch((x0 + X - W / 2, y0 + Y - H / 2), W, H,
                                        boxstyle="round,pad=0,rounding_size=%.2f" % (min(W, H) / 2),
                                        fc="white", ec="#adb5bd", lw=0.6))
    for hx in HX:
        for hy in HY:
            ax.add_patch(Circle((x0 + hx, y0 + hy), HOLE_D / 2, fc="white", ec="k", lw=1))


def plate_drawing():
    fig, ax = plt.subplots(figsize=(11, 9.6))
    plate_outline(ax)
    dim(ax, (0, 0), (PL, 0), "140", off=16, side=-1, fs=13)
    dim(ax, (0, 0), (0, PW), "120", off=16, side=1, fs=13)
    dim(ax, (HX[0], HY[1]), (HX[1], HY[1]), "%.1f  (≈, confirm)" % (HX[1] - HX[0]), off=14, fs=10)
    dim(ax, (HX[1], HY[0]), (HX[1], HY[1]), "%.1f  (≈, confirm)" % (HY[1] - HY[0]), off=16, side=-1, fs=10)
    dim(ax, (0, HY[1]), (HX[0], HY[1]), "%.1f" % HX[0], off=6, fs=9)
    dim(ax, (HX[0], PW), (HX[0], HY[1]), "%.1f" % (PW - HY[1]), off=14, side=-1, fs=9)
    ax.annotate("4 x corner holes, Ø3.4 (M3)\nplate screws to the kit standoffs here",
                xy=(HX[1], HY[0]), xytext=(PL + 8, -14), fontsize=10,
                arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.text(PL / 2, -34, "Outline 140 x 120 mm as measured by John.\n"
            "Cut-outs and hole positions are traced from Images/BasePlate.png and scaled to fit,\n"
            "so they are approximate (±1 mm). Please measure the corner-hole spacing before printing.",
            ha="center", va="top", fontsize=10, color="#495057")
    ax.set_title("Tank chassis mounting plate, top view (mm)", fontsize=15, loc="left", pad=14)
    ax.set_xlim(-30, PL + 62); ax.set_ylim(-58, PW + 30)
    ax.set_aspect("equal"); ax.axis("off")
    fig.savefig(os.path.join(OUT, "07_mounting_plate.png"), dpi=130, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def fit_drawing():
    fig = plt.figure(figsize=(16, 11.5))
    # ---------------- top view ------------------------------------------
    ax = fig.add_axes([0.02, 0.36, 0.48, 0.58])
    plate_outline(ax, fc="#e9ecef", cutouts=False)
    bx0, by0 = (PL - BL) / 2, (PW - BW) / 2
    # cradle walls
    ax.add_patch(Rectangle((bx0 - 0.75 - END_WALL, by0 - 1 - WALL), BAY_L + 2 * END_WALL, BAY_W + 2 * WALL,
                           fc="none", ec=BLUE, lw=2.2, ls="-"))
    ax.add_patch(Rectangle((bx0, by0), BL, BW, fc=ORANGE, ec="k", alpha=0.55, lw=1))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, (PW - AD_W) / 2), AD_L, AD_W, fc=YEL, ec="k", alpha=0.85, lw=1))
    for px in POST_XS:
        for py in (1.2, PW - 1.2 - POST_Y):
            ax.add_patch(Rectangle((px - POST_X / 2, py), POST_X, POST_Y, fc=GREY, ec="k", lw=0.8))
    ax.text(PL / 2, PW / 2, "Power Wheel adapter\n84 x 75 base\n(sits on top of the battery)",
            ha="center", va="center", fontsize=10)
    ax.text(bx0 + 12, PW / 2, "P108 battery\n135 x 80", ha="center", va="center", fontsize=10, rotation=90)
    ax.text(PL / 2, PW - 8, "latch side", ha="center", fontsize=9, color="#495057")
    ax.text(PL / 2, 4, "hinge side", ha="center", fontsize=9, color="#495057")
    dim(ax, (0, 0), (PL, 0), "plate 140", off=12, side=-1)
    dim(ax, (bx0, PW), (bx0 + BL, PW), "battery 135", off=10)
    dim(ax, (PL, 0), (PL, PW), "plate 120", off=12, side=-1)
    dim(ax, (bx0, by0), (bx0, by0 + BW), "80", off=7)
    dim(ax, (bx0 + BL, 0), (bx0 + BL, by0), "20", off=-8, side=-1, fs=9)
    ax.text(PL / 2, -26, "Battery runs along the 140 side: 2.5 mm spare at each end, 20 mm each side.\n"
            "Grey = deck posts (hinge side at the bottom, latch side at the top).",
            ha="center", va="top", fontsize=10, color="#495057")
    ax.set_title("Top view: fits with no overhang", fontsize=14, loc="left", color="#2b8a3e")
    ax.set_xlim(-14, PL + 30); ax.set_ylim(-46, PW + 30); ax.set_aspect("equal"); ax.axis("off")

    # ---------------- side view (looking at the 140 side) ----------------
    ax = fig.add_axes([0.53, 0.36, 0.46, 0.58])
    deck_z = FLOOR + TOTAL_H + GAP_TOP
    ax.add_patch(Rectangle((0, -2), PL, 2, fc="#adb5bd", ec="k"))
    ax.add_patch(Rectangle((0, 0), PL, FLOOR, fc=BLUE, ec="k", alpha=0.7))
    ax.add_patch(Rectangle(((PL - BL) / 2, FLOOR), BL, BODY_H, fc=ORANGE, ec="k", alpha=0.6))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, FLOOR + BODY_H), AD_L, 8, fc=YEL, ec="k"))
    ax.add_patch(Rectangle((PL / 2 - 22, FLOOR + BODY_H + 8), 44, AD_H - 8, fc=YEL, ec="k"))
    ax.add_patch(Rectangle(((PL - BAY_L) / 2 - END_WALL, FLOOR), END_WALL, END_H, fc=BLUE, ec="k"))
    ax.add_patch(Rectangle(((PL + BAY_L) / 2, FLOOR), END_WALL, END_H, fc=BLUE, ec="k"))
    for px in POST_XS:
        ax.add_patch(Rectangle((px - POST_X / 2, FLOOR), POST_X, deck_z - FLOOR, fc=GREY, ec="k", alpha=0.35))
    ax.add_patch(Rectangle((0, deck_z), PL, DECK_T, fc="#a5d8ff", ec="k"))
    ax.plot([PL / 2 + 22, PL / 2 + 40, PL / 2 + 40], [FLOOR + TOTAL_H - 4, FLOOR + TOTAL_H - 4, deck_z - 2],
            color=RED, lw=2)
    ax.text(PL / 2 + 42, FLOOR + TOTAL_H + 6, "adapter leads\n(250 mm)", fontsize=9, color=RED)
    dim(ax, (PL, FLOOR), (PL, FLOOR + TOTAL_H), "150 battery + adapter", off=14, side=-1)
    dim(ax, (0, FLOOR + BODY_H), (0, FLOOR + TOTAL_H), "90 adapter", off=12)
    dim(ax, (0, FLOOR), (0, FLOOR + BODY_H), "≈60", off=12)
    dim(ax, (PL, FLOOR + TOTAL_H), (PL, deck_z), "20 cable space", off=14, side=-1, fs=9)
    ax.text(PL / 2, deck_z + 9, "hinged electronics deck", ha="center", fontsize=10)
    ax.text(POST_XS[0], deck_z - 14, "posts\n(at the\nplate edges)", ha="center", va="top", fontsize=8, color="#495057")
    ax.text(PL / 2, -12, "plate + 3 mm cradle floor", ha="center", fontsize=9, color="#495057")
    ax.set_title("Side view (mm)", fontsize=14, loc="left")
    ax.set_xlim(-40, PL + 75); ax.set_ylim(-20, deck_z + 22); ax.set_aspect("equal"); ax.axis("off")

    # ---------------- the other way round: overhang ----------------------
    ax = fig.add_axes([0.02, 0.02, 0.48, 0.31])
    plate_outline(ax, fc="#e9ecef", cutouts=False)
    bx0, by0 = (PL - BW) / 2, (PW - BL) / 2
    ax.add_patch(Rectangle((bx0, by0), BW, BL, fc=ORANGE, ec="k", alpha=0.45))
    for yy in (by0, PW):
        ax.add_patch(Rectangle((bx0, yy), BW, 7.5, fc=RED, ec=RED, alpha=0.6, hatch="///"))
    ax.text(bx0 + BW + 6, PW + 2, "7.5 overhang", color=RED, fontsize=10)
    ax.text(bx0 + BW + 6, by0 + 1, "7.5 overhang", color=RED, fontsize=10)
    ax.set_title("Turned 90°: battery overhangs the 120 side by 7.5 mm each end", fontsize=12,
                 loc="left", color=RED)
    ax.set_xlim(-10, PL + 60); ax.set_ylim(by0 - 6, PW + 14); ax.set_aspect("equal"); ax.axis("off")

    ax = fig.add_axes([0.53, 0.02, 0.46, 0.31]); ax.axis("off")
    ax.text(0, 0.95, "If the battery turns out longer than 136 mm", fontsize=13, weight="bold", va="top")
    ax.text(0, 0.80,
            "The plate fits a battery up to about 136.5 mm long (140 minus the 1.6 mm end walls).\n"
            "Options if your P108 is longer:\n\n"
            "  A.  Drop the two low end walls. The side walls still locate it,\n"
            "       and it fits up to 140 mm.\n"
            "  B.  Let it overhang the ends by a few mm. Nothing is in the way\n"
            "       at deck-plate height, but it has to clear the tracks.\n"
            "  C.  Turn it 90° (left). That needs a 15 mm wider cradle with\n"
            "       ledges past the plate edge, so it's the last resort.\n\n"
            "Home Depot's 7.16 x 3.39 x 6.5 in is the packaging, not the battery.\n"
            "135 x 80 comes from your Battery+VR model. A ruler check of the\n"
            "battery's length is the one number to confirm.",
            fontsize=10.5, va="top", family="DejaVu Sans")
    fig.suptitle("How the Ryobi P108 battery and Power Wheel adapter fit on the 140 x 120 plate",
                 fontsize=16, x=0.02, ha="left", y=0.995)
    fig.savefig(os.path.join(OUT, "08_battery_fit.png"), dpi=120, bbox_inches="tight", facecolor="white")
    plt.close(fig)


if __name__ == "__main__":
    plate_drawing()
    fit_drawing()
    print("wrote", OUT)
