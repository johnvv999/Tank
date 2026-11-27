// =====================================================================
//  Rear hitch: quick-swap mount for a trailer (lawn-mowing trailer) or a
//  servo-lifted tool (230 mm sand rake).
//
//  Tractor-style, simplified:
//    - two LOWER PINS (M4 x 30, R-clip) that a tool or a trailer drawbar
//      hangs on, pivoting up and down;
//    - one LIFT POINT: a DS3235 (35 kg.cm) servo in the hitch head swings a
//      lift arm; a short link pins from the arm to the tool's mast.
//  Swapping tools = pull 3 pins.  Trailers use a drawbar that hangs on the
//  same two pins, with a vertical swivel pin at its back end.
//
//  The hitch bolts to the METAL chassis plate: a clamp under the plate's
//  rear edge, 4x countersunk M3 down through the battery cradle floor,
//  the rear tray tongue and the plate's two long rear slots.  The rear
//  tray floor is also screwed down onto the hitch beam (2x M3), so a bump
//  on the tray goes into the metal too.
//
//  The servo cable runs in a channel along the top of the beam and comes
//  up through the rear tray's wire slot to the UNO (D3).  No wire shows.
//
//  part = "hitch" | "lid" | "lift_arm" | "link" | "rake_frame" | "rake_bar"
//       | "drawbar" | "hitch_asm" (everything in place, for renders)
// =====================================================================
include <tank_deck_mount.scad>

part = "hitch_asm";
tool = "rake";        // "rake" | "trailer" | "none" (renders)
rake_lift = 0;        // rake angle about the lower pins (deg): -8 = 8 deg deeper, +28 = fully raised (max)

/* ---------- interface ---------- */
plate_t  = 3;          // metal plate thickness (John measured)
pin_d    = 4.2;        // lower pins / link pins: M4
// Real chassis (John's photos, 2026-11-25): behind the plate's rear edge the lower
// chassis runs on for 100 mm with its top 30 mm below the plate; the tracks end
// about 140 mm behind the edge; the plate top is 130 mm above the ground (John measured).
hdx = plate_L - 140;  cy = plate_W/2;   // the hitch was laid out on a 140 x 120 plate; shift it to the real one
lower_L  = 100;  lower_z = -30;  ground_z = -130;
hb_z0    = lower_z + 3;   // bottom of the hitch (flat, print bed): 3 mm above the lower chassis
sx = 250 + hdx; sy = cy; sz = -14;          // servo output shaft (centre of the 60 mm face)
P  = [278 + hdx, hb_z0 + 8];                     // lower pin axis (x, z)
pin_y = [cy - 30, cy + 30];                     // lower pin pairs (tool arms sit between the ears)
ear_t = 4; ear_gap = 7.8;
arm_len = 30;                         // lift arm, shaft to link pin
mast_h  = 30;                         // tool mast top above the lower pins
link_y  = cy + 6.5;                       // link plane

/* ---------- DS3235 servo ---------- */
sv_L = 40; sv_W = 20; sv_H = 40.5;    // body (shaft along H)
sv_ear_L = 54.5; sv_ear_t = 2.5; sv_ear_h = 28;   // ears 28 mm up from the bottom
// lying on its side in the head, shaft pointing +Y at (sx, sy, sz)
sv_x0 = sx - 30;  sv_y0 = sy - sv_H;

/* ---------- beam / head ---------- */
clamp_x0 = plate_L - 17;  clamp_y = [16, 118];  clamp_t = 8;   // clamp stays clear of the rear corner screws' nuts
beam_y   = [cy - 25, cy + 25];
head_x   = [210 + hdx, 270 + hdx];  head_y = [cy - 44, cy + 44];  head_top = -4;
beam_top = tray_z - 0.5;   // just under the rear tray floor

module rbox(x0, x1, y0, y1, z0, z1, r) {   // box with vertical edges rounded
    translate([x0, y0, z0]) rrect(x1 - x0, y1 - y0, min(r, (x1 - x0)/2 - 0.01, (y1 - y0)/2 - 0.01), z1 - z0);
}
module ear_pair(y) {
    for (s = [-1, 1]) translate([head_x[1] - 2, y + s*(ear_gap/2 + ear_t/2) - ear_t/2, hb_z0])
        hull() {
            cube([2, ear_t, 16]);
            translate([P[0] - head_x[1] + 2, 0, P[1] - hb_z0]) rotate([-90, 0, 0]) cylinder(r = 8, h = ear_t);
        }
}

module hitch() {
    difference() {
        union() {
            // clamp under the plate's rear edge, wings chamfered 45 deg underneath (prints without support)
            hull() {
                rbox(clamp_x0, plate_L, clamp_y[0], clamp_y[1], -plate_t - clamp_t, -plate_t, 4);
                rbox(clamp_x0 + 20, plate_L, beam_y[0], beam_y[1], hb_z0, hb_z0 + 1, 4);
            }
            // beam under the rear tray: below the plate up to its edge, full height behind it
            rbox(plate_L - 2, plate_L + 4, beam_y[0], beam_y[1], hb_z0, -plate_t, 1);
            rbox(plate_L + 0.5, head_x[0] + 5, beam_y[0], beam_y[1], hb_z0, beam_top, 3);
            // head: servo housing, 5 mm rounded outside edges
            hull() {
                rbox(head_x[0], head_x[1], head_y[0], head_y[1], hb_z0, head_top - rnd, rnd);
                for (x = [head_x[0] + rnd, head_x[1] - rnd], y = [head_y[0] + rnd, head_y[1] - rnd])
                    translate([x, y, head_top - rnd]) sphere(r = rnd);
            }
            for (y = pin_y) ear_pair(y);
        }
        // bolts up into the plate: hex nut pockets from below
        for (y = hitch_bolt_y) translate([hitch_bolt_x, y, 0]) {
            translate([0, 0, -20]) cylinder(d = m3, h = 30);
            translate([0, 0, -60]) cylinder(d = 6.4, h = 60 - plate_t - 5, $fn = 6);
        }
        // M3 nut pockets for the two screws coming down from the rear tray floor
        for (x = [plate_L + 20, plate_L + 60]) translate([x, cy + 2.5, 0]) {
            translate([0, 0, -12]) cylinder(d = m3, h = 13);
            translate([0, 0, -12 - 30]) cylinder(d = 6.4, h = 30, $fn = 6);
        }
        // servo pocket (open top), ear slot, horn clearance
        translate([sv_x0 - 0.3, sv_y0 - 0.3, sz - sv_W/2 - 0.3]) cube([sv_L + 0.6, sv_H + 0.6, 40]);
        translate([sx - 10 - sv_ear_L/2 - 0.4, sv_y0 + sv_ear_h - 0.3, sz - sv_W/2 - 0.3]) cube([sv_ear_L + 0.8, sv_ear_t + 0.6, 40]);
        // lift-arm slot through the head (arm swings in the X-Z plane at y 61..66)
        translate([sx - 14, sy + 0.5, sz - 12]) cube([head_x[1] - sx + 15, 6, 40]);
        // cable channel: servo pocket -> along the beam top -> up through the rear tray's wire slot
        translate([plate_L + 4, sy - 4, beam_top - 8]) cube([head_x[0] - plate_L + 12, 8, 9]);
        // lower pins
        for (y = pin_y) translate([P[0], y - 20, P[1]]) rotate([-90, 0, 0]) cylinder(d = pin_d, h = 40);
        // servo ear screws (M3 into the head walls, along Y)
        for (dx = [-sv_ear_L/2 + 3.5, sv_ear_L/2 - 3.5], dz = [-5, 5])
            translate([sx - 10 + dx, sv_y0 + sv_ear_h - 15, sz + dz]) rotate([-90, 0, 0]) cylinder(d = 2.8, h = 15);
    }
}
// lid over the servo pocket (2x M3 into the head)
module lid() {
    difference() {
        hull() for (x = [sv_x0 - 2 + 3, sv_x0 + sv_L + 2 - 3], y = [sv_y0 - 2 + 3, sy - 3]) translate([x, y, head_top - 0.01]) cylinder(r = 3, h = 2.4);
        translate([sx - 14, sy - 6, head_top - 1]) cube([30, 7, 5]);
    }
}

// ---------------- lift linkage ----------------
function arm_tip(th) = [sx + arm_len*cos(th), sz + arm_len*sin(th)];
function rot2(p, a) = [p[0]*cos(a) - p[1]*sin(a), p[0]*sin(a) + p[1]*cos(a)];
mast0 = [0, mast_h];                                         // relative to the lower pins at rake_lift = 0
function mast_at(phi) = P + rot2(mast0, phi);
link_len = norm(arm_tip(90) - mast_at(0));                   // arm straight up = rake on the ground
function d2(a, b) = (a[0]-b[0])*(a[0]-b[0]) + (a[1]-b[1])*(a[1]-b[1]);
function solve_th(phi) = let(m = mast_at(phi),
    c = [for (t = [40 : 0.5 : 200]) [abs(sqrt(d2(arm_tip(t), m)) - link_len), t]]) [for (e = c) if (e[0] == min([for (k = c) k[0]])) e[1]][0];

module lift_arm(th = 90) {
    translate([sx, sy + 1, sz]) rotate([90, 0, 0]) mirror([0, 0, 1]) rotate([0, 0, th]) linear_extrude(4) difference() {
        hull() { circle(r = 9); translate([arm_len, 0]) circle(r = 5); }
        circle(d = 6); for (a = [0:90:270]) rotate(a) translate([7, 0]) circle(d = 2.2);
        translate([arm_len, 0]) circle(d = pin_d);
    }
}
module link_bar(a, b) {   // a, b in X-Z
    translate([0, link_y, 0]) rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(3.5) difference() {
        hull() { translate(a) circle(r = 4.5); translate(b) circle(r = 4.5); }
        translate(a) circle(d = pin_d); translate(b) circle(d = pin_d);
    }
}

// ---------------- tools ----------------
rake_W = 230; tine_L = 28; tine_t = 4; tine_pitch = 14;
rake_bar_rel = [50, ground_z + 6 + tine_L - P[1]];   // rake bar centre relative to the lower pins: tines touch the ground at rake_lift 0
module tool_frame_2d_arm() { hull() { circle(r = 7); translate(rake_bar_rel) circle(r = 6); } }
module rake_frame(phi = 0) {
    translate([P[0], 0, P[1]]) rotate([0, -phi, 0]) {
        difference() {
            union() {
                for (y = pin_y) translate([0, y + ear_gap/2 - 0.2, 0]) rotate([90, 0, 0]) linear_extrude(ear_gap - 0.4) tool_frame_2d_arm();
                // cross tube + mast
                translate([12, pin_y[0], -6]) cube([10, pin_y[1] - pin_y[0], 10]);
                translate([0, link_y - 9, 0]) rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(12)
                    hull() { translate([16, 0]) circle(r = 5); translate(mast0) circle(r = 5); }
            }
            for (y = pin_y) translate([0, y - 10, 0]) rotate([-90, 0, 0]) cylinder(d = pin_d, h = 20);
            translate([mast0[0], link_y - 10, mast0[1]]) rotate([-90, 0, 0]) cylinder(d = pin_d, h = 20);
            translate([mast0[0] - 6, link_y - 3.2, mast0[1] - 8]) cube([12, 3.9, 16]);   // link slot
        }
    }
}
module rake_bar(phi = 0) {
    translate([P[0], 0, P[1]]) rotate([0, -phi, 0]) translate([rake_bar_rel[0], cy - rake_W/2, rake_bar_rel[1]]) {
        translate([-7, 0, -7]) rotate([0, 0, 0]) hull() for (y = [3, rake_W - 3]) translate([7, y, 7]) rotate([0, 90, 0]) translate([0, 0, -7]) cylinder(r = 7, h = 14);
        for (y = [tine_pitch/2 : tine_pitch : rake_W - 1])
            translate([0, y, -6]) hull() {
                translate([-5, -tine_t/2, 0]) cube([10, tine_t, 0.1]);
                translate([1, -tine_t/2, -tine_L]) cube([2, tine_t, 0.1]);
            }
    }
}
module drawbar() {   // trailer adapter: hangs on the two lower pins, held level by the link, swivel pin at the back
    translate([P[0], 0, P[1]]) difference() {
        union() {
            for (y = pin_y) translate([0, y + ear_gap/2 - 0.2, 0]) rotate([90, 0, 0]) linear_extrude(ear_gap - 0.4)
                hull() { circle(r = 7); translate([30, 0]) circle(r = 7); }
            hull() { translate([22, pin_y[0], -7]) cube([10, pin_y[1] - pin_y[0], 14]); translate([58, cy, -7]) cylinder(r = 10, h = 14); }
            translate([0, link_y - 9, 0]) rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(12) hull() { translate([22, 0]) circle(r = 5); translate(mast0) circle(r = 5); }
        }
        for (y = pin_y) translate([0, y - 10, 0]) rotate([-90, 0, 0]) cylinder(d = pin_d, h = 20);
        translate([mast0[0], link_y - 10, mast0[1]]) rotate([-90, 0, 0]) cylinder(d = pin_d, h = 20);
        translate([mast0[0] - 6, link_y - 3.2, mast0[1] - 8]) cube([12, 3.9, 16]);
        translate([58, cy, -20]) cylinder(d = 6.5, h = 40);          // trailer swivel pin (6 mm)
    }
}
module servo_ghost() {
    color("#202020") {
        translate([sv_x0, sv_y0, sz - sv_W/2]) cube([sv_L, sv_H, sv_W]);
        translate([sx - 10 - sv_ear_L/2, sv_y0 + sv_ear_h, sz - sv_W/2]) cube([sv_ear_L, sv_ear_t, sv_W]);
        translate([sx, sy, sz]) rotate([-90, 0, 0]) cylinder(d = 6, h = 4);
    }
}

module hitch_asm() {
    th = solve_th(rake_lift);
    color("#2b2d31") hitch(); color("#2b2d31") lid();
    servo_ghost();
    color("silver") lift_arm(th);
    if (tool == "rake") {
        color("#3a3d42") rake_frame(rake_lift); color("#3a3d42") rake_bar(rake_lift);
        color("silver") link_bar(arm_tip(th), mast_at(rake_lift));
    }
    if (tool == "trailer") { color("#3a3d42") drawbar(); color("silver") link_bar(arm_tip(90), mast_at(0)); }
}

if (part == "hitch_asm") hitch_asm();
else if (part == "hitch") translate([0, 0, -hb_z0]) hitch();       // printed upright, flat bottom
else if (part == "lid") translate([0, 0, -head_top]) lid();
else if (part == "lift_arm") translate([0, 0, 4]) rotate([-90, 0, 0]) translate([-sx, -sy - 1, -sz]) lift_arm(0);   // flat
else if (part == "link") translate([0, 0, link_y + 3.5]) rotate([-90, 0, 0]) link_bar([0, 0], [link_len, 0]);   // flat
else if (part == "rake_frame") translate([0, 0, -(P[1] + rake_bar_rel[1] - 6)]) rake_frame(0);   // as installed: arms slope about 60 deg, cross tube bridges
else if (part == "rake_bar") translate([0, 0, P[1] + rake_bar_rel[1] + 7]) rotate([180, 0, 0]) rake_bar(0);   // tines up
else if (part == "drawbar") translate([0, 0, 7 - P[1]]) drawbar();

// ---------------- stand-in trailer for the renders (not printed) ----------------
module trailer_ghost() {
    sw = P[0] + 58;   // swivel pin x
    translate([0, 0, 0]) {
        g = ground_z - (-96.5);   // drop the stand-in trailer to the ground
        hull() { translate([sw, cy, P[1] - 4]) cylinder(r = 8, h = 6); translate([sw + 70, cy, -40 + g]) cylinder(r = 8, h = 6); }   // tongue
        translate([sw + 70, cy - 90, -40 + g]) rrect(170, 180, 10, 6);                                  // frame
        translate([sw + 155, cy, -88 + g]) cylinder(d = 170, h = 34);                               // mower deck
        translate([sw + 155, cy, -54 + g]) cylinder(d = 46, h = 50);                                // mower motor
        for (y = [cy - 104, cy + 90]) translate([sw + 155, y + 7, -61.5 + g]) rotate([90, 0, 0]) cylinder(d = 70, h = 14, center = true);   // wheels
    }
}
