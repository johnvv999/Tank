"""Dimension sheets of every printed part, drawn straight from the STL files (top and side views)."""
import os, sys
import numpy as np, trimesh
import matplotlib; matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
D = os.path.dirname(os.path.abspath(__file__)); S = os.path.join(D, "..", "stl"); OUT = os.path.join(D, "..", "images")

def view(ax, m, axes, title, col):
    t = m.triangles[:, :, axes]
    ax.add_collection(PolyCollection(t, fc=col, ec=col, lw=0.15))
    lo, hi = m.bounds[0][axes], m.bounds[1][axes]
    w, h = hi - lo
    ax.annotate("", xy=(lo[0], lo[1] - 0.08 * max(w, h)), xytext=(hi[0], lo[1] - 0.08 * max(w, h)), arrowprops=dict(arrowstyle="<->", lw=1))
    ax.text((lo[0] + hi[0]) / 2, lo[1] - 0.10 * max(w, h), "%.1f" % w, ha="center", va="top", fontsize=11, fontweight="bold")
    ax.annotate("", xy=(hi[0] + 0.06 * max(w, h), lo[1]), xytext=(hi[0] + 0.06 * max(w, h), hi[1]), arrowprops=dict(arrowstyle="<->", lw=1))
    ax.text(hi[0] + 0.08 * max(w, h), (lo[1] + hi[1]) / 2, "%.1f" % h, ha="left", va="center", fontsize=11, fontweight="bold", rotation=90)
    pad = 0.22 * max(w, h)
    ax.set_xlim(lo[0] - 0.05 * max(w, h), hi[0] + pad); ax.set_ylim(lo[1] - pad, hi[1] + 0.05 * max(w, h))
    ax.set_aspect("equal"); ax.axis("off"); ax.text(0.5, 0.0, title, transform=ax.transAxes, ha="center", va="top", fontsize=9.5, color="#666")

def sheet(parts, name, heading):
    n = len(parts)
    fig, axs = plt.subplots(n, 2, figsize=(14, 4.2 * n), gridspec_kw=dict(width_ratios=[1, 1]))
    if n == 1: axs = [axs]
    for (f, label, note), (a1, a2) in zip(parts, axs):
        m = trimesh.load(os.path.join(S, f + ".stl"))
        e = m.extents
        view(a1, m, [0, 1], "top view, as it sits on the print bed", "#4c6ef5")
        view(a2, m, [0, 2], "side view (bed at the bottom)", "#868e96")
        a1.text(-0.02, 1.12, "%s   %.0f x %.0f x %.0f mm" % (label, e[0], e[1], e[2]), transform=a1.transAxes, fontsize=14, fontweight="bold")
        if note: a1.text(-0.02, 1.03, note, transform=a1.transAxes, fontsize=10.5, color="#333")
    fig.suptitle(heading, fontsize=17, x=0.02, ha="left", y=1.0)
    fig.tight_layout(); fig.savefig(os.path.join(OUT, name), dpi=90, bbox_inches="tight", facecolor="white"); plt.close(fig)

deck = [("deck_mount_base", "Base (battery cradle)", "Sits on the 152 x 135 plate. 4 corner holes 125 x 120, 4 hitch holes 10 mm from the rear edge (see 17_hole_match)"),
        ("deck_mount_front_tray", "Front tray (2x IBT-2)", "Tongue goes into the base's front pocket; sticks out 62 mm past the plate"),
        ("deck_mount_rear_tray", "Rear tray (UNO + GPS)", "Tongue goes into the base's rear pocket; sticks out 80 mm past the plate"),
        ("deck_mount_front_cover", "Front cover", "Prints top down"),
        ("deck_mount_rear_cover", "Rear cover", "Prints top down; USB-C window, antenna pod holes"),
        ("deck_mount_hood", "Hood", "Prints open side down; lifts off for battery changes")]
other = [("antenna_pod_cup", "Antenna pod cup", ""), ("antenna_pod_lid", "Antenna pod lid", "Twist-on bayonet"),
         ("hitch_hitch", "Hitch (clamp, beam and servo head)", "6 walls, 40% infill"), ("hitch_lid", "Servo lid", ""),
         ("hitch_lift_arm", "Lift arm", ""), ("hitch_link", "Link", ""),
         ("hitch_rake_frame", "Rake frame", ""), ("hitch_rake_bar", "Rake bar", "Prints tines up"), ("hitch_drawbar", "Trailer drawbar", "")]
sheet(deck, "18_print_parts_deck.png", "Deck parts to print (sizes in mm, from the STL files)")
sheet(other, "19_print_parts_hitch_pod.png", "Antenna pod and hitch parts to print (sizes in mm, from the STL files)")
print("ok")
