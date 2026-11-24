include <../tank_deck_mount.scad>
what = "";
// illustrative chassis: side frames, sprockets, road wheels, rubber tracks
trk_L = 250; trk_W = 42; trk_H = 62; trk_y = [-trk_W - 6, plate_W + 6];
module track_loop() {   // in X-Z, extruded along Y
    rotate([90, 0, 0]) mirror([0,0,1]) linear_extrude(trk_W) difference() {
        hull() { translate([-trk_L/2 + 31, 0]) circle(r = 31, $fn = 80); translate([trk_L/2 - 31, 0]) circle(r = 31, $fn = 80); }
        hull() { translate([-trk_L/2 + 31, 0]) circle(r = 25, $fn = 80); translate([trk_L/2 - 31, 0]) circle(r = 25, $fn = 80); }
    }
}
module treads() { // grousers on the outside of the loop (top and bottom runs)
    for (x = [-trk_L/2 + 31 : 9 : trk_L/2 - 31]) for (z = [-31 - 2.5, 31]) translate([x - 2, 0, z]) cube([4, trk_W, 2.5]);
}
module wheels() {
    for (x = [-trk_L/2 + 31, trk_L/2 - 31]) translate([x, trk_W/2, 0]) rotate([90, 0, 0]) cylinder(r = 24, h = trk_W - 6, center = true, $fn = 60);
    for (x = [-60, -20, 20, 60]) translate([x, trk_W/2, -10]) rotate([90, 0, 0]) cylinder(r = 14, h = trk_W - 8, center = true, $fn = 48);
}
cx = plate_L/2; cz = -2 - 40;
if (what == "tracks") for (y = trk_y) translate([cx, y, cz]) { track_loop(); treads(); }
if (what == "wheels") for (y = trk_y) translate([cx, y, cz]) wheels();
if (what == "frame") {
    translate([0, 0, -2]) rrect(plate_L, plate_W, plate_r, 2);
    for (y = [-6, plate_W]) translate([cx - 115, y, cz - 18]) cube([230, 6, 56]);          // side plates
    for (y = [-6, plate_W]) for (x = [-trk_L/2 + 31, trk_L/2 - 31, -60, -20, 20, 60]) translate([cx + x, y + 3, cz + (abs(x) > 80 ? 0 : -10)]) rotate([90,0,0]) cylinder(d = 10, h = 14, center = true);
    translate([cx - 115, 0, cz - 20]) cube([230, plate_W, 3]);   // belly plate
    for (y = [10, plate_W - 40]) translate([cx + 70, y, cz - 16]) rotate([0, 90, 0]) cylinder(d = 25, h = 50);  // motors (XR25-370)
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
