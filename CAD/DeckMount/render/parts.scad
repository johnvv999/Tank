include <../tank_deck_mount.scad>
what = "";
// stand-in chassis, traced from John's side and rear photos (2026-11-25):
// the plate sits toward the front; the lower chassis runs 100 mm past the plate's
// rear edge with its top 30 mm below the plate; the tracks end about 140 mm behind it;
// the plate top is 130 mm above the ground (John measured).
trk_W = 42; trk_y = [-trk_W - 6, plate_W + 6]; trk_t = 6;
// track loop outline in X-Z: [x, z, r] of each wheel's outer track radius
trk_pts = [[258, -73, 33], [85, -36, 26], [-23, -75, 28], [14, -102, 28], [116, -102, 28], [221, -102, 28]];
module track_loop() {
    rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(trk_W) difference() {
        hull() for (p = trk_pts) translate([p[0], p[1]]) circle(r = p[2], $fn = 80);
        offset(-trk_t) hull() for (p = trk_pts) translate([p[0], p[1]]) circle(r = p[2], $fn = 80);
    }
}
module treads() { // grousers along the bottom run
    for (x = [14 : 9 : 221]) translate([x - 2, 0, -130]) cube([4, trk_W, 2.5]);
}
module wheels() {
    for (p = trk_pts) translate([p[0], trk_W/2, p[1]]) rotate([90, 0, 0]) cylinder(r = p[2] - trk_t - 2, h = trk_W - 10, center = true, $fn = 60);
}
if (what == "tracks") for (y = trk_y) translate([0, y, 0]) { track_loop(); treads(); }
if (what == "wheels") for (y = trk_y) translate([0, y, 0]) wheels();
if (what == "frame") {
    translate([0, 0, -3]) rrect(plate_L, plate_W, plate_r, 3);   // 3 mm plate
    // side plates: top 30 mm below the plate, running 100 mm past its rear edge
    for (y = [-6, plate_W]) translate([0, y + 6, 0]) rotate([90, 0, 0]) linear_extrude(6)
        polygon([[-30, -40], [-30, -100], [plate_L + 100, -100], [plate_L + 100, -30], [40, -30], [20, -40]]);
    for (y = [-6, plate_W]) for (p = trk_pts) translate([p[0], y + 3, p[1]]) rotate([90, 0, 0]) cylinder(d = 10, h = 14, center = true);   // axles
    translate([-30, 0, -100]) cube([plate_L + 130, plate_W, 3]);   // belly plate
    translate([plate_L + 92, 0, -36]) cube([8, plate_W, 6]);       // rear cross bar
    for (x = hole_x, y = hole_y) translate([x, y, -30]) cylinder(d = 6, h = 27, $fn = 6);   // corner standoffs
    for (y = [10, plate_W - 40]) translate([40, y, -75]) rotate([0, 90, 0]) cylinder(d = 25, h = 50);  // motors (stand-in)
}
if (what == "base") base();
if (what == "trays") { front_tray(); rear_tray(); }
if (what == "hood") translate([0, 0, hood_lift]) hood();
if (what == "covers") { front_cover(); rear_cover(); }
if (what == "battery") translate([0,0,bat_lift]) battery_ghost();
if (what == "adapter") translate([0,0,cap_lift]) cap_ghost();
if (what == "ibt_pcb") for (o = ibt_pos) translate([o[0], o[1], tray_z + tray_t + standoff_h]) cube([ibt_board, ibt_board, 1.6]);
if (what == "ibt_sink") for (o = ibt_pos) translate([o[0] + 5, o[1] + 5, tray_z + tray_t + standoff_h + 1.6]) { cube([40, 40, 6]); for (i = [0:7]) translate([1 + i*5.2, 0, 6]) cube([2, 40, 35]); }
if (what == "uno") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([uno_pos[0], uno_pos[1], tray_z + tray_t + standoff_h]) cube([uno_L, uno_W, 1.6]);
if (what == "uno_parts") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([uno_pos[0], uno_pos[1], tray_z + tray_t + standoff_h + 1.6]) translate([0, uno_W, 0]) mirror([0, 1, 0]) { translate([-1, 32, 0]) cube([8, 9, 3.3]); translate([-2, 3, 0]) cube([14, 9, 11]); translate([20, 2, 0]) cube([45, 2.5, 8.5]); translate([15, 49, 0]) cube([50, 2.5, 8.5]); }
if (what == "gps") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([gps_pos[0], gps_pos[1], tray_z + tray_t + standoff_h]) cube([gps_board, gps_board, 1.6]);
// the adapter's leads: out of the top, a short loop, down in front of the upright to the LEFT of the
// rocker switch, forward over the adapter's base onto the front of the battery top, over the battery's
// left edge and down the gap outside the cradle wall (fuse holder on the red lead hangs there), then
// forward along the left wire lane under the hood end into the front tray.
module lead(k) {   // k = 0 red, 1 black
    zt = floor_t + bat_cap_h;  zb = floor_t + bat_cap_h - cap_h;   // adapter top, battery top
    yl = 51 + 4.6*k;  xd = 36 + 5*k;  yg = k ? 9 : 20;  yl2 = k ? 6.5 : 12;
    up = [[tower_cx + 4, plate_W/2 - 4 + 5*k, zt - 2], [tower_cx + 2, plate_W/2 - 4 + 5*k, zt + 10], [tower_cx - 14, yl, zt + 13],
          [tower_cx - 27.5, yl, zt + 4], [tower_cx - 27.5, yl, zb + 8 + 2.3]];
    dn = [[tower_cx - 54 - 3*k, yl, zb + 8 + 2.3], [tower_cx - 60 - 3*k, yl - 1, zb + 2.3], [xd, 31, zb + 2.3], [xd, 26, zb + 1],
          [xd, yg, zb - 6], [xd, yg, 16], [xd - 4, yl2, floor_t + 3], [-6, yl2, floor_t + 3]];
    pts = concat([for (p = up) p + [0, 0, cap_lift]], dn);
    for (i = [0 : len(pts) - 2]) hull() { translate(pts[i]) sphere(d = 4.5, $fn = 16); translate(pts[i + 1]) sphere(d = 4.5, $fn = 16); }
}
if (what == "lead_red") lead(0);
if (what == "switch") translate([0, 0, cap_lift]) switch_ghost();
if (what == "fuse") fuse_ghost();
if (what == "lead_black") lead(1);

// ---- Ryobi P108 stand-in for the pictures: flat front, curved rear, two release
// buttons on the long sides flanking the stem (under the adapter's base).
bx0 = bay_x0 + end_clear; by0 = bay_y0 + side_clear; bz0 = floor_t;
bat_H = bat_cap_h - cap_h;           // body up to where the adapter sits
btn_x = tower_cx - 5; btn_L = 26; btn_z = [bat_H - 17, bat_H - 5];
module bat_plan(inset = 0) {          // flat front end, rounded rear end
    offset(-inset) hull() {
        translate([bx0 + 2, by0 + 2]) circle(r = 2, $fn = 16);
        translate([bx0 + 2, by0 + bat_W - 2]) circle(r = 2, $fn = 16);
        translate([bx0 + bat_L - 24, by0 + 24]) circle(r = 24, $fn = 80);
        translate([bx0 + bat_L - 24, by0 + bat_W - 24]) circle(r = 24, $fn = 80);
    }
}
module bat_btn_cut() { for (y = [by0 - 1, by0 + bat_W - 3]) translate([btn_x - btn_L/2 - 1, y, bz0 + btn_z[0] - 1]) cube([btn_L + 2, 4, btn_z[1] - btn_z[0] + 2]); }
if (what == "bat_body") translate([0, 0, bat_lift]) difference() {
    union() {
        translate([0, 0, bz0]) linear_extrude(bat_H - 18) bat_plan();
        translate([0, 0, bz0 + bat_H - 4]) linear_extrude(4) bat_plan(1.5);   // top deck under the adapter
    }
    bat_btn_cut();
}
if (what == "bat_band") translate([0, 0, bat_lift]) difference() {     // green upper housing
    translate([0, 0, bz0 + bat_H - 18]) linear_extrude(14) bat_plan(0.3);
    bat_btn_cut();
}
if (what == "bat_btn") translate([0, 0, bat_lift]) for (s = [0, 1]) {   // release buttons, ribbed, standing 1.5 proud
    y = s == 0 ? by0 - 1.5 : by0 + bat_W - 2.5;
    translate([btn_x - btn_L/2, y, bz0 + btn_z[0]]) cube([btn_L, 4, btn_z[1] - btn_z[0]]);
    for (i = [0:4]) translate([btn_x - btn_L/2 + 3 + i*5, s == 0 ? y - 0.8 : y + 3.8, bz0 + btn_z[0] + 2]) cube([1.6, 1, btn_z[1] - btn_z[0] - 4]);
}
if (what == "bat_gauge") translate([0, 0, bat_lift]) for (i = [0:3])   // fuel gauge LEDs on the flat front
    translate([bx0 - 0.6, by0 + bat_W/2 - 15 + i*10, bz0 + bat_H - 26]) cube([1, 6, 3]);
