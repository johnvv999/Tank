// =====================================================================
//  Tank deck mount: Ryobi P108 battery + Power Wheel adapter in the
//  middle, electronics on trays that overhang the front and rear.
//  Chassis: "Smart Robot Car" full-metal big tank kit, plate 140 x 120.
//
//  Coordinates: X = along the direction of travel (the plate's 140 mm
//  side; X = 0 is the FRONT 120 mm edge), Y = across, Z = up from the
//  top of the chassis plate.  Units: mm.
//
//  Nothing sits above the battery: lift the adapter straight up, then
//  lift the battery out.  The two IBT-2s ride on the front tray, the
//  UNO R4 WiFi and the GPS on the rear tray.
//
//  Each tray has a 1.5 mm tongue that slides UNDER the cradle's
//  stepped floor ends (a half-lap joint).  The chassis corner screws go
//  through tongue + cradle, so each tray is clamped straight to the
//  metal plate by two screws.
//
//  Render one part at a time:  openscad -D 'part="base"' -o base.stl ...
//  part = "assembly" | "base" | "front_tray" | "rear_tray"
// =====================================================================

part = "assembly";
cap_lift = 0;      // pictures: adapter lifted (mm)
bat_lift = 0;      // pictures: battery lifted (mm)
explode  = 0;      // pictures: exploded spacing (mm)

/* ---------- chassis plate ---------- */
plate_L  = 140;    // along travel (John)
plate_W  = 120;    // across (John)
plate_r  = 4;
// Corner screw holes: traced from Images/BasePlate.png and scaled to the
// 140 x 120 outline. APPROXIMATE: measure and correct before printing.
hole_x   = [12.0, 128.2];
hole_y   = [7.0, 112.9];

/* ---------- battery + adapter ---------- */
bat_L = 135; bat_W = 80;          // P108 footprint (Archive/Battery+VR.f3d)
bat_cap_h = 150;                  // battery + adapter (John)
cap_L = 84; cap_W = 75; cap_h = 90;   // Power Wheel adapter
end_clear = 0.75; side_clear = 1.0;

/* ---------- trays ---------- */
front_ovh = 62;    // front tray reaches this far past the front edge
rear_ovh  = 74;    // rear tray reaches this far past the rear edge
tray_W    = 116;   // narrower than the plate, to clear the side frames
lap_L     = 26;    // tongue length under the cradle
lap_t     = 1.5;   // tongue thickness (half of the 3 mm floor)

/* ---------- general ---------- */
floor_t = 3.0; wall_t = 2.4; wall_h = 22; end_t = 1.6; end_h = 12;
lip_h = 6; m3 = 3.4; standoff_h = 6;
$fn = 40;

bay_L  = bat_L + 2*end_clear;  bay_W = bat_W + 2*side_clear;
bay_x0 = (plate_L - bay_L)/2;  bay_y0 = (plate_W - bay_W)/2;
ty0    = (plate_W - tray_W)/2;

assert(bay_L + 2*end_t <= plate_L + 0.01, "battery too long for the plate");
assert(hole_x[0] < lap_L - 4 && hole_x[1] > plate_L - lap_L + 4, "corner screws must be inside the laps");

module rrect(l, w, r, h) { translate([r, r, 0]) linear_extrude(h) offset(r) square([l - 2*r, w - 2*r]); }
module slot(len, w, h) { hull() for (s = [-1, 1]) translate([s*(len - w)/2, 0, 0]) cylinder(d = w, h = h); }
module ziptie_pair(h) { for (s = [-1, 1]) translate([0, s*4, 0]) cube([4, 2, h], center = true); }

// =====================================================================
//  BASE / BATTERY CRADLE
// =====================================================================
module base() {
    difference() {
        union() {
            // floor: full 3 mm in the middle, upper 1.5 mm only over the laps
            intersection() {
                rrect(plate_L, plate_W, plate_r, floor_t);
                union() {
                    translate([lap_L, -1, 0]) cube([plate_L - 2*lap_L, plate_W + 2, floor_t]);
                    translate([-1, -1, lap_t]) cube([plate_L + 2, plate_W + 2, floor_t]);
                }
            }
            for (y = [bay_y0 - wall_t, bay_y0 + bay_W])
                translate([bay_x0 - end_t, y, lap_t]) cube([bay_L + 2*end_t, wall_t, wall_h - lap_t]);
            for (x = [bay_x0 - end_t, bay_x0 + bay_L])
                translate([x, bay_y0 - wall_t, lap_t]) cube([end_t, bay_W + 2*wall_t, end_h - lap_t]);
        }
        // windows under the middle of the battery
        for (x = [lap_L + 6, plate_L/2 + 3])
            translate([x, bay_y0 + 10, -1]) rrect(plate_L/2 - lap_L - 9, bay_W - 20, 4, floor_t + 2);
        // side wire channels
        for (y = [4, plate_W - 4 - (bay_y0 - wall_t - 8)])
            translate([lap_L + 6, y, -1]) rrect(plate_L - 2*lap_L - 12, bay_y0 - wall_t - 8, 2, floor_t + 2);
        // corner screws
        for (x = hole_x, y = hole_y) translate([x, y, -1]) cylinder(d = m3, h = floor_t + 2);
        // finger scoops in the side walls to grip the battery
        for (y = [bay_y0 - wall_t - 1, bay_y0 + bay_W - 1])
            translate([plate_L/2, y, wall_h]) rotate([-90, 0, 0]) cylinder(d = 34, h = wall_t + 2);
    }
}

// =====================================================================
//  TRAYS  (built for the FRONT end: tray runs from X = -ovh to X = lap_L)
// =====================================================================
ibt_holes = 40;   ibt_board = 50;      // Archive/MotorBox.f3d
gps_holes = 38.1; gps_board = 43.2;    // Archive/GPSBox.f3d (SparkFun GPS-RTK2)
uno_L = 68.6;  uno_W = 53.3;
uno_holes = [[13.97, 2.54], [15.24, 50.80], [66.04, 7.62], [66.04, 35.56]];

function sq_holes(o, b, s) = [for (i = [0, 1], j = [0, 1]) [o[0] + (b - s)/2 + i*s, o[1] + (b - s)/2 + j*s]];
module radial_standoff(p, c, d) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], 0]) rotate([0, 0, a]) slot(5 + d, d, floor_t + standoff_h);
}
module radial_slot(p, c) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], -1]) rotate([0, 0, a]) slot(m3 + 5, m3, floor_t + standoff_h + 2);
}

// board positions (lower-left corner, tray coordinates = chassis X/Y)
ibt_pos = [[-front_ovh + 6, ty0 + 3], [-front_ovh + 6, plate_W - ty0 - 3 - ibt_board]];
// rear tray is modelled at the front and then mirrored, so these X are "outboard distance"
uno_pos = [-rear_ovh + 3, ty0 + 3];               // UNO 68.6 runs along X, USB-C + barrel jack face the rear
gps_pos = [-rear_ovh + 10, plate_W - ty0 - 4 - gps_board];
function uno_pts() = [for (h = uno_holes) [uno_pos[0] + h[0], uno_pos[1] + h[1]]];

module tray_body(ovh) {
    difference() {
        union() {
            // tray plate + tongue
            hull() {
                translate([-ovh, ty0, 0]) rrect(4, tray_W, 2, floor_t);
                translate([-1, ty0, 0]) cube([1, tray_W, floor_t]);
            }
            intersection() {
                rrect(plate_L, plate_W, plate_r, lap_t);
                translate([-1, -1, 0]) cube([lap_L + 1, plate_W + 2, lap_t]);
            }
            // lips on the three outer edges, tapering into deep side rails
            difference() {
                translate([-ovh, ty0, 0]) cube([ovh, tray_W, floor_t + lip_h]);
                translate([-ovh + 2.4, ty0 + 2.4, floor_t]) cube([ovh, tray_W - 4.8, lip_h + 1]);
            }
            for (y = [ty0, ty0 + tray_W - 2.4]) hull() {
                translate([-ovh, y, 0]) cube([4, 2.4, floor_t + lip_h]);
                translate([-6, y, 0]) cube([6, 2.4, floor_t + 16]);
            }
        }
        // tongue gets the corner screws
        for (y = hole_y) translate([hole_x[0], y, -1]) cylinder(d = m3, h = 5);
        // cable-tie slots near the plate edge
        for (y = [plate_W/2 - 30, plate_W/2 + 30]) translate([-8, y, floor_t/2]) ziptie_pair(floor_t + 2);
    }
}

module front_tray() {
    difference() {
        union() {
            tray_body(front_ovh);
            for (o = ibt_pos) for (p = sq_holes(o, ibt_board, ibt_holes))
                radial_standoff(p, [o[0] + ibt_board/2, o[1] + ibt_board/2], 8);
        }
        for (o = ibt_pos) {
            c = [o[0] + ibt_board/2, o[1] + ibt_board/2];
            for (p = sq_holes(o, ibt_board, ibt_holes)) radial_slot(p, c);
            for (dy = [-12, -4, 4, 12]) translate([c[0], c[1] + dy, -1]) slot(26, 4, floor_t + 2);  // air slots
        }
        // wire pass-through between the two drivers
        translate([ibt_pos[0][0] + ibt_board/2, plate_W/2, -1]) slot(30, 6, floor_t + 2);
    }
}

module rear_tray_unmirrored() {
    difference() {
        union() {
            tray_body(rear_ovh);
            for (p = uno_pts()) translate([p[0], p[1], 0]) cylinder(d = 6.5, h = floor_t + standoff_h);
            for (p = sq_holes(gps_pos, gps_board, gps_holes))
                radial_standoff(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2], 7);
        }
        for (p = uno_pts()) translate([p[0], p[1], -1]) cylinder(d = m3, h = floor_t + standoff_h + 2);
        // wire pass-through between the UNO and the GPS
        translate([-10, plate_W/2, -1]) rotate([0, 0, 90]) slot(30, 7, floor_t + 2);
        for (p = sq_holes(gps_pos, gps_board, gps_holes))
            radial_slot(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2]);
    }
}
// mirrored about X so it attaches at the rear (X = 140) edge
module rear_tray() { translate([plate_L, 0, 0]) mirror([1, 0, 0]) rear_tray_unmirrored(); }

// =====================================================================
//  stand-ins for the pictures (not printed)
// =====================================================================
module battery_ghost() {
    color("darkorange", 0.8) translate([bay_x0 + end_clear, bay_y0 + side_clear, floor_t])
        cube([bat_L, bat_W, bat_cap_h - cap_h]);
}
module cap_ghost() {
    color("yellowgreen", 0.95) translate([(plate_L - cap_L)/2, (plate_W - cap_W)/2, floor_t + bat_cap_h - cap_h]) {
        cube([cap_L, cap_W, 8]);
        translate([cap_L/2 - 22, cap_W/2 - 20, 0]) cube([44, 40, cap_h]);
    }
}
module boards_ghost() {
    z = floor_t + standoff_h;
    for (o = ibt_pos) color("dimgray") translate([o[0], o[1], z]) cube([ibt_board, ibt_board, 43]);
    translate([plate_L, 0, 0]) mirror([1, 0, 0]) {
        color("teal") translate([uno_pos[0], uno_pos[1], z]) cube([uno_L, uno_W, 12]);
        color("red") translate([gps_pos[0], gps_pos[1], z]) cube([gps_board, gps_board, 8]);
    }
}
module chassis_ghost() { color("silver", 0.6) translate([0, 0, -2]) rrect(plate_L, plate_W, plate_r, 2); }

module assembly() {
    chassis_ghost();
    color("royalblue") translate([0, 0, explode*0.5]) base();
    color("cornflowerblue") translate([-explode, 0, 0]) front_tray();
    color("cornflowerblue") translate([explode, 0, 0]) rear_tray();
    translate([0, 0, bat_lift + explode]) battery_ghost();
    translate([0, 0, cap_lift + explode*1.8]) cap_ghost();
    translate([0, 0, explode*0.6]) {
        translate([-explode, 0, 0]) for (o = ibt_pos) color("dimgray") translate([o[0], o[1], floor_t + standoff_h]) cube([ibt_board, ibt_board, 43]);
        translate([explode, 0, 0]) translate([plate_L, 0, 0]) mirror([1, 0, 0]) {
            color("teal") translate([uno_pos[0], uno_pos[1], floor_t + standoff_h]) cube([uno_L, uno_W, 12]);
            color("red") translate([gps_pos[0], gps_pos[1], floor_t + standoff_h]) cube([gps_board, gps_board, 8]);
        }
    }
}

if (part == "assembly") assembly();
else if (part == "base") base();
else if (part == "front_tray") front_tray();
else if (part == "rear_tray") rear_tray_unmirrored();   // printed flat, tongue to the right
