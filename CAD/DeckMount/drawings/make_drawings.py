"""Dimensioned 2-D drawings of the tank mounting plate and the battery fit.

Run:  python3 make_drawings.py   (needs matplotlib)  ->  writes PNGs next to this file.
All numbers mirror the variables in ../tank_deck_mount.scad.
"""
import os
import numpy as np
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle, Circle

OUT = os.path.dirname(os.path.abspath(__file__))

# --- plate (John, 2026-11-24) -------------------------------------------
PL, PW = 152.0, 135.0            # John, 2026-11-25: 152 x 135
# Corner holes and cut-outs are traced from Images/BasePlate.png and scaled
# to John's 140 x 120 outline, so they are approximate.
SX, SY = PL / 151.6, PW / 135.3
HX = [(PL - 125) / 2, (PL + 125) / 2]   # John: 125 x 120, centred
HY = [(PW - 120) / 2, (PW + 120) / 2]
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
BL, BW = 136.0, 75.6             # P108 body footprint (John, 2026-11-25)
TOTAL_H = 150.0                  # battery + adapter, measured by John
AD_L, AD_W, AD_H = 84.0, 75.0, 90.0   # Power Wheel adapter (listing photo)
BODY_H = TOTAL_H - AD_H          # battery top surface (where the adapter base sits)
FLOOR = 4.0                      # cradle floor
WALL, WALL_H, END_WALL, END_H = 2.4, 22.0, 1.2, 12.0
BAY_L, BAY_W = BL + 1.6, BW + 2.0
STANDOFF, LIP_H = 11.0, 10.0
# trays (match tank_deck_mount.scad)
FRONT_OVH, REAR_OVH, TRAY_W, LAP_L, LAP_T = 62, 80, 116, 30.0, 2.0
TRAY_T = 5.0
CASE_TOP, SHOULDER, TOWER_OUT, HOOD_TOP = FLOOR + 65.0, 70.1, 30 + 5 + 14 + 4.8, FLOOR + 146.0
TOWER_CX = (PL - BAY_L) / 2 + 0.8 + 80 + 15   # upright: switch face 80 mm behind the battery's front end (John)
TW_CX = TOWER_CX - 7         # tower opening shifted forward 7 mm: lead, fuse and switch gap in front of the adapter
LANE_W = 13.0
IBT_POS = [(-FRONT_OVH + 6, (PW - TRAY_W) / 2 + 3.5), (-FRONT_OVH + 6, PW - (PW - TRAY_W) / 2 - 3.5 - 50)]

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
    dim(ax, (0, 0), (PL, 0), "%.0f" % PL, off=16, side=-1, fs=13)
    dim(ax, (0, 0), (0, PW), "%.0f" % PW, off=16, side=1, fs=13)
    dim(ax, (HX[0], HY[1]), (HX[1], HY[1]), "%.0f" % (HX[1] - HX[0]), off=14, fs=10)
    dim(ax, (HX[1], HY[0]), (HX[1], HY[1]), "%.0f" % (HY[1] - HY[0]), off=16, side=-1, fs=10)
    dim(ax, (0, HY[1]), (HX[0], HY[1]), "%.1f" % HX[0], off=6, fs=9)
    dim(ax, (HX[0], PW), (HX[0], HY[1]), "%.1f" % (PW - HY[1]), off=14, side=-1, fs=9)
    ax.annotate("4 x corner holes, Ø3.4 (M3)\nM3 x 12 + nut under the plate",
                xy=(HX[1], HY[0]), xytext=(PL + 8, -14), fontsize=10,
                arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.text(PL / 2, -34, "Outline 152 x 135 mm as measured by John.\n"
            "Corner holes 125 x 120 apart, centred (John). Cut-outs are traced\n"
            "from Images/BasePlate.png, so they are approximate.\n"
            "Shaded strips: where the tray tongues lie under the cradle.",
            ha="center", va="top", fontsize=10, color="#495057")
    ax.text(-8, PW / 2, "FRONT edge (135)\nfront tray hooks on here", rotation=90, ha="center", va="center",
            fontsize=10, color=BLUE, weight="bold")
    ax.text(PL + 44, PW / 2 + 20, "REAR edge (135)\nrear tray hooks on here", rotation=90, ha="center",
            va="center", fontsize=10, color=BLUE, weight="bold")
    for x0 in (0, PL - LAP_L):
        ax.add_patch(Rectangle((x0, 0), LAP_L, PW, fc=BLUE, alpha=0.08, ec=BLUE, ls=":", lw=0.8))
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
    ax.add_patch(Rectangle((bx0 - 0.8 - END_WALL, by0 - 1 - WALL), BAY_L + 2 * END_WALL, BAY_W + 2 * WALL,
                           fc="none", ec=BLUE, lw=2.2))
    ax.add_patch(Rectangle((bx0, by0), BL, BW, fc=ORANGE, ec="k", alpha=0.55))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, (PW - AD_W) / 2), AD_L, AD_W, fc=YEL, ec="k", alpha=0.85))
    ax.text(PL / 2, PW / 2, "Power Wheel adapter\n84 x 75\non top of the P108\n(nothing above it)",
            ha="center", va="center", fontsize=10)
    for (x, y) in IBT_POS:
        ax.add_patch(Rectangle((x, y), 50, 50, fc="#495057", ec="k", alpha=0.85))
        ax.text(x + 25, y + 25, "IBT-2\n50 x 50\nheatsink up", color="white", ha="center", va="center", fontsize=9)
    ux, uy = PL + REAR_OVH - 6 - 68.6, ty0 + 3.5
    ax.add_patch(Rectangle((ux, uy), 68.6, 53.3, fc="#0b7285", ec="k", alpha=0.85))
    ax.text(ux + 34, uy + 26, "UNO R4 WiFi\n68.6 x 53.3", color="white", ha="center", va="center", fontsize=9)
    ax.annotate("USB-C faces\nthe rear", xy=(PL + REAR_OVH - 6, uy + 30), xytext=(PL + REAR_OVH + 6, uy + 26),
                fontsize=9, arrowprops=dict(arrowstyle="->", lw=0.8))
    gx, gy = PL + REAR_OVH - 12 - 43.2, PW - ty0 - 7 - 43.2
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
    dim(ax, (0, 0), (PL, 0), "plate %.0f" % PL, off=10, side=-1)
    dim(ax, (PL, 0), (PL + REAR_OVH, 0), "%d past the plate" % REAR_OVH, off=10, side=-1)
    dim(ax, (-FRONT_OVH, -16), (PL + REAR_OVH, -16), "overall %d" % (FRONT_OVH + PL + REAR_OVH), off=12, side=-1)
    dim(ax, (PL + REAR_OVH, ty0), (PL + REAR_OVH, ty0 + TRAY_W), "tray %d" % TRAY_W, off=36, side=-1)
    dim(ax, (bx0 + BL, by0), (bx0 + BL, by0 + BW), "80", off=-9, side=-1)
    ax.text(LAP_L / 2 + 2, 14, "tongue\nunder", ha="center", fontsize=8, color=BLUE)
    ax.text(PL - LAP_L / 2 - 2, 14, "tongue\nunder", ha="center", fontsize=8, color=BLUE)
    ax.set_title("Top view (mm). Dashed lines are the plate's front and rear edges.", fontsize=13, loc="left")
    ax.set_xlim(-FRONT_OVH - 30, PL + REAR_OVH + 50); ax.set_ylim(-48, PW + 22)
    ax.set_aspect("equal"); ax.axis("off")

    # ---------------- side view -----------------------------------------
    ax = fig.add_axes([0.02, 0.02, 0.62, 0.38])
    ax.add_patch(Rectangle((0, -2), PL, 2, fc="#adb5bd", ec="k"))
    ax.add_patch(Rectangle((LAP_L, 0), PL - 2 * LAP_L, FLOOR, fc=BLUE, ec="k", alpha=0.7))
    for x0 in (0, PL - LAP_L):
        ax.add_patch(Rectangle((x0, LAP_T), LAP_L, FLOOR - LAP_T, fc=BLUE, ec="k", alpha=0.7))
    ax.add_patch(Rectangle((-FRONT_OVH, 0), FRONT_OVH + LAP_L, LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((-FRONT_OVH, LAP_T), FRONT_OVH, TRAY_T - LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((PL - LAP_L, 0), REAR_OVH + LAP_L, LAP_T, fc="#74c0fc", ec="k"))
    ax.add_patch(Rectangle((PL, LAP_T), REAR_OVH, TRAY_T - LAP_T, fc="#74c0fc", ec="k"))
    for x0, L in ((-FRONT_OVH, FRONT_OVH), (PL, REAR_OVH)):
        ax.add_patch(Rectangle((x0, TRAY_T), L, LIP_H, fc="none", ec=BLUE, lw=1, ls=":"))
    # casing outline
    hx = [0, 0, TW_CX - TOWER_OUT / 2, TW_CX - TOWER_OUT / 2, TW_CX + TOWER_OUT / 2, TW_CX + TOWER_OUT / 2, PL, PL]
    hz = [FLOOR, CASE_TOP, CASE_TOP + SHOULDER, HOOD_TOP, HOOD_TOP, CASE_TOP + SHOULDER, CASE_TOP, FLOOR]
    ax.plot(hx, hz, color="#212529", lw=2.2)
    for x0, x1 in ((-FRONT_OVH, 0), (PL + REAR_OVH, PL)):
        sgn = 1 if x1 > x0 else -1
        ax.plot([x0, x0, x0 + sgn * 6, x1], [TRAY_T + LIP_H, CASE_TOP - 6, CASE_TOP, CASE_TOP], color="#212529", lw=2.2)
    ax.text(PL / 2 + 34, CASE_TOP + SHOULDER + 8, "casing (hood lifts off)", fontsize=9)
    ax.add_patch(Rectangle((bx0, FLOOR), BL, BODY_H, fc=ORANGE, ec="k", alpha=0.6))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, FLOOR + BODY_H), AD_L, 8, fc=YEL, ec="k"))
    ax.add_patch(Rectangle((TOWER_CX - 15, FLOOR + BODY_H + 8), 30, AD_H - 8, fc=YEL, ec="k"))
    zb = TRAY_T + STANDOFF
    ax.add_patch(Rectangle((-FRONT_OVH + 6, zb), 50, 43, fc="#495057", ec="k", alpha=0.85))
    ax.text(-FRONT_OVH + 31, zb + 21, "IBT-2", color="white", ha="center", va="center", fontsize=9)
    ax.add_patch(Rectangle((PL + REAR_OVH - 71.6, zb), 68.6, 12, fc="#0b7285", ec="k", alpha=0.85))
    ax.text(PL + REAR_OVH - 37, zb + 20, "UNO + GPS", ha="center", fontsize=9)
    ax.annotate("lift straight up", xy=(PL / 2, FLOOR + TOTAL_H + 26), xytext=(PL / 2, FLOOR + TOTAL_H + 2),
                ha="center", fontsize=10, arrowprops=dict(arrowstyle="-|>", lw=2, color="#2b8a3e"))
    dim(ax, (PL, FLOOR), (PL, FLOOR + TOTAL_H), "150", off=26, side=-1)
    dim(ax, (-FRONT_OVH, zb), (-FRONT_OVH, zb + 43), "≈43", off=8, fs=9)
    ax.text(PL / 2, -14, "tray tongue (2) slides under the cradle's stepped floor; the kit's corner screws clamp both",
            ha="center", fontsize=9, color="#495057")
    ax.set_title("Side view (mm), front on the left. Black line = casing", fontsize=13, loc="left")
    ax.set_xlim(-FRONT_OVH - 22, PL + REAR_OVH + 30); ax.set_ylim(-22, FLOOR + TOTAL_H + 34)
    ax.set_aspect("equal"); ax.axis("off")

    ax = fig.add_axes([0.66, 0.02, 0.33, 0.36]); ax.axis("off")
    ax.text(0, 0.98, "Balance and clearance", fontsize=13, weight="bold", va="top")
    ax.text(0, 0.88,
            "Casing about 250 g, roughly symmetric.\n"
            "Front: 2 x IBT-2, about 130 g, centre about 100 mm\n"
            "ahead of the plate centre.\n"
            "Rear: UNO + GPS, about 40 g, about 110 mm behind it.\n"
            "Battery + adapter (about 1 kg) stay centred.\n\n"
            "The mount's centre of mass ends up about 6 to 7 mm\n"
            "ahead of the plate centre. With the motors and tracks\n"
            "included it's smaller still, so you won't notice it.\n\n"
            "Plate 152 x 135, corner holes 125 x 120 (John).\n"
            "Trays reach 17 mm above the plate, covers 69 mm.",
            fontsize=10.5, va="top")
    fig.suptitle("How everything fits: battery in the middle, electronics past the front and rear edges",
                 fontsize=16, x=0.02, ha="left", y=0.995)
    fig.savefig(os.path.join(OUT, "07_layout.png"), dpi=115, bbox_inches="tight", facecolor="white")
    plt.close(fig)



def wiring_drawing():
    """Where the wires run so only the adapter's leads show outside the casing."""
    fig = plt.figure(figsize=(17, 12))
    ax = fig.add_axes([0.02, 0.33, 0.96, 0.6])
    ty0 = (PW - TRAY_W) / 2
    bx0, by0 = (PL - BL) / 2, (PW - BW) / 2
    for x0, x1 in ((-FRONT_OVH, 0), (PL, PL + REAR_OVH)):
        ax.add_patch(FancyBboxPatch((x0, ty0), x1 - x0, TRAY_W, boxstyle="round,pad=0,rounding_size=5",
                                    fc="#e7f5ff", ec=BLUE, lw=1.4))
    ax.add_patch(FancyBboxPatch((0, 0), PL, PW, boxstyle="round,pad=0,rounding_size=5", fc="#f1f3f5", ec="k", lw=1.4))
    lanes = [(2.7, 2.7 + LANE_W), (PW - 2.7 - LANE_W, PW - 2.7)]
    for y0, y1 in lanes:
        ax.add_patch(Rectangle((0, y0), PL, y1 - y0, fc="#fff3bf", ec="#e8b100", lw=1, ls="--"))
    for y in (by0 - 1 - WALL, by0 + BW + 1):
        ax.add_patch(Rectangle((bx0 + 3, y), BAY_L - 8, WALL, fc=BLUE, ec="none", alpha=0.6))
    ax.add_patch(Rectangle((bx0, by0), BL, BW, fc=ORANGE, ec="k", alpha=0.35))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, (PW - AD_W) / 2), AD_L, AD_W, fc=YEL, ec="k", alpha=0.5))
    ax.add_patch(Rectangle((TOWER_CX - 15, PW / 2 - 15), 30, 30, fc=YEL, ec="k"))
    ax.add_patch(Rectangle((TW_CX - 24.5, PW / 2 - 19.5), 49, 39, fc="none", ec="#212529", lw=1.6, ls=":"))
    ax.text(TOWER_CX, PW / 2, "adapter\n30 x 30", ha="center", va="center", fontsize=9)
    for (x, y) in IBT_POS:
        ax.add_patch(Rectangle((x, y), 50, 50, fc="#495057", ec="k", alpha=0.75))
    ax.text(-FRONT_OVH + 31, IBT_POS[0][1] + 25, "IBT-2\nleft motor", color="white", ha="center", va="center", fontsize=9)
    ax.text(-FRONT_OVH + 31, IBT_POS[1][1] + 25, "IBT-2\nright motor", color="white", ha="center", va="center", fontsize=9)
    ux, uy = PL + REAR_OVH - 6 - 68.6, ty0 + 3.5
    ax.add_patch(Rectangle((ux, uy), 68.6, 53.3, fc="#0b7285", ec="k", alpha=0.75))
    ax.text(ux + 34, uy + 26, "UNO R4", color="white", ha="center", va="center", fontsize=9)
    gx, gy = PL + REAR_OVH - 12 - 43.2, PW - ty0 - 7 - 43.2
    ax.add_patch(Rectangle((gx, gy), 43.2, 43.2, fc=RED, ec="k", alpha=0.6))
    ax.text(gx + 21.6, gy + 21.6, "GPS", color="white", ha="center", va="center", fontsize=9)
    # plate slots the motor wires come up through (approximate, from the plate scan)
    for y in (6.0, PW - 6.0):
        ax.add_patch(FancyBboxPatch((40, y - 1.4), 50, 2.8, boxstyle="round,pad=0,rounding_size=1.4", fc="white", ec="k", lw=0.8))
    def path(pts, col, lw=3.2, ls="-"):
        xs, ys = zip(*pts); ax.plot(xs, ys, color=col, lw=lw, ls=ls, solid_capstyle="round", solid_joinstyle="round")
    PWR, MOT, SIG = "#e03131", "#f08c00", "#1c7ed6"
    # power: adapter leads drop down in front of the adapter, cross the battery top to the left lane, forward to both IBT-2s
    path([(TW_CX - 25, PW / 2 + 4), (TW_CX - 25, 12.5), (-8, 12.5), (-8, IBT_POS[0][1] + 12), (IBT_POS[0][0] + 44, IBT_POS[0][1] + 12)], PWR)
    path([(-8, 12.5), (-8, PW / 2), (-16, PW / 2), (-16, IBT_POS[1][1] + 12), (IBT_POS[1][0] + 44, IBT_POS[1][1] + 12)], PWR, lw=2.6)
    # thin 18 V feed to the UNO VIN along the right lane
    path([(-12, PW / 2 + 2), (-4, PW / 2 + 2), (-4, PW - 6), (PL + 10, PW - 6), (PL + 10, uy + 40), (ux + 60, uy + 40)], PWR, lw=1.6)
    # motor wires: up through the plate slots, along the lanes, into the IBT-2 motor terminals
    path([(65, 6.0), (65, 6.5), (-3, 6.5), (-3, IBT_POS[0][1] + 40), (IBT_POS[0][0] + 44, IBT_POS[0][1] + 40)], MOT)
    path([(65, PW - 6.0), (60, PW - 9.5), (-1, PW - 9.5), (-1, IBT_POS[1][1] + 40), (IBT_POS[1][0] + 44, IBT_POS[1][1] + 40)], MOT)
    # signals: UNO pins along the right lane to both IBT-2 headers
    path([(ux + 50, uy + 48), (PL + 6, uy + 48), (PL + 6, PW - 12.5), (-6, PW - 12.5), (-6, PW / 2 + 10), (IBT_POS[0][0] + 44, IBT_POS[0][1] + 30)], SIG, lw=2)
    path([(-6, PW / 2 + 10), (-6, IBT_POS[1][1] + 25), (IBT_POS[1][0] + 44, IBT_POS[1][1] + 25)], SIG, lw=2)
    path([(ux + 20, uy + 50), (gx + 10, gy + 5)], SIG, lw=2)
    ax.annotate("adapter leads leave its top and drop down\ninside the hood, in the 12 mm gap in front\nof the adapter (dotted = hood opening)",
                xy=(TW_CX - 25, PW / 2 + 4), xytext=(TW_CX - 60, PW + 22), fontsize=9.5, arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.annotate("hidden wire lanes under the hood\n(13 mm wide, between the cradle walls\nand the hood sides)", xy=(110, PW - 9), xytext=(150, PW + 22),
                fontsize=9.5, arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.annotate("motor wires come up from the chassis\nthrough the plate's edge slots", xy=(80, 6), xytext=(60, -30),
                fontsize=9.5, arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.annotate("lanes continue into the trays through\npassages in the hood ends and notches\nin the tray rib, under the boards", xy=(-3, 9), xytext=(-FRONT_OVH - 10, -30),
                fontsize=9.5, arrowprops=dict(arrowstyle="->", lw=0.8))
    ax.annotate("USB-C: only for programming", xy=(PL + REAR_OVH, uy + 30), xytext=(PL + REAR_OVH + 4, uy + 8), fontsize=9,
                arrowprops=dict(arrowstyle="->", lw=0.8))
    from matplotlib.lines import Line2D
    ax.legend(handles=[Line2D([], [], color=PWR, lw=3, label="18 V power (adapter leads), thin branch to UNO VIN"),
                       Line2D([], [], color=MOT, lw=3, label="motor wires"),
                       Line2D([], [], color=SIG, lw=2, label="signals: UNO to IBT-2s and GPS")],
              loc="upper right", bbox_to_anchor=(1.0, 1.08), fontsize=10, frameon=False)
    ax.set_title("Top view with the casing removed (mm). Front on the left.", fontsize=13, loc="left")
    ax.set_xlim(-FRONT_OVH - 25, PL + REAR_OVH + 70); ax.set_ylim(-48, PW + 40); ax.set_aspect("equal"); ax.axis("off")

    # side detail: the adapter leads
    ax = fig.add_axes([0.05, 0.02, 0.45, 0.3])
    top = FLOOR + TOTAL_H
    ax.add_patch(Rectangle((0, FLOOR), PL, BODY_H, fc=ORANGE, ec="k", alpha=0.35))
    ax.add_patch(Rectangle(((PL - AD_L) / 2, FLOOR + BODY_H), AD_L, 8, fc=YEL, ec="k"))
    ax.add_patch(Rectangle((TOWER_CX - 15, FLOOR + BODY_H + 8), 30, AD_H - 8, fc=YEL, ec="k"))
    hx = [0, 0, TW_CX - TOWER_OUT / 2, TW_CX - TOWER_OUT / 2]
    hz = [FLOOR, CASE_TOP, CASE_TOP + SHOULDER, HOOD_TOP]
    ax.plot(hx, hz, color="#212529", lw=2.2)
    ax.plot([PL - x for x in hx][::-1] + [], hz[::-1], color="#212529", lw=2.2) if False else ax.plot([PL, PL, TW_CX + TOWER_OUT / 2, TW_CX + TOWER_OUT / 2], hz, color="#212529", lw=2.2)
    lx = PL / 2 - 20.5
    ax.plot([PL / 2 - 5, PL / 2 - 5, lx - 5, lx - 6, lx - 6, 14, 14, 14], [top - 1, top + 12, top + 12, top - 6, FLOOR + BODY_H + 12, FLOOR + BODY_H + 4, 30, FLOOR + 3],
            color=PWR, lw=3, solid_joinstyle="round")
    ax.text(PL / 2 + 6, top + 14, "only this loop of the adapter leads is outside", fontsize=10, color=PWR)
    ax.text(16, 34, "down the side of the battery\ninto the wire lane", fontsize=9, color=PWR)
    ax.text(PL + 6, CASE_TOP, "hood", fontsize=10)
    ax.set_title("Adapter leads, side view", fontsize=12, loc="left")
    ax.set_xlim(-10, PL + 70); ax.set_ylim(-4, top + 26); ax.set_aspect("equal"); ax.axis("off")

    ax = fig.add_axes([0.55, 0.02, 0.43, 0.27]); ax.axis("off")
    ax.text(0, 0.98, "Why it's routed this way", fontsize=13, weight="bold", va="top")
    ax.text(0, 0.86,
            "The adapter's leads go down inside the hood's tower, in front of the adapter.\n"
            "So the hood still lifts straight off over the adapter and its leads, and you\n"
            "don't have to unplug anything to change the battery.\n\n"
            "Everything else stays inside the casing. Motor wires come up from the chassis\n"
            "through the plate's edge slots under the hood. All runs between the trays use\n"
            "the two hidden side lanes, and they pass under the boards in the trays.\n\n"
            "Leave about 150 mm of slack in the adapter leads so the adapter can be set\n"
            "beside the tank while the battery comes out.",
            fontsize=10.5, va="top")
    fig.suptitle("Wire routing: only the adapter's leads show outside the casing", fontsize=16, x=0.02, ha="left", y=0.995)
    fig.savefig(os.path.join(OUT, "08_wiring.png"), dpi=110, bbox_inches="tight", facecolor="white")
    plt.close(fig)


def slot_measure_drawing():
    """What John needs to measure for the hitch: the two long slots at the plate's rear edge."""
    fig, ax = plt.subplots(figsize=(11, 10))
    plate_outline(ax, fc="#e9ecef")
    sl = [(PL - 10, 20.0, 62.0), (PL - 10, 82.0, 127.0)]   # John measured 2026-11-25: (x centre, y start, y end)
    w = 4.0   # John measured
    for x, y0, y1 in sl:
        ax.add_patch(FancyBboxPatch((x - w / 2, y0), w, y1 - y0, boxstyle="round,pad=0,rounding_size=1.8",
                                    fc="#ffd8a8", ec=ORANGE, lw=2))
    # A: rear edge to the slot centre line
    dim(ax, (sl[0][0], 8), (PL, 8), "A", off=0, fs=15, color=RED)
    dim(ax, (sl[1][0], 112), (PL, 112), "A2", off=0, fs=13, color=RED)
    # B..E: from the left long edge (y = 0) to each slot end, along the slot
    xs = PL + 10
    for lab, y in [("B", sl[0][1]), ("C", sl[0][2]), ("D", sl[1][1]), ("E", sl[1][2])]:
        ax.plot([sl[0][0] if lab in "BC" else sl[1][0], xs + (["B", "C", "D", "E"].index(lab)) * 9], [y, y], color=BLUE, lw=0.6, ls=":")
    for i, (lab, y) in enumerate([("B", sl[0][1]), ("C", sl[0][2]), ("D", sl[1][1]), ("E", sl[1][2])]):
        xx = xs + i * 9
        ax.annotate("", xy=(xx, 0), xytext=(xx, y), arrowprops=dict(arrowstyle="<->", lw=1.0, color=BLUE))
        ax.text(xx, y + 2.5, lab, ha="center", va="bottom", fontsize=14, color=BLUE, fontweight="bold")
    ax.plot([PL - 4, xs + 32], [0, 0], color=BLUE, lw=0.8)
    # W: slot width
    ax.annotate("W", xy=(sl[1][0] + w / 2, 84), xytext=(sl[1][0] - 22, 84), fontsize=14, color=RED, fontweight="bold",
                va="center", arrowprops=dict(arrowstyle="->", lw=1.0, color=RED))
    ax.text(PL / 2 - 10, PW / 2 + 6, "FRONT", fontsize=13, color=GREY, ha="center")
    ax.annotate("", xy=(PL / 2 - 40, PW / 2), xytext=(PL / 2 + 20, PW / 2), arrowprops=dict(arrowstyle="->", lw=1.5, color=GREY))
    ax.annotate("REAR edge (hitch side)", xy=(PL, PW - 20), xytext=(PL - 30, PW + 7), fontsize=12, ha="left", arrowprops=dict(arrowstyle="->", lw=1.0))
    for y in (26, 56, 86, 108):   # hitch bolt positions
        ax.add_patch(plt.Circle((PL - 10, y), 1.6, fc=RED, ec="black", lw=0.6, zorder=5))
    ax.text(-2, -6, "B to E are from this long edge", ha="left", va="top", fontsize=11, color=BLUE)
    ax.text(PL / 2, -18,
            "Measured by John, in mm, on the top face of the metal plate:\n"
            "A   rear edge to the centre line of the slots (A2 too, if the two slots are not in line)\n"
            "B, C   that long edge to the near and far ends of the first slot\n"
            "D, E   that long edge to the near and far ends of the second slot\n"
            "W   slot width.        Already known: plate 152 x 135, 3 mm thick.\n"
            "Measured: A = A2 = 10, B 20, C 62, D 82, E 127.  W = 4.  Red dots: the 4 hitch bolts (y 26, 56, 86, 108).",
            ha="center", va="top", fontsize=11.5, multialignment="left")
    ax.set_xlim(-12, PL + 55); ax.set_ylim(-62, PW + 14); ax.set_aspect("equal"); ax.axis("off")
    ax.set_title("Hitch: the plate's two rear slots as measured, with the hitch bolts (top view)", fontsize=15, loc="left")
    fig.savefig(os.path.join(OUT, "15_measure_rear_slots.png"), dpi=120, bbox_inches="tight", facecolor="white")
    plt.close(fig)


if __name__ == "__main__":
    plate_drawing()
    fit_drawing()
    wiring_drawing()
    slot_measure_drawing()
    print("wrote", OUT)


def hole_match_drawing():
    """Which holes in the printed base line up with which holes in the metal plate."""
    import trimesh
    from matplotlib.patches import PathPatch
    from matplotlib.path import Path
    base = trimesh.load(os.path.join(OUT, "..", "stl", "deck_mount_base.stl"))
    sec = base.section(plane_origin=[0, 0, 1.0], plane_normal=[0, 0, 1]).to_2D(to_2D=np.eye(4))[0]
    fig, axs = plt.subplots(1, 2, figsize=(20, 10))
    bolts = [(PL - 10, y) for y in (26, 56, 86, 108)]
    corners = [(hx, hy) for hx in HX for hy in HY]
    labs = {c: str(i + 1) for i, c in enumerate(sorted(corners, key=lambda c: (c[1] < 60, c[0])))}
    for k, ax in enumerate(axs):
        if k == 0:
            plate_outline(ax, fc="#dee2e6")
            for x, y0, y1 in [(PL - 10, 20, 62), (PL - 10, 82, 127)]:
                ax.add_patch(FancyBboxPatch((x - 2, y0), 4, y1 - y0, boxstyle="round,pad=0,rounding_size=2",
                                            fc="white", ec="k", lw=1))
            ax.set_title("Metal plate (top view)", fontsize=16, loc="left")
        else:
            loops = [np.asarray(l)[:, :2] for l in sec.discrete]
            area = lambda l: 0.5 * np.sum(l[:-1, 0] * l[1:, 1] - l[1:, 0] * l[:-1, 1])
            big = max(range(len(loops)), key=lambda i: abs(area(loops[i])))
            verts, codes = [], []
            for i, l in enumerate(loops):
                if (area(l) > 0) != (i == big): l = l[::-1]
                verts += l.tolist(); codes += [Path.MOVETO] + [Path.LINETO] * (len(l) - 1)
            ax.add_patch(PathPatch(Path(verts, codes), fc="#a5b4fc", ec=BLUE, lw=1))
            ax.set_title("Printed base, same way round (top view)", fontsize=16, loc="left")
        for c in corners:
            ax.add_patch(Circle(c, 4.2, fc="none", ec="#2f9e44", lw=3))
            ax.text(c[0], c[1] + (6.5 if c[1] > 60 else -6.5), labs[c], ha="center", va="bottom" if c[1] > 60 else "top", fontsize=15, color="#2f9e44", fontweight="bold")
        for i, b in enumerate(bolts):
            ax.add_patch(Circle(b, 3.2, fc="none", ec=RED, lw=2.5))
            ax.text(b[0] - 5, b[1], "H%d" % (i + 1), ha="right", va="center", fontsize=12, color=RED, fontweight="bold")
        h1, h3 = (HX[0], HY[1]), (HX[0], HY[0])
        # outside size and corner-hole pattern
        dim(ax, (0, 0), (PL, 0), "%.0f" % PL, off=24, side=-1, fs=12)
        dim(ax, (0, 0), (0, PW), "%.0f" % PW, off=34, fs=12)
        dim(ax, (HX[0], HY[1]), (HX[1], HY[1]), "125", off=PW - HY[1] + 10, fs=12, color="#2f9e44")
        dim(ax, h3, h1, "120", off=HX[0] + 18, fs=12, color="#2f9e44")
        dim(ax, (0, HY[0]), (HX[0], HY[0]), "%.1f" % HX[0], off=HY[0] + 9, side=-1, fs=10, color="#2f9e44")
        dim(ax, (HX[1], HY[0]), (PL, HY[0]), "%.1f" % (PL - HX[1]), off=HY[0] + 9, side=-1, fs=10, color="#2f9e44")
        dim(ax, (HX[0], 0), (HX[0], HY[0]), "%.1f" % HY[0], off=HX[0] + 8, fs=10, color="#2f9e44")
        # hitch bolts: 10 from the rear edge, heights from the bottom long edge
        dim(ax, (PL - 10, PW), (PL, PW), "10", off=6, fs=10, color=RED)
        for i, b_ in enumerate(bolts):
            xx = PL + 8 + i * 8
            ax.plot([b_[0] + 3.2, xx], [b_[1], b_[1]], color=RED, lw=0.5, ls=":")
            ax.annotate("", xy=(xx, 0), xytext=(xx, b_[1]), arrowprops=dict(arrowstyle="<->", lw=0.9, color=RED))
            ax.text(xx, b_[1] + 1.5, "%d" % b_[1], ha="center", va="bottom", fontsize=10, color=RED, fontweight="bold")
        ax.plot([PL, PL + 34], [0, 0], color=RED, lw=0.6)
        if k == 0:
            ax.text(6, -40, "Corner holes: the kit's M3 holes (about Ø3.4)\nRear slots: 4 wide, 20 to 62 and 82 to 127 from the bottom edge,\ncentre line 10 from the rear edge", fontsize=11, va="top")
        else:
            ax.text(6, -40, "Corner holes 1-4: Ø3.4 through (M3)\nHitch holes H1-H4: Ø3.4 through (M3)\nAll holes go right through the flat base", fontsize=11, va="top")
        ax.text(PL / 2, PW + 26, "front on the left, rear (hitch end) on the right; hitch heights from the bottom edge", ha="center", fontsize=11, color=GREY)
        ax.set_xlim(-40, PL + 44); ax.set_ylim(-62, PW + 32); ax.set_aspect("equal"); ax.axis("off")
    fig.text(0.05, 0.02,
             "Green 1-4: corner holes. They must line up: the 4 corner screws go through tray tongue, base and plate, with a nut under the plate (125 x 120 apart).\n"
             "Red H1-H4: hitch bolts. They line up over the plate's two rear slots (only used when the hitch is fitted).\n"
             "No other holes need to line up. The base's other openings are wire and weight-saving cut-outs.",
             fontsize=13, va="bottom")
    fig.savefig(os.path.join(OUT, "17_hole_match.png"), dpi=110, bbox_inches="tight", facecolor="white")
    plt.close(fig)
