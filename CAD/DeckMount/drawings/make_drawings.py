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
STANDOFF, LIP_H = 6.0, 6.0
# trays (match tank_deck_mount.scad)
FRONT_OVH, REAR_OVH, TRAY_W, LAP_L, LAP_T = 62, 74, 116, 26.0, 1.5
IBT_POS = [(-FRONT_OVH + 6, (PW - TRAY_W) / 2 + 3), (-FRONT_OVH + 6, PW - (PW - TRAY_W) / 2 - 3 - 50)]

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
            "so they are approximate (±1 mm). Please measure the corner-hole spacing before printing.\n"
            "Shaded strips: where the tray tongues lie under the cradle. Which end is the front is assumed.",
            ha="center", va="top", fontsize=10, color="#495057")
    ax.text(-8, PW / 2, "FRONT edge (120)\nfront tray hooks on here", rotation=90, ha="center", va="center",
            fontsize=10, color=BLUE, weight="bold")
    ax.text(PL + 44, PW / 2 + 20, "REAR edge (120)\nrear tray hooks on here", rotation=90, ha="center",
            va="center", fontsize=10, color=BLUE, weight="bold")
    for x0 in (0, PL - 26):
        ax.add_patch(Rectangle((x0, 0), 26, PW, fc=BLUE, alpha=0.08, ec=BLUE, ls=":", lw=0.8))
    ax.set_title("Tank chassis mounting plate, top view (mm)", fontsize=15, loc="left", pad=14)
    ax.set_xlim(-30, PL + 62); ax.set_ylim(-58, PW + 30)
    ax.set_aspect("equal"); ax.axis("off")
    fig.savefig(os.path.join(OUT, "06_mounting_plate.png"), dpi=130, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def fit_drawing():
    """Top and side views of v5: battery in the middle, trays past the 120 mm ends."""
    fig = plt.figure(figsize=(17, 12.5))
    bx0, by0 = (PL - BL) / 2, (PW - BW) / 2
    ty0 = (PW - TRAY_W) / 2
    # ---------------- top view ------------------------------------------
    ax = fig.add_axes([0.02, 0.40, 0.96, 0.53])
    for x0, x1 in ((-FRONT_OVH, LAP_L), (PL - LAP_L, PL + REAR_OVH)):
        ax.add_patch(FancyBboxPatch((x0, ty0), x1 - x0, TRAY_W, boxstyle="round,pad=0,rounding_size=2",
                                    fc="#a5d8ff", ec=BLUE, lw=1.6, alpha=0.8))
    plate_outline(ax, fc="#e9ecef", cutouts=False)
    ax.add_patch(Rectangle((bx0 - 0.75 - END_WALL, by0 - 1 - WALL), BAY_L + 2 * END_WALL, BAY_W + 2 * WALL,
                           fc="none", ec=BLUE, lw=2.2))
    ax.add_patch(Rectangle((bx0, by0), BL, BW, fc=ORANGE, ec="k", alpha=0.55))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, (PW - AD_W) / 2), AD_L, AD_W, fc=YEL, ec="k", alpha=0.85))
    ax.text(PL / 2, PW / 2, "Power Wheel adapter\n84 x 75\non top of the P108\n(nothing above it)",
            ha="center", va="center", fontsize=10)
    for (x, y) in IBT_POS:
        ax.add_patch(Rectangle((x, y), 50, 50, fc="#495057", ec="k", alpha=0.85))
        ax.text(x + 25, y + 25, "IBT-2\n50 x 50\nheatsink up", color="white", ha="center", va="center", fontsize=9)
    ux, uy = PL + REAR_OVH - 3 - 68.6, ty0 + 3
    ax.add_patch(Rectangle((ux, uy), 68.6, 53.3, fc="#0b7285", ec="k", alpha=0.85))
    ax.text(ux + 34, uy + 26, "UNO R4 WiFi\n68.6 x 53.3", color="white", ha="center", va="center", fontsize=9)
    ax.annotate("USB-C faces\nthe rear", xy=(PL + REAR_OVH - 3, uy + 30), xytext=(PL + REAR_OVH + 6, uy + 26),
                fontsize=9, arrowprops=dict(arrowstyle="->", lw=0.8))
    gx, gy = PL + REAR_OVH - 10 - 43.2, PW - ty0 - 4 - 43.2
    ax.add_patch(Rectangle((gx, gy), 43.2, 43.2, fc=RED, ec="k", alpha=0.8))
    ax.text(gx + 21.6, gy + 21.6, "GPS\n43 x 43", color="white", ha="center", va="center", fontsize=9)
    for x in (0, PL):
        ax.plot([x, x], [-4, PW + 4], color="k", lw=1.2, ls="--")
    ax.text(-FRONT_OVH / 2, PW + 12, "FRONT TRAY", ha="center", fontsize=12, weight="bold", color=BLUE)
    ax.text(PL + REAR_OVH / 2, PW + 12, "REAR TRAY", ha="center", fontsize=12, weight="bold", color=BLUE)
    ax.text(PL / 2, PW + 12, "BATTERY CRADLE on the plate", ha="center", fontsize=12, weight="bold", color=BLUE)
    ax.annotate("", xy=(-FRONT_OVH - 22, PW / 2), xytext=(-FRONT_OVH - 4, PW / 2),
                arrowprops=dict(arrowstyle="-|>", lw=2.5, color="k"))
    ax.text(-FRONT_OVH - 14, PW / 2 + 7, "travel", ha="center", fontsize=10)
    dim(ax, (-FRONT_OVH, 0), (0, 0), "%d past the plate" % FRONT_OVH, off=10, side=-1)
    dim(ax, (0, 0), (PL, 0), "plate 140", off=10, side=-1)
    dim(ax, (PL, 0), (PL + REAR_OVH, 0), "%d past the plate" % REAR_OVH, off=10, side=-1)
    dim(ax, (-FRONT_OVH, -16), (PL + REAR_OVH, -16), "overall %d" % (FRONT_OVH + PL + REAR_OVH), off=12, side=-1)
    dim(ax, (PL + REAR_OVH, ty0), (PL + REAR_OVH, ty0 + TRAY_W), "tray %d" % TRAY_W, off=36, side=-1)
    dim(ax, (bx0 + BL, by0), (bx0 + BL, by0 + BW), "80", off=-9, side=-1)
    ax.text(LAP_L / 2 + 2, 14, "tongue\nunder", ha="center", fontsize=8, color=BLUE)
    ax.text(PL - LAP_L / 2 - 2, 14, "tongue\nunder", ha="center", fontsize=8, color=BLUE)
    ax.set_title("Top view (mm). Dashed lines are the plate's 120 mm front and rear edges.", fontsize=13, loc="left")
    ax.set_xlim(-FRONT_OVH - 30, PL + REAR_OVH + 50); ax.set_ylim(-48, PW + 22)
    ax.set_aspect("equal"); ax.axis("off")

    # ---------------- side view -----------------------------------------
    ax = fig.add_axes([0.02, 0.02, 0.62, 0.38])
    ax.add_patch(Rectangle((0, -2), PL, 2, fc="#adb5bd", ec="k"))
    ax.add_patch(Rectangle((LAP_L, 0), PL - 2 * LAP_L, FLOOR, fc=BLUE, ec="k", alpha=0.7))
    for x0 in (0, PL - LAP_L):
        ax.add_patch(Rectangle((x0, LAP_T), LAP_L, FLOOR - LAP_T, fc=BLUE, ec="k", alpha=0.7))
    ax.add_patch(Rectangle((-FRONT_OVH, 0), FRONT_OVH + LAP_L, LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((-FRONT_OVH, LAP_T), FRONT_OVH, FLOOR - LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((PL - LAP_L, 0), REAR_OVH + LAP_L, LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((PL, LAP_T), REAR_OVH, FLOOR - LAP_T, fc="#74c0fc", ec="k"))
    for x0, L in ((-FRONT_OVH, FRONT_OVH), (PL, REAR_OVH)):
        ax.add_patch(Rectangle((x0, FLOOR), L, LIP_H, fc="none", ec=BLUE, lw=1, ls=":"))
    ax.add_patch(Rectangle((bx0, FLOOR), BL, BODY_H, fc=ORANGE, ec="k", alpha=0.6))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, FLOOR + BODY_H), AD_L, 8, fc=YEL, ec="k"))
    ax.add_patch(Rectangle((PL / 2 - 22, FLOOR + BODY_H + 8), 44, AD_H - 8, fc=YEL, ec="k"))
    zb = FLOOR + STANDOFF
    ax.add_patch(Rectangle((-FRONT_OVH + 6, zb), 50, 43, fc="#495057", ec="k", alpha=0.85))
    ax.text(-FRONT_OVH + 31, zb + 21, "IBT-2", color="white", ha="center", va="center", fontsize=9)
    ax.add_patch(Rectangle((PL + REAR_OVH - 71.6, zb), 68.6, 12, fc="#0b7285", ec="k", alpha=0.85))
    ax.text(PL + REAR_OVH - 37, zb + 20, "UNO + GPS", ha="center", fontsize=9)
    ax.annotate("lift straight up", xy=(PL / 2, FLOOR + TOTAL_H + 26), xytext=(PL / 2, FLOOR + TOTAL_H + 2),
                ha="center", fontsize=10, arrowprops=dict(arrowstyle="-|>", lw=2, color="#2b8a3e"))
    dim(ax, (PL, FLOOR), (PL, FLOOR + TOTAL_H), "150", off=10, side=-1)
    dim(ax, (-FRONT_OVH, zb), (-FRONT_OVH, zb + 43), "≈43", off=8, fs=9)
    ax.text(PL / 2, -14, "tray tongue (1.5) slides under the cradle's stepped floor; the kit's corner screws clamp both",
            ha="center", fontsize=9, color="#495057")
    ax.set_title("Side view (mm), front on the left", fontsize=13, loc="left")
    ax.set_xlim(-FRONT_OVH - 22, PL + REAR_OVH + 30); ax.set_ylim(-22, FLOOR + TOTAL_H + 34)
    ax.set_aspect("equal"); ax.axis("off")

    ax = fig.add_axes([0.66, 0.02, 0.33, 0.36]); ax.axis("off")
    ax.text(0, 0.98, "Balance and clearance", fontsize=13, weight="bold", va="top")
    ax.text(0, 0.88,
            "Front: 2 x IBT-2, about 130 g, centre about 100 mm\n"
            "ahead of the plate centre.\n"
            "Rear: UNO + GPS, about 40 g, about 107 mm behind it.\n"
            "Battery + adapter (about 1 kg) stay centred.\n\n"
            "The mount's centre of mass ends up about 6 to 7 mm\n"
            "ahead of the plate centre. With the motors and tracks\n"
            "included it's smaller still, so you won't notice it.\n\n"
            "Please check on the real chassis:\n"
            "  1. Nothing sticks up past the 120 mm ends at plate\n"
            "     height (side frames, the front slope, the rear\n"
            "     rake). The trays sit 0 to 19 mm above the plate.\n"
            "  2. Which 120 mm end is the front.\n"
            "  3. The corner-hole spacing (about 116 x 106).\n"
            "  4. The battery length (cradle takes up to 136.5).",
            fontsize=10.5, va="top")
    fig.suptitle("How everything fits: battery in the middle, electronics past the front and rear edges",
                 fontsize=16, x=0.02, ha="left", y=0.995)
    fig.savefig(os.path.join(OUT, "07_layout.png"), dpi=115, bbox_inches="tight", facecolor="white")
    plt.close(fig)


if __name__ == "__main__":
    plate_drawing()
    fit_drawing()
    print("wrote", OUT)
