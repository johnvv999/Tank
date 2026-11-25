include <../tank_deck_mount.scad>
what = "";
// stand-in chassis, traced from John's side and rear photos (2026-11-25):
// the plate sits toward the front; the lower chassis runs 100 mm past the plate's
// rear edge with its top 30 mm below the plate; the tracks end about 140 mm behind it;
// the ground is about 140 mm below the plate top.
trk_W = 42; trk_y = [-trk_W - 6, plate_W + 6]; trk_t = 6;
// track loop outline in X-Z: [x, z, r] of each wheel's outer track radius
trk_pts = [[246, -73, 33], [79, -36, 26], [-23, -75, 28], [14, -112, 28], [110, -112, 28], [209, -112, 28]];
module track_loop() {
    rotate([90, 0, 0]) mirror([0, 0, 1]) linear_extrude(trk_W) difference() {
        hull() for (p = trk_pts) translate([p[0], p[1]]) circle(r = p[2], $fn = 80);
        offset(-trk_t) hull() for (p = trk_pts) translate([p[0], p[1]]) circle(r = p[2], $fn = 80);
    }
}
module treads() { // grousers along the bottom run
    for (x = [14 : 9 : 209]) translate([x - 2, 0, -140]) cube([4, trk_W, 2.5]);
}
module wheels() {
    for (p = trk_pts) translate([p[0], trk_W/2, p[1]]) rotate([90, 0, 0]) cylinder(r = p[2] - trk_t - 2, h = trk_W - 10, center = true, $fn = 60);
}
if (what == "tracks") for (y = trk_y) translate([0, y, 0]) { track_loop(); treads(); }
if (what == "wheels") for (y = trk_y) translate([0, y, 0]) wheels();
if (what == "frame") {
    translate([0, 0, -2]) rrect(plate_L, plate_W, plate_r, 2);
    // side plates: top 30 mm below the plate, running 100 mm past its rear edge
    for (y = [-6, plate_W]) translate([0, y + 6, 0]) rotate([90, 0, 0]) linear_extrude(6)
        polygon([[-30, -40], [-30, -100], [240, -100], [240, -30], [40, -30], [20, -40]]);
    for (y = [-6, plate_W]) for (p = trk_pts) translate([p[0], y + 3, p[1]]) rotate([90, 0, 0]) cylinder(d = 10, h = 14, center = true);   // axles
    translate([-30, 0, -100]) cube([270, plate_W, 3]);   // belly plate
    translate([232, 0, -36]) cube([8, plate_W, 6]);       // rear cross bar
    for (x = hole_x, y = hole_y) translate([x, y, -30]) cylinder(d = 6, h = 28, $fn = 6);   // corner standoffs
    for (y = [10, plate_W - 40]) translate([40, y, -75]) rotate([0, 90, 0]) cylinder(d = 25, h = 50);  // motors (stand-in)
}
if (what == "base") base();
if (what == "trays") { front_tray(); rear_tray(); }
if (what == "hood") translate([0, 0, hood_lift]) hood();
if (what == "covers") { front_cover(); rear_cover(); }
if (what == "battery") translate([0,0,bat_lift]) battery_ghost();
if (what == "adapter") translate([0,0,cap_lift]) cap_ghost();
if (what == "ibt_pcb") for (o = ibt_pos) translate([o[0], o[1], tray_t + standoff_h]) cube([ibt_board, ibt_board, 1.6]);
if (what == "ibt_sink") for (o = ibt_pos) translate([o[0] + 5, o[1] + 5, tray_t + standoff_h + 1.6]) { cube([40, 40, 6]); for (i = [0:7]) translate([1 + i*5.2, 0, 6]) cube([2, 40, 35]); }
if (what == "uno") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([uno_pos[0], uno_pos[1], tray_t + standoff_h]) cube([uno_L, uno_W, 1.6]);
if (what == "uno_parts") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([uno_pos[0], uno_pos[1], tray_t + standoff_h + 1.6]) { translate([-1, 32, 0]) cube([8, 9, 3.3]); translate([-2, 3, 0]) cube([14, 9, 11]); translate([20, 2, 0]) cube([45, 2.5, 8.5]); translate([15, 49, 0]) cube([50, 2.5, 8.5]); }
if (what == "gps") translate([plate_L, 0, 0]) mirror([1,0,0]) translate([gps_pos[0], gps_pos[1], tray_t + standoff_h]) cube([gps_board, gps_board, 1.6]);
// the adapter's leads: out of the top, a short loop, then down inside the hood in front of the adapter
module lead(dy) {
    zt = floor_t + bat_cap_h + cap_lift;
    pts = [[plate_L/2 + 4, plate_W/2 + dy, zt - 2], [plate_L/2 + 2, plate_W/2 + dy, zt + 10], [plate_L/2 - 12, plate_W/2 + dy, zt + 14],
           [plate_L/2 - 25, plate_W/2 + dy, zt + 6], [plate_L/2 - 26, plate_W/2 + dy, zt - 30], [plate_L/2 - 27, plate_W/2 + dy, floor_t + 75]];
    for (i = [0 : len(pts) - 2]) hull() { translate(pts[i]) sphere(d = 4.5, $fn = 16); translate(pts[i + 1]) sphere(d = 4.5, $fn = 16); }
}
if (what == "lead_red") lead(-3);
if (what == "lead_black") lead(3);
