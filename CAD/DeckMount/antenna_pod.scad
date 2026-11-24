// =====================================================================
//  GPS antenna pod for the rear cover.
//  The magnetic antenna sits on John's 102 mm steel plate, which has a
//  1/4-20 nut welded underneath.  A 1/4-20 x 1/2" button-head bolt comes
//  up through the pod lid into that nut.
//  Under the lid: a cup with a 40 mm hub to wind spare antenna cable on
//  (cord storage).  The cable leaves the puck, drops through a notch in the
//  lid rim, winds round the hub, and goes down through the cover into the
//  rear tray to the GPS board.  The lid twists off (bayonet, 3 lugs).
//
//  part = "pod_cup" | "pod_lid" | "pod_asm"
// =====================================================================
include <tank_deck_mount.scad>
part = "pod_asm";

pod_d   = 108;     // outside
pod_h   = 22;      // cup height
pod_t   = 2.4;
hub_d   = 40;      // cable bend radius 20 mm (RG174 is fine at 15+)
plate_d = 102;  plate_th = 1.6;   // steel plate (John)
lid_t   = 4;
lug_n   = 3;  lug_w = 10;  lug_h = 2.5;

module pod_cup() {
    difference() {
        union() {
            // cup with a 5 mm rounded top outer edge
            rotate_extrude($fn = 120) hull() {
                square([pod_d/2 - rnd, pod_h]);
                square([pod_d/2, pod_h - rnd]);
                translate([pod_d/2 - rnd, pod_h - rnd]) circle(r = rnd, $fn = 40);
            }
        }
        // storage space (leaves the floor, the hub and the wall)
        difference() {
            translate([0, 0, pod_t]) cylinder(d = pod_d - 2*pod_t, h = pod_h, $fn = 120);
            cylinder(d = hub_d, h = pod_h - lid_t, $fn = 80);
        }
        // hub: hollow, open-topped (light), with a cable slot
        translate([0, 0, pod_t]) cylinder(d = hub_d - 2*pod_t, h = pod_h, $fn = 80);
        // cable exit down into the rear cover
        translate([ant_exit[0] - ant_c[0], ant_exit[1] - ant_c[1], -1]) cylinder(d = 10, h = pod_t + 2);
        // M3 screws down into the cover (heads inside the cup)
        for (a = ant_screw_a) translate([ant_screw_r*cos(a), ant_screw_r*sin(a), -1]) cylinder(d = m3, h = pod_t + 2);
        // bayonet: vertical entry slots + a horizontal run in the wall, just under the rim
        for (i = [0 : lug_n - 1]) rotate([0, 0, i*360/lug_n]) {
            rotate([0, 0, -lug_w/2 / (pod_d/2) * 57.3]) rotate_extrude(angle = 30 + lug_w/(pod_d/2)*57.3, $fn = 120)
                translate([pod_d/2 - pod_t - 0.1, pod_h - lid_t - 0.3]) square([1.3, lug_h + 0.6]);
            rotate([0, 0, -lug_w/2 / (pod_d/2) * 57.3]) rotate_extrude(angle = lug_w/(pod_d/2)*57.3 + 1, $fn = 120)
                translate([pod_d/2 - pod_t - 0.1, pod_h - lid_t - 0.3]) square([1.3, lid_t + 2]);
        }
    }
}
// lid: sits inside the rim, flush with it; seat for the plate on top
module pod_lid() {
    r_in = pod_d/2 - pod_t - 0.3;
    difference() {
        union() {
            cylinder(r = r_in, h = lid_t, $fn = 120);
            for (i = [0 : lug_n - 1]) rotate([0, 0, i*360/lug_n + 30])
                rotate([0, 0, -lug_w/2 / (pod_d/2) * 57.3]) rotate_extrude(angle = lug_w/(pod_d/2)*57.3, $fn = 60)
                    translate([r_in - 0.5, 0]) square([1.5, lug_h]);
            // grip ring the plate sits in (1 mm lip around the plate)
            difference() { cylinder(r = plate_d/2 + 1.6, h = lid_t + plate_th, $fn = 120); translate([0, 0, lid_t]) cylinder(r = plate_d/2 + 0.4, h = 5, $fn = 120); }
        }
        // 1/4-20 bolt (6.6 mm clearance), head underneath
        translate([0, 0, -1]) cylinder(d = 6.8, h = lid_t + 2);
        // cable notch through the lip and lid edge (turn the lid so it faces the puck's cable)
        translate([plate_d/2 - 6, -4, -1]) cube([12, 8, lid_t + plate_th + 2]);
        // two finger dimples to twist it
        for (a = [90, 270]) rotate([0, 0, a]) translate([r_in - 8, 0, lid_t + 0.01]) scale([1, 1.6, 1]) sphere(r = 4, $fn = 24);
    }
}
module plate_ghost() { color("silver") translate([0, 0, lid_t]) cylinder(d = plate_d, h = plate_th, $fn = 120); }
module puck_ghost()  { color("#151515") translate([0, 0, lid_t + plate_th]) hull() { cylinder(d = 66, h = 6, $fn = 80); translate([0, 0, 12]) cylinder(d = 56, h = 2, $fn = 80); } }

// placed on the rear cover (chassis coordinates)
module at_pod() { translate([plate_L, 0, 0]) mirror([1, 0, 0]) translate([ant_c[0], ant_c[1], case_top]) children(); }
module pod_asm() {
    at_pod() { color("#2b2d31") pod_cup(); color("#3a3d42") translate([0, 0, pod_h - lid_t]) pod_lid(); translate([0, 0, pod_h - lid_t]) { plate_ghost(); puck_ghost(); } }
}

if (part == "pod_asm") pod_asm();
else if (part == "pod_cup") pod_cup();
else if (part == "pod_lid") pod_lid();
