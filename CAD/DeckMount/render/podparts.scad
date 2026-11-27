include <../antenna_pod.scad>
what = ""; lid_up = 0;
if (what == "p_cup") at_pod() pod_cup();
if (what == "p_lid") at_pod() translate([0, 0, pod_h - lid_t + lid_up]) pod_lid();
if (what == "p_plate") at_pod() translate([0, 0, pod_h - lid_t + lid_up]) translate([0, 0, lid_t]) cylinder(d = plate_d, h = plate_th, $fn = 120);
if (what == "p_puck") at_pod() translate([0, 0, pod_h - lid_t + lid_up]) translate([0, 0, lid_t + plate_th]) ann_mb();
if (what == "p_coil") at_pod() for (k = [0:3]) translate([0, 0, pod_t + 2.2 + k*3.2]) rotate_extrude($fn = 80) translate([hub_d/2 + 2 + (k%2)*3, 0]) circle(d = 3, $fn = 12);
