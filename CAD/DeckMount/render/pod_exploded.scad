include <../antenna_pod.scad>
part = "none"; what = "";
// exploded offsets (mm, upward)
e_bolt = 22; e_lid = 48; e_plate = 72; e_ant = 96; e_rad = 140;
z0 = pod_h - lid_t;
if (what == "p_cup") at_pod() pod_cup();
if (what == "p_bolt") at_pod() translate([0, 0, z0 + e_bolt]) {
    translate([0, 0, -1.6]) difference() { cylinder(d = 13, h = 1.6, $fn = 40); translate([0, 0, -1]) cylinder(d = 6.8, h = 4); }
    translate([0, 0, -1.6 - 3.6 - 6]) scale([1, 1, 3.6/5.5]) intersection() { sphere(d = 11, $fn = 40); cylinder(d = 12, h = 6); }
    translate([0, 0, -1.6 - 6]) cylinder(d = 6.35, h = 9.5, $fn = 30);
}
if (what == "p_lid") at_pod() translate([0, 0, z0 + e_lid]) pod_lid();
if (what == "p_plate") at_pod() translate([0, 0, z0 + e_plate + lid_top]) {
    cylinder(d = plate_d, h = plate_th, $fn = 120);
    translate([0, 0, -nut_h]) difference() { cylinder(d = nut_af/cos(30), h = nut_h, $fn = 6); translate([0, 0, -1]) cylinder(d = 6.35, h = nut_h + 2, $fn = 30); }
}
if (what == "p_puck") at_pod() translate([0, 0, z0 + e_ant + lid_top + plate_th]) ann_mb();
if (what == "p_radome") at_pod() translate([0, 0, z0 + e_rad + lid_top + plate_th]) radome();
if (what == "p_coil") at_pod() for (k = [0:3]) translate([0, 0, pod_t + 2.2 + k*3.2]) rotate_extrude($fn = 80) translate([hub_d/2 + 2 + (k%2)*3, 0]) circle(d = 3, $fn = 12);
