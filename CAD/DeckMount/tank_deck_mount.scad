// =====================================================================
//  Tank deck mount: Ryobi ONE+ battery cradle + two-tier electronics deck
//  Chassis: "Smart Robot Car" full-metal big tank kit (303 x 235 x 124 mm)
//  Electronics: Arduino UNO R4 WiFi, 2x IBT-2 (BTS7960), SparkFun ZED-F9P
//
//  Coordinates: X = along the direction of travel (X = 0 is the FRONT edge
//  of the top plate), Y = across the tank, Z = up from the top of the
//  chassis top plate.  Units: mm.
//
//  Battery access: the deck is hinged along the LEFT edge (Y = 0). Undo the
//  two thumb screws on the right, swing the deck up and over to the left
//  (it rests on a built-in stop at about 94 deg), lift the cap straight up
//  off the battery, then lift or slide the battery out.
//
//  Render one part at a time:  openscad -D 'part="base"' -o base.stl ...
//  part = "assembly" | "base" | "deck" | "hinge_post" | "latch_post" | "gate"
// =====================================================================

part = "assembly";
// assembly view controls (pictures only)
open_deg = 0;      // deck opening angle (0 = closed, ~94 = resting on the stop)
cap_lift = 0;      // how far the cap is lifted (mm)
bat_lift = 0;      // how far the battery is lifted (mm)
explode  = 0;      // exploded-view spacing (mm)
cap_aside = false; // pictures: cap set down beside the tank (wires still attached)

/* ---------- chassis top plate (measured from Images/BasePlate.png, 300 dpi scan) */
plate_L   = 151.6;    // along travel
plate_W   = 135.3;    // across tracks (kit drawing says 138)
plate_r   = 4;        // corner radius
hole_x    = [13.0, 138.8];   // corner screw holes, from front edge
hole_y    = [7.9, 127.3];    // corner screw holes, from left edge
// The 4 corner screws hold the plate to the kit's brass standoffs.
// They are replaced by screws 3 mm longer that pass through the cradle floor.

/* ---------- Ryobi ONE+ battery (footprint from Archive/Battery+VR.f3d) ---- */
bat_L     = 135;      // length (slide direction, along X)
bat_W     = 80;       // width
bat_cap_h = 150;      // battery standing rails-up WITH its cap fitted
                      // (measured by John, 2026-11-24)
cap_L     = 148;      // cap footprint, from Archive/RyobiBatteryHolder v7.f3d
cap_W     = 110;      // (the posts must clear this; see the assert below)
cap_h     = 40;       // cap height (for the pictures only)
bat_clear = 1.0;      // clearance per side in the bay

/* ---------- general ---------- */
floor_t   = 3.0;      // cradle floor thickness
wall_t    = 2.4;      // cradle wall thickness
wall_h    = 22;       // cradle wall height
deck_t    = 3.0;      // top deck thickness
lip_h     = 5;        // stiffening lip around top deck (points up)
gap_top   = 3;        // cap top to deck underside
m3        = 3.4;      // M3 clearance hole
m3_head   = 6.5;      // M3 socket head clearance
m3_nut    = 6.0;      // M3 nut across flats + clearance
post_x    = 14;       // post size along X
post_y    = 9.6;      // post size along Y (thin: it has to fit beside the cap)
post_xs   = [26, 116.6];        // post centres along X (clear of the corner screws)
cup_clear = 0.4;      // post-to-cup clearance
cup_h     = 14;       // socket cup height
cup_wall  = 2.4;      // cup walls front/back/inside
cup_out   = 1.0;      // cup wall on the outer edge of the plate

/* ---------- hinge (left edge) ---------- */
pin_y     = -8;       // hinge pin axis, outboard of the plate edge (over the track gap)
pin_dz    = 4.5;      // pin axis height above the deck top surface
lug_r     = 6;        // deck hinge lug radius
lug_w     = 6.4;      // deck hinge lug width (along X)
stop_h    = 10;       // stop block height above the pin
$fn = 40;

/* ---------- derived ---------- */
bay_W   = bat_W + 2*bat_clear;           // 82
bay_L   = bat_L + 2*bat_clear;           // 137
bay_y0  = (plate_W - bay_W)/2;           // 26.65
bay_x0  = 5 + wall_t;                    // inner front face of bay
bay_x1  = bay_x0 + bay_L;                // rear opening
post_L  = bat_cap_h + gap_top;           // cup floor to deck underside
deck_z  = floor_t + post_L;              // deck underside height
chan_w  = bay_y0 - wall_t - 11;          // side wire-channel window width
cup_ox  = post_x + 2*cup_clear + 2*cup_wall;          // cup size along X
cup_oy  = cup_out + post_y + 2*cup_clear + cup_wall;  // cup size along Y

// post centres: along the two long edges of the plate
function cx(i) = post_xs[i];
function cy(j) = j == 0 ? cup_out + cup_clear + post_y/2
                        : plate_W - (cup_out + cup_clear + post_y/2);
pin_z   = deck_z + deck_t + pin_dz;     // hinge pin height (absolute)
// deck hinge lugs sit just inboard (along X) of each hinge post
lug_x0  = [post_xs[0] + post_x/2 + 0.4, post_xs[1] - post_x/2 - 0.4 - lug_w];
post_inner = cup_out + cup_clear + post_y;   // inner face of the left posts
assert(post_inner < (plate_W - cap_W)/2 - 1,
       "posts collide with the battery cap: make post_y smaller");

module rrect(l, w, r, h) {
    translate([r, r, 0]) linear_extrude(h)
        offset(r) square([l - 2*r, w - 2*r]);
}
module slot(len, w, h) {            // rounded slot along X, centred
    hull() for (s = [-1, 1]) translate([s*(len - w)/2, 0, 0]) cylinder(d = w, h = h);
}
module ziptie_pair(h) {             // two 2 x 4 slots, 8 mm apart along X, for a cable tie
    for (s = [-1, 1]) translate([s*4, 0, 0]) cube([2, 4, h], center = true);
}

// =====================================================================
//  BASE / BATTERY CRADLE  (bolts to the chassis top plate)
// =====================================================================
module base() {
    difference() {
        union() {
            // skeleton floor
            difference() {
                rrect(plate_L, plate_W, plate_r, floor_t);
                // big windows under the battery: lighter, and they leave the
                // plate's own wire cut-outs reachable
                for (wx = [[bay_x0 + 8, 58], [bay_x0 + 74, 55]])
                    translate([wx[0], bay_y0 + 10, -1])
                        rrect(wx[1], bay_W - 20, 4, floor_t + 2);
                // side wire channels (left and right of the bay, between the cups)
                for (y = [3, plate_W - 3 - chan_w])
                    translate([post_xs[0] + cup_ox/2 + 3, y, -1])
                        rrect(post_xs[1] - post_xs[0] - cup_ox - 6, chan_w, 3, floor_t + 2);
            }
            // battery bay walls: front + two sides, rear left open for sliding
            translate([bay_x0 - wall_t, bay_y0 - wall_t, 0])
                cube([wall_t, bay_W + 2*wall_t, wall_h]);
            for (y = [bay_y0 - wall_t, bay_y0 + bay_W])
                translate([bay_x0 - wall_t, y, 0]) cube([bay_L + wall_t, wall_t, wall_h]);
            // rear gate pillars
            for (y = [bay_y0 - 7, bay_y0 + bay_W])
                translate([bay_x1 - 1, y, 0]) cube([8, 7, wall_h]);
            // socket cups for the deck posts, along the long edges
            for (i = [0, 1]) {
                translate([cx(i) - cup_ox/2, 0, 0]) cube([cup_ox, cup_oy, cup_h]);
                translate([cx(i) - cup_ox/2, plate_W - cup_oy, 0]) cube([cup_ox, cup_oy, cup_h]);
            }
        }
        // cup pockets
        for (i = [0, 1], j = [0, 1])
            translate([cx(i), cy(j), floor_t + cup_h/2])
                cube([post_x + 2*cup_clear, post_y + 2*cup_clear, cup_h], center = true);
        // chassis screw holes (true positions)
        for (i = [0, 1], j = [0, 1])
            translate([hole_x[i], hole_y[j], -1]) cylinder(d = m3, h = floor_t + 2);
        // cross-pin through the cups (along X), locks the posts in
        for (i = [0, 1], j = [0, 1])
            translate([cx(i), cy(j), floor_t + 6]) rotate([0, 90, 0])
                cylinder(d = m3, h = cup_ox + 2, center = true);
        // gate slots in the pillars (open at the top)
        for (y = [bay_y0 - 4, bay_y0 + bay_W])
            translate([bay_x1 + 2, y, floor_t + 2]) cube([2.8, 4, wall_h]);
        // cable-tie slots along the outside of the side walls (bucks, wiring)
        for (x = [35, 75, 115], y = [3 + chan_w + 3.5, plate_W - 3 - chan_w - 3.5])
            translate([x, y, floor_t/2]) ziptie_pair(floor_t + 2);
        // finger notch in the front wall to push the battery out
        translate([bay_x0 - wall_t - 1, plate_W/2, wall_h])
            rotate([0, 90, 0]) cylinder(d = 30, h = wall_t + 2);
    }
}

// =====================================================================
//  REAR GATE  (optional: drops into the pillar slots and stops the
//  battery sliding backwards)
// =====================================================================
module gate() {
    gw = bay_W + 2*4 - 0.6;
    difference() {
        union() {
            cube([gw, wall_h - 2, 2.4]);                     // printed flat
            translate([gw/2 - 12, wall_h - 2, 0]) cube([24, 8, 2.4]);   // pull tab
        }
        translate([gw/2, wall_h + 2.5, -1]) cylinder(d = 6, h = 5);   // finger hole
    }
}

// =====================================================================
//  POSTS  (printed lying on their side)
//  latch_post x2: right-hand side, deck held down by an M3 thumb screw
//  hinge_post x2: left-hand side, carries the hinge pin and the open stop
// =====================================================================
module hinge_post() {
    py0 = cup_out + cup_clear;           // absolute Y of the post's outer face
    ply = pin_y - py0;                   // pin Y in post coordinates
    plz = post_L + deck_t + pin_dz;      // pin Z in post coordinates
    difference() {
        union() {
            cube([post_x, post_y, post_L]);
            // arm out to the pin, below deck level, with a gusset
            hull() {
                translate([0, ply - 4.5, post_L - 10]) cube([post_x, post_y - ply + 4.5, 10]);
                translate([0, 0, post_L - 34]) cube([post_x, 1, 1]);
            }
            // stop block around the pin. The deck's edge lands on its inner
            // face at about 94 deg and holds the deck open.
            translate([0, ply - 4.5, post_L - 1]) cube([post_x, 8.5, plz - post_L + 1 + stop_h]);
        }
        // hinge pin (M3 x 25 along X, nylock nut on the lug side)
        translate([-1, ply, plz]) rotate([0, 90, 0]) cylinder(d = m3, h = post_x + 2);
        // cross-pin hole (matches the cup)
        translate([-1, post_y/2, 6]) rotate([0, 90, 0]) cylinder(d = m3, h = post_x + 2);
    }
}

module latch_post() {
    difference() {
        cube([post_x, post_y, post_L]);
        // cross-pin hole (matches the cup, runs along X)
        translate([-1, post_y/2, 6]) rotate([0, 90, 0]) cylinder(d = m3, h = post_x + 2);
        // top: M3 screw from the deck into a captive nut
        translate([post_x/2, post_y/2, post_L - 16]) cylinder(d = m3, h = 17);
        translate([-1, post_y/2 - m3_nut/2, post_L - 10])
            cube([post_x/2 + 1 + m3_nut/2, m3_nut, 2.8]);      // nut slot from the end
    }
}

// =====================================================================
//  TOP DECK  (electronics; IBT-2s at the front, UNO + GPS at the rear)
// =====================================================================
ibt_holes = 40;       // IBT-2 hole square, from Archive/MotorBox.f3d
ibt_board = 50;
gps_holes = 38.1;     // SparkFun GPS-RTK2 (1.5 in), from Archive/GPSBox.f3d
gps_board = 43.2;
uno_L = 68.6;  uno_W = 53.3;
// UNO R4 hole positions (Arduino UNO datasheet), board origin bottom-left,
// USB-C / barrel jack on the X = 0 edge
uno_holes = [[13.97, 2.54], [15.24, 50.80], [66.04, 7.62], [66.04, 35.56]];
standoff_h = 6;

// board placements on the deck (lower-left corner of each board)
ibt_pos = [[30, 8], [30, plate_W - 8 - ibt_board]];     // front, left / right
uno_pos = [plate_L - uno_L + 1, 22];                    // rear-left, USB faces rear (1 mm overhang)
gps_pos = [plate_L - 24 - gps_board, plate_W - 12 - gps_board];  // rear-right

module standoff_at(p, d = 7) {
    translate([p[0], p[1], 0]) cylinder(d = d, h = deck_t + standoff_h);
}
module bolt_at(p) {
    translate([p[0], p[1], -1]) cylinder(d = m3, h = deck_t + standoff_h + 2);
}
// radial slot (towards board centre) so +-2.5 mm hole-spacing error still fits
module radial_slot_at(p, c) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], -1]) rotate([0, 0, a]) slot(m3 + 5, m3, deck_t + standoff_h + 2);
}
module radial_standoff_at(p, c, d) {       // pill-shaped boss that matches the slot
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], 0]) rotate([0, 0, a]) slot(5 + d, d, deck_t + standoff_h);
}
function sq_holes(o, b, s) = [for (i = [0, 1], j = [0, 1])
    [o[0] + (b - s)/2 + i*s, o[1] + (b - s)/2 + j*s]];
function uno_pts() = [for (h = uno_holes) [uno_pos[0] + uno_L - h[0], uno_pos[1] + uno_W - h[1]]]; // rotated 180

module deck() {
    ibt_pts = [for (o = ibt_pos) each sq_holes(o, ibt_board, ibt_holes)];
    gps_pts = sq_holes(gps_pos, gps_board, gps_holes);
    difference() {
        union() {
            difference() {
                rrect(plate_L, plate_W, plate_r, deck_t + lip_h);
                translate([3, 3, deck_t]) rrect(plate_L - 6, plate_W - 6, plate_r - 1, lip_h + 1);
                // no lip where the hinge posts' stop blocks swing past
                for (x = post_xs) translate([x - post_x/2 - 0.5, -1, deck_t]) cube([post_x + 1, 5, lip_h + 1]);
            }
            // landing pads for the thumb screws (right-hand side only)
            for (i = [0, 1]) translate([cx(i), cy(1), 0]) cylinder(d = 12, h = deck_t + 1);
            // hinge lugs on the left edge
            for (x = lug_x0) hull() {
                translate([x, pin_y, deck_t + pin_dz]) rotate([0, 90, 0]) cylinder(r = lug_r, h = lug_w);
                translate([x, pin_y - lug_r, 0]) cube([lug_w, lug_r - pin_y + 4, deck_t]);
            }
            for (o = ibt_pos) for (p = sq_holes(o, ibt_board, ibt_holes))
                radial_standoff_at(p, [o[0] + ibt_board/2, o[1] + ibt_board/2], 8);
            for (p = uno_pts()) standoff_at(p, 6.5);
            for (p = gps_pts) radial_standoff_at(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2], 7);
            // centre spine rib for stiffness
            translate([14, plate_W/2 - 1.2, 0]) cube([ibt_pos[0][0] + ibt_board - 14, 2.4, deck_t + 4]);
        }
        // thumb screws (M3 x 12, right-hand side) into the latch posts' captive nuts
        for (i = [0, 1]) translate([cx(i), cy(1), -1]) cylinder(d = m3, h = 10);
        // hinge pin holes
        for (x = lug_x0) translate([x - 1, pin_y, deck_t + pin_dz]) rotate([0, 90, 0])
            cylinder(d = m3, h = lug_w + 2);
        // IBT-2: radial slots + air slots under each board
        for (k = [0, 1]) {
            c = [ibt_pos[k][0] + ibt_board/2, ibt_pos[k][1] + ibt_board/2];
            for (p = sq_holes(ibt_pos[k], ibt_board, ibt_holes)) radial_slot_at(p, c);
            for (dx = [-12, -4, 4, 12]) translate([c[0] + dx, c[1], -1])
                rotate([0, 0, 90]) slot(26, 4, deck_t + 2);
        }
        // UNO: plain M3 holes (datasheet positions)
        for (p = uno_pts()) bolt_at(p);
        // GPS: radial slots
        gc = [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2];
        for (p = gps_pts) radial_slot_at(p, gc);
        // wire pass-throughs
        // front: motor leads come up here, right in front of the IBT-2 outputs
        for (y = [40, plate_W - 40]) translate([14, y, -1]) rotate([0, 0, 90]) slot(22, 9, deck_t + 2);
        // between drivers and UNO: battery feed + PWM leads
        for (y = [30, plate_W - 30]) translate([ibt_pos[0][0] + ibt_board + 1.5, y, -1])
            rotate([0, 0, 90]) slot(18, 6, deck_t + 2);
        translate([uno_pos[0] + 20, uno_pos[1] + uno_W/2, -1]) slot(24, 9, deck_t + 2);
        translate([gps_pos[0] - 6, gps_pos[1] + gps_board/2, -1]) rotate([0, 0, 90]) slot(20, 7, deck_t + 2);
        // cable-tie slots along both edges (rear half)
        for (x = [88, 118], y = [7, plate_W - 7])
            translate([x, y, deck_t/2]) ziptie_pair(deck_t + 2);
    }
}

// =====================================================================
//  stand-in models for the assembly view only (not printed)
// =====================================================================
module battery_ghost() {
    color("darkorange", 0.75) translate([bay_x0 + bat_clear, bay_y0 + bat_clear, floor_t])
        cube([bat_L, bat_W, bat_cap_h - cap_h]);
}
module cap_ghost() {
    color("gold", 0.8) translate([(plate_L - cap_L)/2, (plate_W - cap_W)/2, floor_t + bat_cap_h - cap_h])
        cube([cap_L, cap_W, cap_h]);
}
module boards_ghost() {           // in deck coordinates
    z = deck_t + standoff_h;
    for (o = ibt_pos) color("dimgray") translate([o[0], o[1], z]) cube([ibt_board, ibt_board, 43]);
    color("teal") translate([uno_pos[0], uno_pos[1], z]) cube([uno_L, uno_W, 12]);
    color("red") translate([gps_pos[0], gps_pos[1], z]) cube([gps_board, gps_board, 8]);
}
module chassis_ghost() {
    color("silver", 0.6) translate([0, 0, -2]) rrect(plate_L, plate_W, plate_r, 2);
}
module place_post(i, j) {
    translate([cx(i) - post_x/2, cy(j) - post_y/2, floor_t + explode]) children();
}

module assembly() {
    chassis_ghost();
    color("royalblue") base();
    color("royalblue") translate([bay_x1 + 2.1, bay_y0 - 4 + 0.3, floor_t + 2 + explode*0.5])
        rotate([90, 0, 90]) gate();
    for (i = [0, 1]) {
        color("lightsteelblue") place_post(i, 0) hinge_post();
        color("lightsteelblue") place_post(i, 1) latch_post();
    }
    translate([0, 0, bat_lift + explode*0.3]) battery_ghost();
    if (cap_aside) translate([0, plate_W + 25, -floor_t - bat_cap_h + cap_h + 20]) cap_ghost();
    else translate([0, 0, cap_lift + explode*0.8]) cap_ghost();
    // deck rotates about the hinge pin axis (along X)
    translate([0, pin_y, pin_z + explode*2.2]) rotate([open_deg, 0, 0])
        translate([0, -pin_y, -pin_z]) translate([0, 0, deck_z]) {
            color("lightblue") deck();
            translate([0, 0, explode*0.6]) boards_ghost();
        }
}

if (part == "assembly") assembly();
else if (part == "base") base();
else if (part == "deck") deck();
else if (part == "gate") gate();
// posts lie on their side (on the +X end face) for strength
else if (part == "hinge_post") rotate([0, 90, 0]) translate([-post_x, 0, 0]) hinge_post();
else if (part == "latch_post") rotate([0, 90, 0]) translate([-post_x, 0, 0]) latch_post();
