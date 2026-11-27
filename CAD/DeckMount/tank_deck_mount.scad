// =====================================================================
//  Tank deck mount: Ryobi P108 battery + Power Wheel adapter in the
//  middle, electronics on trays that overhang the front and rear.
//  Chassis: "Smart Robot Car" full-metal big tank kit, plate 140 x 120.
//
//  Coordinates: X = along the direction of travel (the plate's 140 mm
//  side; X = 0 is the FRONT 120 mm edge), Y = across, Z = up from the
//  top of the chassis plate.  Units: mm.
//
//  No boards above the battery.  The two IBT-2s ride on the front tray,
//  the UNO R4 WiFi and the GPS on the rear tray.
//
//  Each tray has a 2 mm tongue that slides UNDER the cradle's stepped
//  floor ends (a half-lap joint).  The chassis corner screws go through
//  tongue + cradle, so each tray is clamped straight to the metal plate.
//
//  Casing (tool-free):
//    hood        - lifts straight up over the adapter; only the top of the
//                  adapter shows.  Its lips hold the two covers down.
//    front/rear cover - hook under the tray bumper at the outer end, held
//                  at the plate end by the hood's lips.
//  Battery change: lift the hood, lift the adapter, lift the battery.
//  The adapter's leads come out of its top, through the hood opening.
//
//  Render one part at a time:  openscad -D 'part="base"' -o base.stl ...
//  part = "assembly" | "base" | "front_tray" | "rear_tray" | "hood"
//       | "front_cover" | "rear_cover"
// =====================================================================

part = "assembly";
cap_lift = 0;      // pictures: adapter lifted (mm)
bat_lift = 0;      // pictures: battery lifted (mm)
explode  = 0;      // pictures: exploded spacing (mm)
hood_lift = 0;     // pictures: hood lifted (mm)
show_case = true;  // pictures: casing on/off

/* ---------- chassis plate ---------- */
plate_L  = 152;    // along travel (John, 2026-11-25: 152 x 135)
plate_W  = 135;    // across (John)
plate_r  = 4;
// Corner screw holes (John, 2026-11-25): 125 front-to-back x 120 side-to-side,
// about 13 mm in from the ends and 7-8 mm in from the sides, so centred.
hole_sx  = 125; hole_sy = 120;
hole_x   = [(plate_L - hole_sx)/2, (plate_L + hole_sx)/2];
hole_y   = [(plate_W - hole_sy)/2, (plate_W + hole_sy)/2];
screw_head_d = 6.5;   // room for an M3 pan/button head on the cradle floor
// Rear hitch (hitch.scad) bolts up through the plate's two long rear slots
// John measured (2026-11-25): slot centre line 10 mm in from the rear edge,
// slots at y 20..62 and 82..114 from the long edge.  The clamp under the
// plate stays clear of the rear corner screws' nuts (13.5 mm in from the rear edge).
hitch_bolt_x = plate_L - 10;
hitch_bolt_y = [26, 56, 86, 108];
m3_csk = 6.4;      // countersunk M3 head

/* ---------- battery + adapter ---------- */
bat_L = 136; bat_W = 75.6;        // P108 footprint (John, 2026-11-25: about 136 x 75.6, cell housing 53.1 tall)
bat_cap_h = 150;                  // battery + adapter (John)
cap_L = 84; cap_W = 75; cap_h = 90;   // Power Wheel adapter
end_clear = 0.8; side_clear = 1.0;   // 1.6 mm total on the length, 2 mm on the width (John: 1.5 to 2)

/* ---------- trays ---------- */
front_ovh = 62;    // front tray reaches this far past the front edge
rear_ovh  = 80;    // rear tray reaches this far past the rear edge
tray_W    = 116;   // narrower than the plate, to clear the side frames
lap_L     = 30;    // tongue length under the cradle
lap_t     = 2;     // tongue thickness (half of the 4 mm floor)
// The cradle's bottom is flat everywhere (prints with no supports). Its ends are 2 mm thick with an
// open pocket on top; each tray's tongue lies in that pocket, flush with the cradle floor, and the
// corner screws clamp tongue + cradle to the plate. So each tray sits 2 mm up (tray_z).
tray_z    = lap_t;
lap_clear = 0.3;   // tongue tip to pocket end

/* ---------- general ---------- */
floor_t = 4.0; wall_t = 2.4; wall_h = 22; end_t = 1.6;
m3 = 3.4; standoff_h = 11;   // boards sit 16 above the plate, clear of all tray walls
// tray strength (John: "make extensions thicker in case they are bumped")
tray_t   = 5;     // tray floor past the plate edge
bump_t   = 5;     // bumper wall across the outer end
bump_h   = 10;    // walls stand this far above the tray floor (top at z = 15)
side_t   = 3;     // side walls
rib_h    = 5;     // cross rib under the boards at the plate edge
gus      = 10;    // corner gussets inside the bumper

/* ---------- casing ---------- */
case_t   = 2.4;   // hood + cover walls
hood_end = 1.2;   // hood end walls (between battery and plate edge): 3 lines at 0.4 mm, backed by the covers
case_top = floor_t + bat_cap_h - cap_h + 5;   // top of covers / start of the hood shoulder
tower_clear = 2.5;                // around the adapter's tower
tower_L = 30; tower_W = 30;       // adapter tower, measured by John (30 x 30)
fuse_W  = 35; fuse_t = 10;        // inline fuse holder on the red lead, 35 x 10 x 35 (John): it hangs beside the battery's left side, under the hood
sw_from_front = 80;   // battery's flat front end to the upright's switch face, along the top (John)
sw_drop = 40;  sw_win_w = 36; sw_win_h = 44;   // finger opening (John: 24 x 30 was too small to reach through). Rocker switch on the tower's front face, centre about 40 mm below its top (from John's photo)
hood_top = floor_t + bat_cap_h - 4;   // top 4 mm of the adapter shows
lip_L    = 6;     // hood lips reaching over each cover
cham     = 6;     // lip inset from the covers' long sides
rnd      = 5;     // outside edge radius (John: "round all edges by 5mm")
rim_r    = 1.2;   // tower top rim: the 2.4 mm wall can only take a 1.2 mm round
lead_gap = 14;    // extra room in front of the adapter tower for its leads, the fuse holder and the switch rocker
fit_gap  = 0.4;   // printing clearance where the hood meets the covers (end faces and lip seats)
lane_w   = 13;    // hidden wire lanes between the cradle walls and the hood sides
hook_w   = 50;    // cover hook width
/* ---------- GPS antenna pod on the rear cover (antenna_pod.scad) ---------- */
ant_c    = [-62, 60];   // pod centre in rear-cover coordinates (= 62 mm behind the plate's rear edge)
ant_exit = [-62, 86];   // cable down into the rear tray, right above the GPS board
ant_screw_r = 44; ant_screw_a = [90, 210, 330];
$fn = 40;

bay_L  = bat_L + 2*end_clear;  bay_W = bat_W + 2*side_clear;
bay_x0 = (plate_L - bay_L)/2;  bay_y0 = (plate_W - bay_W)/2;
ty0    = (plate_W - tray_W)/2;

assert(bay_L + 2*hood_end <= plate_L + 0.01, "battery too long for the plate");
assert(hole_x[0] < lap_L - 4 && hole_x[1] > plate_L - lap_L + 4, "corner screws must be inside the laps");

module rrect(l, w, r, h) { translate([r, r, 0]) linear_extrude(h) offset(r) square([l - 2*r, w - 2*r]); }
module slot(len, w, h) { hull() for (s = [-1, 1]) translate([s*(len - w)/2, 0, 0]) cylinder(d = w, h = h); }
module ziptie_pair(h) { for (s = [-1, 1]) translate([0, s*4, 0]) cube([4, 2, h], center = true); }

// =====================================================================
//  BASE / BATTERY CRADLE
// =====================================================================
wall_x0 = max(bay_x0 + 4, lap_L + lap_clear + 0.5);
module base() {
    difference() {
        union() {
            // floor: one flat 4 mm slab, solid to the bed everywhere
            rrect(plate_L, plate_W, rnd, floor_t);
            // side walls stand between the two tongue pockets
            for (y = [bay_y0 - wall_t, bay_y0 + bay_W])
                translate([wall_x0, y, 0]) cube([plate_L - 2*wall_x0, wall_t, wall_h]);
        }
        // tongue pockets: the top 2 mm of each end, open on top and at the end
        for (x0 = [-1, plate_L - lap_L - lap_clear]) translate([x0, -1, lap_t]) cube([lap_L + lap_clear + 1, plate_W + 2, floor_t]);
        // windows under the middle of the battery
        for (x = [lap_L + 6, plate_L/2 + 3])
            translate([x, bay_y0 + 10, -1]) rrect(plate_L/2 - lap_L - 9, bay_W - 20, 4, floor_t + 2);
        // side wire channels
        for (y = [4, plate_W - 4 - (bay_y0 - wall_t - 8)])
            translate([lap_L + 6, y, -1]) rrect(plate_L - 2*lap_L - 12, bay_y0 - wall_t - 8, 2, floor_t + 2);
        // corner screws
        for (x = hole_x, y = hole_y) translate([x, y, -1]) cylinder(d = m3, h = floor_t + 2);
        // rear hitch bolts (countersunk in the rear tray's tongue above)
        for (y = hitch_bolt_y) translate([hitch_bolt_x, y, -1]) cylinder(d = m3, h = floor_t + 2);
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
    translate([p[0], p[1], 0]) rotate([0, 0, a]) slot(5 + d, d, tray_t + standoff_h);
}
module radial_slot(p, c) {
    a = atan2(p[1] - c[1], p[0] - c[0]);
    translate([p[0], p[1], -1]) rotate([0, 0, a]) slot(m3 + 5, m3, tray_t + standoff_h + 2);
}

// board positions (lower-left corner, tray coordinates = chassis X/Y)
ibt_pos = [[-front_ovh + 6, ty0 + side_t + 0.5], [-front_ovh + 6, plate_W - ty0 - side_t - 0.5 - ibt_board]];
// rear tray is modelled at the front and then mirrored, so these X are "outboard distance"
uno_pos = [-rear_ovh + 6, ty0 + side_t + 0.5];               // UNO 68.6 runs along X, USB-C + barrel jack face the rear
gps_pos = [-rear_ovh + 12, plate_W - ty0 - side_t - 4 - gps_board];
rear_hook_y = gps_pos[1] + gps_board/2;   // hook sits behind the GPS, clear of the USB window
function uno_pts() = [for (h = uno_holes) [uno_pos[0] + h[0], uno_pos[1] + h[1]]];

// hook pocket in the bumper's outer face; the cover's tab drops into it
module hook_pocket(ovh, yc) {
    translate([-ovh - 1, yc - hook_w/2 - 0.3, tray_t + bump_h - 8]) cube([3, hook_w + 0.6, 9]);       // 2 mm deep pocket
    translate([-ovh + 1.9, yc - hook_w/2 - 0.3, tray_t + bump_h - 7]) cube([1.7, hook_w + 0.6, 2]);   // groove for the nub
}

module tray_body(ovh, hook_y) {
    difference() {
        union() {
            // thick tray floor + tongue under the cradle
            hull() {
                translate([-ovh, ty0, 0]) rrect(12, tray_W, 5, tray_t);
                translate([-1, ty0, 0]) cube([1, tray_W, tray_t]);
            }
            intersection() {
                rrect(plate_L, plate_W, plate_r, lap_t);
                translate([-1, -1, 0]) cube([lap_L + 1, plate_W + 2, lap_t]);
            }
            // bumper wall across the outer end + side walls, all 15 tall, rounded outer corners
            difference() {
                hull() {
                    translate([-ovh, ty0, 0]) rrect(12, tray_W, 5, tray_t + bump_h);
                    translate([-1, ty0, 0]) cube([1, tray_W, tray_t + bump_h]);
                }
                translate([-ovh + bump_t, ty0 + side_t, tray_t]) cube([ovh, tray_W - 2*side_t, bump_h + 1]);
            }
            // cross rib under the boards where the tray meets the plate
            translate([-5, ty0, 0]) cube([4, tray_W, tray_t + rib_h]);
            // corner gussets inside the bumper
            for (s = [0, 1]) {
                y = s == 0 ? ty0 + side_t : ty0 + tray_W - side_t;
                hull() {
                    translate([-ovh + bump_t, y - s*gus, 0]) cube([0.01, gus, tray_t + bump_h]);
                    translate([-ovh + bump_t, y - s*0.01, 0]) cube([gus, 0.01, tray_t + bump_h]);
                }
            }
        }
        // tongue gets the corner screws
        for (y = hole_y) translate([hole_x[0], y, -1]) cylinder(d = m3, h = 5);
        // cable-tie slots near the plate edge
        for (y = [plate_W/2 - 30, plate_W/2 + 30]) translate([-9, y, tray_t/2]) ziptie_pair(tray_t + 2);
        // wire notches through the rib: middle, and the two hidden side lanes from under the hood
        translate([-6, plate_W/2 - 8, tray_t]) cube([6, 16, rib_h + 1]);
        for (y = [ty0 + side_t, plate_W - ty0 - side_t - lane_w + 2]) translate([-6, y, tray_t]) cube([6, lane_w - 2, rib_h + 1]);
        hook_pocket(ovh, hook_y);
    }
}

module front_tray_flat() {   // as printed (floor on the bed)
    difference() {
        union() {
            tray_body(front_ovh, plate_W/2);
            for (o = ibt_pos) for (p = sq_holes(o, ibt_board, ibt_holes))
                radial_standoff(p, [o[0] + ibt_board/2, o[1] + ibt_board/2], 8);
        }
        for (o = ibt_pos) {
            c = [o[0] + ibt_board/2, o[1] + ibt_board/2];
            for (p = sq_holes(o, ibt_board, ibt_holes)) radial_slot(p, c);
            for (dy = [-12, -4, 4, 12]) translate([c[0], c[1] + dy, -1]) slot(26, 4, tray_t + 2);  // air slots
        }
        // wire pass-through between the two drivers
        translate([ibt_pos[0][0] + ibt_board/2, plate_W/2, -1]) slot(30, 6, tray_t + 2);
    }
}

module rear_tray_unmirrored() {
    difference() {
        union() {
            tray_body(rear_ovh, rear_hook_y);
            for (p = uno_pts()) translate([p[0], p[1], 0]) cylinder(d = 6.5, h = tray_t + standoff_h);
            for (p = sq_holes(gps_pos, gps_board, gps_holes))
                radial_standoff(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2], 7);
        }
        for (p = uno_pts()) translate([p[0], p[1], -1]) cylinder(d = m3, h = tray_t + standoff_h + 2);
        // rear hitch: bolts through the tongue, and two countersunk M3 that tie the tray floor to the hitch beam
        for (y = hitch_bolt_y) translate([plate_L - hitch_bolt_x, y, -1]) {   // countersunk flush, the battery stands on them
            cylinder(d = m3, h = lap_t + 2);
            translate([0, 0, lap_t + 0.01 - (m3_csk - m3)/2]) cylinder(d1 = m3, d2 = m3_csk, h = (m3_csk - m3)/2 + 1);
        }
        for (x = [-20, -60]) translate([x, plate_W/2 + 2.5, -1]) {
            cylinder(d = m3, h = tray_t + 2);
            translate([0, 0, tray_t + 1 - (m3_csk - m3)/2]) cylinder(d1 = m3, d2 = m3_csk, h = (m3_csk - m3)/2 + 0.01);
        }
        // wire pass-through between the UNO and the GPS (the hitch servo cable comes up here too)
        translate([-10, plate_W/2, -1]) rotate([0, 0, 90]) slot(30, 7, tray_t + 2);
        for (p = sq_holes(gps_pos, gps_board, gps_holes))
            radial_slot(p, [gps_pos[0] + gps_board/2, gps_pos[1] + gps_board/2]);
    }
}
// mirrored about X so it attaches at the rear (X = 140) edge
module front_tray() { translate([0, 0, tray_z]) front_tray_flat(); }
module rear_tray() { translate([plate_L, 0, tray_z]) mirror([1, 0, 0]) rear_tray_unmirrored(); }

// =====================================================================
//  CASING
// =====================================================================
// hood lip profile (x <= 0 side, in the X-Z plane): 45 deg underside, rounded tip
lip_tip_r = 2.5;
module lip_profile(c = 0) {
    tx = -lip_L - c;
    polygon(concat([[c, case_top - lip_L - 3 - c], [tx, case_top - 3 - c]],
        [for (a = [180 : -10 : 90]) [tx + lip_tip_r + lip_tip_r*cos(a), case_top - lip_tip_r + lip_tip_r*sin(a)]],
        [[c, case_top + 0.01]]));
}
module lip_solid(c = 0) {   // across the flat top of the covers, plan corners rounded
    y0 = ty0 + cham - c;  y1 = ty0 + tray_W - cham + c;
    intersection() {
        translate([0, y0, 0]) rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(y1 - y0) lip_profile(c);
        translate([0, 0, case_top - 20]) linear_extrude(30) hull() {
            for (y = [y0 + rnd, y1 - rnd]) translate([-lip_L - c + rnd, y]) circle(r = rnd);
            translate([0, y0]) square([1, y1 - y0]);
        }
    }
}
// hood: built from hulls of spheres and cylinders so every outside edge has a 5 mm radius
tw_L = tower_L + 2*tower_clear + lead_gap;  tw_W = max(tower_W + 2*tower_clear, fuse_W + 4);
tower_cx = bay_x0 + end_clear + sw_from_front + tower_L/2;   // the upright sits behind the battery's middle
tw_cx = tower_cx - lead_gap/2;   // tower opening shifted forward: the leads run down in front of the adapter
tlx_o = (tw_L + 2*case_t)/2 - rnd;
// shoulder rise: at least 45 deg on both slopes, so the hood prints open side down without supports
shoulder = max(50, (tw_cx - tlx_o) - rnd + 1, (plate_L - rnd) - (tw_cx + tlx_o) + 1);
assert(case_top + shoulder <= hood_top - 10, "tower too far from the middle: no room for the sleeve");
module corners(x0, x1, y0, y1) { for (x = [x0, x1], y = [y0, y1]) translate([x, y, 0]) children(); }
module hood_shell(o) {   // o = 0 outside, 1 inside
    r  = o ? rnd - case_t : rnd;
    ex = o ? hood_end : 0;  ey = o ? case_t : 0;   // inside faces
    xo = rnd;  yo = rnd;                            // sphere centres shared by inside and outside on the sides
    xi = o ? hood_end + r : rnd;                    // ends are thinner (1.6), so the inside centres move out
    tlx = (tw_L + 2*case_t)/2 - rnd;  tly = (tw_W + 2*case_t)/2 - rnd;   // tower centres (outside)
    hull() {
        corners(xi, plate_L - xi, yo, plate_W - yo) translate([0, 0, floor_t - o]) cylinder(r = r, h = 0.01);
        corners(xi, plate_L - xi, yo, plate_W - yo) translate([0, 0, case_top - rnd]) sphere(r = r);
        corners(tw_cx - tlx, tw_cx + tlx, plate_W/2 - tly, plate_W/2 + tly)
            translate([0, 0, case_top + shoulder - rnd - (o ? 2 : 0)]) sphere(r = r);
    }
    if (o == 0) hull() {   // tower sleeve with a rounded top rim
        corners(tw_cx - tlx, tw_cx + tlx, plate_W/2 - tly, plate_W/2 + tly) {
            translate([0, 0, case_top + shoulder - rnd]) cylinder(r = rnd, h = hood_top - rim_r - case_top - shoulder + rnd);
            translate([0, 0, hood_top - rim_r]) rotate_extrude($fn = 40) translate([rnd - rim_r, 0]) circle(r = rim_r, $fn = 16);
        }
    }
    else translate([tw_cx - tw_L/2, plate_W/2 - tw_W/2, case_top + shoulder - 8]) rrect(tw_L, tw_W, rnd - case_t, 200);
}
module hood() {
    difference() {
        union() {
            hood_shell(0);
            // lips that reach over the front and rear covers
            lip_solid();
            translate([plate_L, 0, 0]) mirror([1, 0, 0]) lip_solid();
        }
        hood_shell(1);
        // hidden wire lanes: passages at the bottom of both ends, on both sides (between hood and cradle walls)
        for (x = [-1, plate_L - hood_end - 1], y = [case_t + 0.3, plate_W - case_t - 0.3 - lane_w])
            translate([x, y, floor_t - 1]) cube([hood_end + 2, lane_w, 13]);
        // finger window for the adapter's rocker switch, in the tower's front face
        // straight-through finger opening, cut from the front edge so it opens the whole slope in front of the switch
        translate([-5, plate_W/2 - sw_win_w/2, floor_t + bat_cap_h - sw_drop - sw_win_h/2])
            rotate([0, 90, 0]) translate([-sw_win_h, 0, 0]) rrect(sw_win_h, sw_win_w, 8, tw_cx - tw_L/2 + 5 + case_t + 1);
        // decorative grooves along the sides
        for (z = [case_top - 18, case_top - 26]) for (y = [-1, plate_W - 0.8])
            translate([14, y, z]) cube([plate_L - 28, 1.8, 1.6]);
    }
}

// covers, modelled at the front (x from -ovh to 0), open toward the plate
// o = 0 outside, 1 inside.  Outer end corners and the top edges have a 5 mm radius; the plate end
// (against the hood) and the bottom edge (sits on the tray wall) stay square.
module cover_shell(ovh, o) {
    r  = o ? rnd - case_t : rnd;
    zb = tray_z + tray_t + bump_h - o;
    ya = ty0 + rnd;  yb = ty0 + tray_W - rnd;
    hull() {
        corners(-ovh + rnd, -ovh + rnd, ya, yb) translate([0, 0, zb]) cylinder(r = r, h = 0.01);
        corners(-ovh + rnd, -ovh + rnd, ya, yb) translate([0, 0, case_top - rnd]) sphere(r = r);
        for (y = [ya, yb]) translate([-1, y, case_top - rnd]) rotate([0, 90, 0]) cylinder(r = r, h = 1 + o);
        translate([-1, ty0 + o*case_t, zb]) cube([1 + o, tray_W - 2*o*case_t, 0.01]);
        // covers print top down: the last 1.5 mm of each top round becomes a 45 deg chamfer (no overhang on the bed)
        if (o == 0) {
            t = rnd*(sqrt(2) - 1);
            corners(-ovh + rnd, -ovh + rnd, ya, yb) translate([0, 0, case_top - 0.01]) cylinder(r = t, h = 0.01);
            translate([-1, ya - t, case_top - 0.01]) cube([1, yb - ya + 2*t, 0.01]);
        }
    }
}
module cover_outer(ovh) { cover_shell(ovh, 0); }
module cover_inner(ovh) { cover_shell(ovh, 1); }
module cover_body(ovh, hook_y) {
    difference() {
        union() {
            difference() { cover_outer(ovh); cover_inner(ovh); }
            // solid ledge under the hood lip (45 deg underside, so it prints top-down)
            intersection() {
                cover_outer(ovh);
                translate([0, ty0, 0]) rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(tray_W)
                    polygon([[-lip_L - 3, case_top], [-lip_L - 3, case_top - case_t], [0, case_top - lip_L - 3 - case_t - 3], [0, case_top]]);
            }
            // hook tab: drops into the bumper pocket, nub clicks into the groove
            translate([-ovh + 0.3, hook_y - hook_w/2, tray_z + tray_t + bump_h - 7.7]) cube([1.7, hook_w, 8]);
            translate([-ovh + 1.9, hook_y - hook_w/2, tray_z + tray_t + bump_h - 6.8]) cube([1.2, hook_w, 1.6]);
        }
        lip_solid(fit_gap);   // seat for the hood's lip
        // sliding gap between the cover's plate end and the hood's end wall
        translate([-fit_gap, -1, -1]) cube([fit_gap + 5, plate_W + 2, 200]);
    }
}
module front_cover() {
    difference() {
        cover_body(front_ovh, plate_W/2);
        // vent slots over both IBT-2 heatsinks
        for (o = ibt_pos) for (i = [0:5]) translate([o[0] + ibt_board/2 - 2, o[1] + 7 + i*7.2, case_top - 5]) slot(36, 3.2, 10);
        // grille in the front face
        for (i = [-6:6]) translate([-front_ovh - 1, plate_W/2 + i*7 - 1.6, tray_z + tray_t + bump_h + 10]) cube([case_t + 2, 3.2, 26]);
    }
}
module rear_cover_unmirrored() {
    difference() {
        cover_body(rear_ovh, rear_hook_y);
        // USB-C + barrel jack window, from the bottom edge up
        translate([-rear_ovh - 1, uno_pos[1] + 1, tray_z + tray_t + bump_h - 1]) cube([case_t + 2, uno_W - 2, 16]);
        // antenna cable comes down from the pod here (an SMA plug passes a 10 mm hole)
        translate([ant_exit[0], ant_exit[1], case_top - 5]) cylinder(d = 10, h = 10);
        // 3x M3 holding the antenna pod (nuts inside the cover)
        for (a = ant_screw_a) translate([ant_c[0] + ant_screw_r*cos(a), ant_c[1] + ant_screw_r*sin(a), case_top - 5]) cylinder(d = m3, h = 10);
        // vents moved to the side next to the UNO (the pod covers the top)
        for (i = [0:5]) translate([-rear_ovh + 14 + i*9, ty0 - 1, tray_z + tray_t + bump_h + 12]) cube([3.2, case_t + 2, 24]);
    }
}
module rear_cover() { translate([plate_L, 0, 0]) mirror([1, 0, 0]) rear_cover_unmirrored(); }

// =====================================================================
//  stand-ins for the pictures (not printed)
// =====================================================================
module battery_ghost() {
    color("darkorange", 0.8) translate([bay_x0 + end_clear, bay_y0 + side_clear, floor_t])
        cube([bat_L, bat_W, bat_cap_h - cap_h]);
}
module cap_ghost() {
    // base: about 40 mm of it in front of the upright (from John's photo), curved end behind
    color("yellowgreen", 0.95) translate([tower_cx - tower_L/2 - 40, (plate_W - cap_W)/2, floor_t + bat_cap_h - cap_h]) {
        cube([cap_L, cap_W, 8]);
        translate([40, cap_W/2 - tower_W/2, 0]) cube([tower_L, tower_W, cap_h]);
    }
}
module switch_ghost() {   // rocker switch on the tower's front face
    translate([tower_cx - tower_L/2 - 4, plate_W/2 - 7.5, floor_t + bat_cap_h - sw_drop - 10]) cube([4, 15, 20]);
}
module fuse_ghost() {     // inline fuse holder on the red lead, hanging upright in the gap outside the left cradle wall,
                          // well in front of the battery's release button (not in front of the switch)
    translate([36 - fuse_W/2, 15, 22]) cube([fuse_W, fuse_t, 35]);
}
module boards_ghost() {
    z = tray_z + tray_t + standoff_h;
    for (o = ibt_pos) color("dimgray") translate([o[0], o[1], z]) cube([ibt_board, ibt_board, 43]);
    translate([plate_L, 0, 0]) mirror([1, 0, 0]) {
        color("teal") translate([uno_pos[0], uno_pos[1], z]) cube([uno_L, uno_W, 12]);
        color("red") translate([gps_pos[0], gps_pos[1], z]) cube([gps_board, gps_board, 8]);
    }
}
module chassis_ghost() { color("silver", 0.6) translate([0, 0, -3]) rrect(plate_L, plate_W, plate_r, 3); }   // 3 mm metal plate

module assembly() {
    chassis_ghost();
    color("royalblue") translate([0, 0, explode*0.5]) base();
    color("cornflowerblue") translate([-explode, 0, 0]) front_tray();
    color("cornflowerblue") translate([explode, 0, 0]) rear_tray();
    translate([0, 0, bat_lift + explode]) battery_ghost();
    translate([0, 0, cap_lift + explode*1.8]) cap_ghost();
    translate([0, 0, explode*0.6]) {
        translate([-explode, 0, 0]) for (o = ibt_pos) color("dimgray") translate([o[0], o[1], tray_z + tray_t + standoff_h]) cube([ibt_board, ibt_board, 43]);
        translate([explode, 0, 0]) translate([plate_L, 0, 0]) mirror([1, 0, 0]) {
            color("teal") translate([uno_pos[0], uno_pos[1], tray_z + tray_t + standoff_h]) cube([uno_L, uno_W, 12]);
            color("red") translate([gps_pos[0], gps_pos[1], tray_z + tray_t + standoff_h]) cube([gps_board, gps_board, 8]);
        }
    }
    if (show_case) {
        color("#2b2d31") translate([0, 0, hood_lift + explode*3]) hood();
        color("#3a3d42") translate([-explode*1.6, 0, explode*1.5]) front_cover();
        color("#3a3d42") translate([explode*1.6, 0, explode*1.5]) rear_cover();
    }
}

if (part == "assembly") assembly();
else if (part == "base") base();
else if (part == "front_tray") front_tray_flat();
else if (part == "rear_tray") mirror([1, 0, 0]) rear_tray_unmirrored();   // printed flat exactly as fitted (top up, tongue to the left); not a mirror image
else if (part == "hood") hood();                         // printed open side down
else if (part == "front_cover") translate([0, 0, case_top]) mirror([0, 0, 1]) front_cover();          // printed top down
else if (part == "rear_cover") translate([0, 0, case_top]) mirror([0, 0, 1]) rear_cover_unmirrored();  // printed top down
else if (part == "hood_asm") hood();
else if (part == "front_cover_asm") front_cover();
else if (part == "rear_cover_asm") rear_cover();
