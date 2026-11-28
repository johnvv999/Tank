// =====================================================================
//  GPS antenna pod for the rear cover.
//  The magnetic u-blox ANN-MB antenna (82 x 60 x 22.5) sits on John's 102 mm steel plate, which has a
//  1/4-20 nut welded underneath (it drops into a pocket in the lid).  A 1/4-20 x 3/8"
//  button-head bolt comes up through the pod lid into that nut.
//  Under the lid: a cup with a 40 mm hub to wind spare antenna cable on
//  (cord storage).  The cable leaves the puck, drops through a notch in the
//  lid rim, winds round the hub, and goes down through the cover into the
//  rear tray to the GPS board.  The lid twists off (bayonet, 3 lugs).
//
//  part = "pod_cup" | "pod_lid" | "pod_radome" | "pod_asm"
// =====================================================================
include <tank_deck_mount.scad>
part = "pod_asm";

pod_d   = 108;     // outside
pod_h   = 22;      // cup height
pod_t   = 2.4;
cup_r   = 1.5;     // rounding on the cup's top outer edge
hub_d   = 40;      // cable bend radius 20 mm (RG174 is fine at 15+)
plate_d = 102;  plate_th = 1.6;   // steel plate (John)
lid_t   = 4;       // lid thickness at the bayonet lugs (sets the cup's slots)
lid_top = lid_t + 4;   // lid body is 8 thick (stands 4 mm proud of the cup) so the plate's welded nut drops into a pocket
nut_af = 11.1; nut_h = 5.6;   // 1/4-20 hex nut welded under the steel plate (7/16 in across flats)
lug_n   = 3;  lug_w = 10;  lug_h = 2.5;

module pod_cup() {
    difference() {
        union() {
            // cup with a 1.5 mm rounded top outer edge (a 5 mm round ate the 2.4 mm wall and left a knife-edge
            // rim hanging over the bayonet slots, which the slicer flagged as a floating cantilever)
            rotate_extrude($fn = 120) hull() {
                square([pod_d/2 - cup_r, pod_h]);
                square([pod_d/2, pod_h - cup_r]);
                translate([pod_d/2 - cup_r, pod_h - cup_r]) circle(r = cup_r, $fn = 24);
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
        // antenna cable notches through the rim: the lid's cable notch lines up with one of them whichever
        // way the lid is locked. The cable comes over the plate edge, down through here and under the lid.
        for (i = [0 : lug_n - 1]) rotate([0, 0, i*360/lug_n]) translate([pod_d/2 - pod_t - 1, -2, pod_h - lid_t - 4.5]) cube([pod_t + 2, 4, 10]);
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
            cylinder(r = r_in, h = lid_top, $fn = 120);
            for (i = [0 : lug_n - 1]) rotate([0, 0, i*360/lug_n + 30])
                rotate([0, 0, -lug_w/2 / (pod_d/2) * 57.3]) rotate_extrude(angle = lug_w/(pod_d/2)*57.3, $fn = 60)
                    translate([r_in - 0.5, 0]) square([1.5, lug_h]);
            // grip ring the plate sits in (1 mm lip around the plate). It starts at the cup's rim and rests on it,
            // so it doesn't cut into the cup wall
            translate([0, 0, lid_t]) difference() { cylinder(r = plate_d/2 + 1.6, h = lid_top - lid_t + plate_th, $fn = 120); translate([0, 0, lid_top - lid_t]) cylinder(r = plate_d/2 + 0.4, h = 5, $fn = 120); }
        }
        // 1/4-20 bolt (6.8 mm clearance), head underneath; pocket on top for the nut welded under the plate,
        // leaving a 2.2 mm floor for the bolt head to pull against
        translate([0, 0, -1]) cylinder(d = 6.8, h = lid_top + 2);
        translate([0, 0, lid_top - nut_h - 0.2]) cylinder(d = nut_af/cos(30) + 0.6, h = nut_h + 1, $fn = 48);
        // cable notch through the lip and lid edge (turn the lid so it faces the puck's cable)
        translate([plate_d/2 - 6, -4, -1]) cube([12, 8, lid_top + plate_th + 2]);
        // two finger dimples to twist it
        for (a = [90, 270]) rotate([0, 0, a]) translate([r_in - 8, 0, lid_top + 0.01]) scale([1, 1.6, 1]) sphere(r = 4, $fn = 24);
    }
}
module plate_ghost() { color("silver") translate([0, 0, lid_top]) cylinder(d = plate_d, h = plate_th, $fn = 120); }
// u-blox ANN-MB (datasheet UBX-18049862): 82 x 60 x 22.5 mm, magnetic base, 5 m RG174 cable.
// Its patch specs are measured on a 150 mm ground plane; the 102 mm steel plate is that ground plane.
// Shape: a smooth, flat-topped oval dome; the cable leaves through a boot at one end of the base (+x here).
ann_H = 22.5;
function ann_f(z) = 0.62 + 0.38 * pow(max(0, 1 - pow(z / ann_H, 3)), 1/3);
module ann_layer(z, t = 0.01) { f = ann_f(z); translate([0, 0, z]) linear_extrude(t) offset(r = 22*f + 0.01) square([max(0.1, 82*f - 44*f), max(0.1, 60*f - 44*f)], center = true); }
module ann_mb() {
    hull() for (k = [0 : 14]) ann_layer(min(ann_H - 0.01, ann_H * k / 14));
    translate([36, 0, 4.2]) rotate([0, 90, 0]) cylinder(d1 = 8, d2 = 5, h = 13, $fn = 30);        // cable boot
}
module ann_cable() {   // boot -> over the plate edge -> down through the lid and cup notches -> under the lid into the cup
    pts = [[49, 0, 4.2], [53, 0, 3], [53, 0, -lid_top - plate_th - 2.4], [47, 0, -lid_top - plate_th - 4], [44, 0, -lid_top - plate_th - 6]];
    for (i = [0 : len(pts) - 2]) hull() { translate(pts[i]) sphere(d = 2.8, $fn = 12); translate(pts[i + 1]) sphere(d = 2.8, $fn = 12); }
}
// radome: optional thin cover over the antenna. Plain PETG/PLA/ASA 1.6 mm, at least 3 mm clear of the
// antenna, is close to RF-transparent. Its skirt slides over the lid's plate ring (friction fit) and has
// a notch for the cable. Prints top down, no supports.
rad_t = 1.6; rad_gap = 3; rad_skirt = 5.5;
rad_top = [78, 58];   // flat top, sized so every side slopes at 45 deg or steeper (prints top down, no supports)
module radome_shape(o) {   // o = 0 outside, 1 inside
    t = o ? rad_t : 0;
    hull() {
        translate([0, 0, -rad_skirt]) cylinder(r = plate_d/2 + 1.6 + 0.3 + rad_t - t, h = 2, $fn = 120);
        translate([0, 0, ann_H + rad_gap + rad_t - t - 0.01]) linear_extrude(0.01) offset(r = 18 - t) square([rad_top[0] - 36, rad_top[1] - 36], center = true);
    }
}
module radome() {   // modelled in place: z = 0 is the top of the steel plate
    difference() {
        radome_shape(0);
        radome_shape(1);
        translate([0, 0, -rad_skirt - 10]) cylinder(r = 80, h = 10);
        translate([0, 0, -rad_skirt - 1]) cylinder(r = plate_d/2 + 1.6 + 0.3, h = rad_skirt + 1, $fn = 120);   // skirt bore over the ring
        translate([plate_d/2 - 6, -2, -rad_skirt - 1]) cube([12, 4, rad_skirt + 1 + 8.5]);   // cable notch, lines up with the lid's
    }
}
module puck_ghost()  { color("#151515") translate([0, 0, lid_top + plate_th]) { ann_mb(); ann_cable(); } }

// placed on the rear cover (chassis coordinates)
module at_pod() { translate([plate_L, 0, 0]) mirror([1, 0, 0]) translate([ant_c[0], ant_c[1], case_top]) children(); }
module pod_asm() {
    at_pod() { color("#2b2d31") pod_cup(); color("#3a3d42") translate([0, 0, pod_h - lid_t]) pod_lid(); translate([0, 0, pod_h - lid_t]) { plate_ghost(); puck_ghost(); } }
}

if (part == "pod_asm") pod_asm();
else if (part == "pod_cup") pod_cup();
else if (part == "pod_lid") pod_lid();
else if (part == "pod_radome") mirror([0, 0, 1]) translate([0, 0, -(ann_H + rad_gap + rad_t)]) radome();   // top down on the bed
