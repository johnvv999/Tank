// =====================================================================
//  Tank deck mount: Ryobi P108 battery + Power Wheel adapter + hinged
//  electronics deck (Arduino UNO R4 WiFi, 2x IBT-2, SparkFun ZED-F9P)
//  Chassis: "Smart Robot Car" full-metal big tank kit
//
//  Coordinates: X = along the plate's 140 mm side, Y = along its 120 mm
//  side, Z = up from the top of the chassis plate.  Units: mm.
//  The battery lies along X. The deck hinges on the Y = 0 edge ("hinge
//  side") and is pinned shut on the Y = 120 edge ("latch side").
//
//  Battery access: pull the two latch pins (M3 thumb screws), swing the
//  deck up and over the hinge side (it rests on a stop at about 93 deg),
//  lift the adapter straight up off the battery, then lift the battery out.
//
//  Render one part at a time:  openscad -D 'part="base"' -o base.stl ...
//  part = "assembly" | "base" | "deck" | "post"
// =====================================================================

part = "assembly";
// assembly view controls (pictures only)
open_deg  = 0;     // deck opening angle (0 = closed, ~93 = resting on the stop)
cap_lift  = 0;     // how far the adapter is lifted (mm)
bat_lift  = 0;     // how far the battery is lifted (mm)
cap_aside = false; // adapter set down beside the tank (leads still attached)
explode   = 0;     // exploded-view spacing (mm)

/* ---------- chassis top plate ---------- */
plate_L   = 140;      // John, 2026-11-24
plate_W   = 120;      // John, 2026-11-24
plate_r   = 4;
// Corner screw holes: traced from Images/BasePlate.png and scaled to the
// 140 x 120 outline. APPROXIMATE: measure and correct before printing.
hole_x    = [12.0, 128.2];
hole_y    = [7.0, 112.9];

/* ---------- Ryobi P108 battery + Power Wheel adapter ---------- */
bat_L     = 135;      // footprint from Archive/Battery+VR.f3d (confirm with a ruler)
bat_W     = 80;
bat_cap_h = 150;      // battery + adapter, standing (John)
cap_L     = 84;       // adapter base, from the listing photo
cap_W     = 75;
cap_h     = 90;
end_clear = 0.75;     // bay clearance at each end
side_clear= 1.0;      // bay clearance at each side

/* ---------- general ---------- */
floor_t   = 3.0;
wall_t    = 2.4;      // side walls
wall_h    = 22;
end_t     = 1.6;      // end walls (thin: the battery is only 5 mm shorter than the plate)
end_h     = 12;
deck_t    = 3.0;
lip_h     = 5;
gap_top   = 20;       // adapter top to deck underside: room for the adapter's leads
m3        = 3.4;
m3_nut    = 6.0;
post_x    = 14;
post_y    = 9.6;
post_xs   = [35, 105];      // post centres along X (clear of the corner screws)
cup_clear = 0.4;
cup_h     = 14;
cup_wall  = 2.4;
cup_out   = 1.0;

/* ---------- hinge and latch pins ---------- */
pin_off   = 8;        // pin axis distance outboard of the plate edge
pin_dz    = 4.5;      // pin axis height above the deck top surface
lug_r     = 6;
lug_w     = 6.4;
stop_h    = 10;
$fn = 40;

/* ---------- derived ---------- */
bay_L   = bat_L + 2*end_clear;
bay_W   = bat_W + 2*side_clear;
bay_x0  = (plate_L - bay_L)/2;
bay_y0  = (plate_W - bay_W)/2;
post_L  = bat_cap_h + gap_top;
deck_z  = floor_t + post_L;
pin_z   = deck_z + deck_t + pin_dz;
pin_ys  = [-pin_off, plate_W + pin_off];          // hinge side, latch side
cup_ox  = post_x + 2*cup_clear + 2*cup_wall;
cup_oy  = cup_out + post_y + 2*cup_clear + cup_wall;
lug_x0  = [post_xs[0] + post_x/2 + 0.4, post_xs[1] - post_x/2 - 0.4 - lug_w];
function cx(i) = post_xs[i];
function cy(j) = j == 0 ? cup_out + cup_clear + post_y/2
                        : plate_W - (cup_out + cup_clear + post_y/2);

assert(bay_L + 2*end_t <= plate_L + 0.01, "battery too long for the plate: see drawings/08_battery_fit.png");
assert(cup_oy < bay_y0 - wall_t, "post cups hit the cradle side walls");
assert(cap_W <= bat_W && cap_L <= bat_L, "adapter wider than the battery: check the cradle");

module rrect(l, w, r, h) {
    translate([r, r, 0]) linear_extrude(h) offset(r) square([l - 2*r, w - 2*r]);
}
module slot(len, w, h) {
    hull() for (s = [-1, 1]) translate([s*(len - w)/2, 0, 0]) cylinder(d = w, h = h);
}
module ziptie_pair(h) {
    for (s = [-1, 1]) translate([s*4, 0, 0]) cube([2, 4, h], center = true);
}

// =====================================================================
//  BASE / BATTERY CRADLE
// =====================================================================
module base() {
    chan_w = bay_y0 - wall_t - 6;
    difference() {
        union() {
            difference() {
                rrect(plate_L, plate_W, plate_r, floor_t);
                // windows under the battery (lighter; plate cut-outs stay reachable)
                ww = (bay_L - 30)/2;
                for (x = [bay_x0 + 10, bay_x0 + 20 + ww])
                    translate([x, bay_y0 + 10, -1]) rrect(ww, bay_W - 20, 4, floor_t + 2);
                // side wire channels between the cups
                for (y = [3, plate_W - 3 - chan_w])
                    translate([post_xs[0] + cup_ox/2 + 3, y, -1])
                        rrect(post_xs[1] - post_xs[0] - cup_ox - 6, chan_w, 2, floor_t + 2);
            }
            // side walls (full length) and low end walls
            for (y = [bay_y0 - wall_t, bay_y0 + bay_W])
                translate([bay_x0 - end_t, y, 0]) cube([bay_L + 2*end_t, wall_t, wall_h]);
            for (x = [bay_x0 - end_t, bay_x0 + bay_L])
                translate([x, bay_y0 - wall_t, 0]) cube([end_t, bay_W + 2*wall_t, end_h]);
            // post cups along both 140 mm edges
            for (i = [0, 1]) {
                translate([cx(i) - cup_ox/2, 0, 0]) cube([cup_ox, cup_oy, cup_h]);
                translate([cx(i) - cup_ox/2, plate_W - cup_oy, 0]) cube([cup_ox, cup_oy, cup_h]);
            }
        }
        for (i = [0, 1], j = [0, 1]) {
            translate([cx(i), cy(j), floor_t + cup_h/2])
                cube([post_x + 2*cup_clear, post_y + 2*cup_clear, cup_h], center = true);
            translate([cx(i), cy(j), floor_t + 6]) rotate([0, 90, 0])
                cylinder(d = m3, h = cup_ox + 2, center = true);
            translate([hole_x[i], hole_y[j], -1]) cylinder(d = m3, h = floor_t + 2);
        }
        // finger scoops in the side walls to grip the battery
        for (y = [bay_y0 - wall_t - 1, bay_y0 + bay_W - 1])
            translate([plate_L/2, y, wall_h]) rotate([-90, 0, 0]) cylinder(d = 34, h = wall_t + 2);
    }
}

// =====================================================================
//  POST  (x4, all identical; print lying on its side)
//  Arm out to a pin outboard of the plate edge, plus a stop block.
//  Hinge side: M3 x 25 + nylock as the hinge pin.
//  Latch side: same post turned round; an M3 x 25 thumb screw is the latch pin.
// =====================================================================
module post() {
    py0 = cup_out + cup_clear;
    ply = -pin_off - py0;
    plz = post_L + deck_t + pin_dz;
    difference() {
        union() {
            cube([post_x, post_y, post_L]);
            hull() {
                translate([0, ply - 4.5, post_L - 10]) cube([post_x, post_y - ply + 4.5, 10]);
                translate([0, 0, post_L - 34]) cube([post_x, 1, 1]);
            }
            translate([0, ply - 4.5, post_L - 1]) cube([post_x, 8.5, plz - post_L + 1 + stop_h]);
        }
        translate([-1, ply, plz]) rotate([0, 90, 0]) cylinder(d = m3, h = post_x + 2);
        translate([-1, post_y/2, 6]) rotate([0, 90, 0]) cylinder(d = m3, h = post_x + 2);
    }
}

// =====================================================================
//  DECK
// =====================================================================
ibt_holes = 40;   ibt_board = 50;      // Archive/MotorBox.f3d
gps_holes = 38.1; gps_board = 43.2;    // Archive/GPSBox.f3d (SparkFun GPS-RTK2)
uno_L = 68.6;  uno_W = 53.3;
uno_holes = [[13.97, 2.54], [15.24, 50.80], [66.04, 7.62], [66.04, 35.56]];
standoff_h = 6;

ibt_pos = [[14, 9], [76, 9]];                    // hinge-side row
uno_pos = [5, plate_W - 6 - uno_W];              // latch-side row, USB on the X = 0 edge
gps_pos = [plate_L - 12 - gps_board, plate_W - 12 - gps_board];

module standoff_at(p, d = 7) { translate([p[0], p[1], 0]) cylinder(d = d, h = deck_t + standoff_h); }
module bolt_at(p) { translate([p[0], p[1], -1]) cylinder(d = m3, h = deck_t + standoff_h + 2); }
module radial_slot_at(p, c) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], -1]) rotate([0, 0, a]) slot(m3 + 5, m3, deck_t + standoff_h + 2);
}
module radial_standoff_at(p, c, d) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], 0]) rotate([0, 0, a]) slot(5 + d, d, deck_t + standoff_h);
}
function sq_holes(o, b, s) = [for (i = [0, 1], j = [0, 1]) [o[0] + (b - s)/2 + i*s, o[1] + (b - s)/2 + j*s]];
function uno_pts() = [for (h = uno_holes) [uno_pos[0] + h[0], uno_pos[1] + h[1]]];

// pin lug on the deck edge; y = pin axis (outboard of the edge)
module lug(x, y, nut, nut_hi = true) {
    y0 = y < 0 ? y - lug_r : plate_W - 4;
    y1 = y < 0 ? 4 : y + lug_r;
    difference() {
        hull() {
            translate([x, y, deck_t + pin_dz]) rotate([0, 90, 0]) cylinder(r = lug_r, h = lug_w);
            translate([x, y0, 0]) cube([lug_w, y1 - y0, deck_t]);
        }
        translate([x - 1, y, deck_t + pin_dz]) rotate([0, 90, 0]) cylinder(d = m3, h = lug_w + 2);
        if (nut) translate([nut_hi ? x + lug_w - 2.6 : x - 0.4, y, deck_t + pin_dz]) rotate([0, 90, 0])
            cylinder(d = m3_nut / cos(30), h = 3, $fn = 6);
    }
}

module deck() {
    gps_pts = sq_holes(gps_pos, gps_board, gps_holes);
    difference() {
        union() {
            difference() {
                rrect(plate_L, plate_W, plate_r, deck_t + lip_h);
                translate([3, 3, deck_t]) rrect(plate_L - 6, plate_W - 6, plate_r - 1, lip_h + 1);
                for (x = post_xs, y = [-1, plate_W - 4])       // clear the stop blocks
                    translate([x - post_x/2 - 0.5, y, deck_t]) cube([post_x + 1, 5, lip_h + 1]);
            }
            for (k = [0, 1]) { lug(lug_x0[k], pin_ys[0], false); lug(lug_x0[k], pin_ys[1], true, k == 0); }
            for (o = ibt_pos) for (p = sq_holes(o, ibt_board, ibt_holes))
                radial_standoff_at(p, [o[0] + ibt_board/2, o[1] + ibt_board/2], 8);
            for (p = uno_pts()) standoff_at(p, 6.5);
            for (p = gps_pts) radial_standoff_at(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2], 7);
            // stiffening rib between the two rows of boards
            translate([6, ibt_pos[0][1] + ibt_board + 1, 0]) cube([plate_L - 12, 2.4, deck_t + 4]);
        }
        for (k = [0, 1]) {
            c = [ibt_pos[k][0] + ibt_board/2, ibt_pos[k][1] + ibt_board/2];
            for (p = sq_holes(ibt_pos[k], ibt_board, ibt_holes)) radial_slot_at(p, c);
            for (dx = [-12, -4, 4, 12]) translate([c[0] + dx, c[1], -1]) rotate([0, 0, 90]) slot(26, 4, deck_t + 2);
        }
        for (p = uno_pts()) bolt_at(p);
        gc = [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2];
        for (p = gps_pts) radial_slot_at(p, gc);
        // wire pass-throughs
        for (x = [7, plate_L/2, plate_L - 7])                  // beside / between the IBT-2s
            translate([x, ibt_pos[0][1] + ibt_board/2, -1]) rotate([0, 0, 90]) slot(22, 6, deck_t + 2);
        translate([uno_pos[0] + uno_L + 7, gps_pos[1] + gps_board/2, -1]) rotate([0, 0, 90]) slot(26, 8, deck_t + 2);
    }
}

// =====================================================================
//  stand-ins for the pictures (not printed)
// =====================================================================
module battery_ghost() {
    color("darkorange", 0.75) translate([bay_x0 + end_clear, bay_y0 + side_clear, floor_t])
        cube([bat_L, bat_W, bat_cap_h - cap_h]);
}
module cap_ghost() {                 // Power Wheel adapter: base flange + tower
    color("yellowgreen", 0.9) translate([(plate_L - cap_L)/2, (plate_W - cap_W)/2, floor_t + bat_cap_h - cap_h]) {
        cube([cap_L, cap_W, 8]);
        translate([cap_L/2 - 22, cap_W/2 - 20, 0]) cube([44, 40, cap_h]);
    }
}
module boards_ghost() {
    z = deck_t + standoff_h;
    for (o = ibt_pos) color("dimgray") translate([o[0], o[1], z]) cube([ibt_board, ibt_board, 43]);
    color("teal") translate([uno_pos[0], uno_pos[1], z]) cube([uno_L, uno_W, 12]);
    color("red") translate([gps_pos[0], gps_pos[1], z]) cube([gps_board, gps_board, 8]);
}
module chassis_ghost() { color("silver", 0.6) translate([0, 0, -2]) rrect(plate_L, plate_W, plate_r, 2); }

module assembly() {
    chassis_ghost();
    color("royalblue") base();
    for (i = [0, 1]) {
        color("lightsteelblue") translate([cx(i) - post_x/2, cy(0) - post_y/2, floor_t + explode]) post();
        // latch-side post: the same part turned round
        color("lightsteelblue") translate([cx(i) + post_x/2, cy(1) + post_y/2, floor_t + explode])
            rotate([0, 0, 180]) post();
    }
    translate([0, 0, bat_lift + explode*0.3]) battery_ghost();
    if (cap_aside) translate([0, plate_W + 30, -floor_t - bat_cap_h + cap_h + 8]) cap_ghost();
    else translate([0, 0, cap_lift + explode*0.8]) cap_ghost();
    translate([0, pin_ys[0], pin_z + explode*2.2]) rotate([open_deg, 0, 0])
        translate([0, -pin_ys[0], -pin_z]) translate([0, 0, deck_z]) {
            color("lightblue") deck();
            translate([0, 0, explode*0.6]) boards_ghost();
        }
}

if (part == "assembly") assembly();
else if (part == "base") base();
else if (part == "deck") deck();
else if (part == "post") rotate([0, 90, 0]) translate([-post_x, 0, 0]) post();  // lying on its side
