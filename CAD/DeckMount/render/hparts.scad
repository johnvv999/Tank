include <../hitch.scad>
what = "";
th = solve_th(rake_lift);
if (what == "hitch") { hitch(); lid(); }
if (what == "servo") servo_ghost();
if (what == "links") { lift_arm(tool == "trailer" ? 90 : th); if (tool == "rake") link_bar(arm_tip(th), mast_at(rake_lift)); if (tool == "trailer") link_bar(arm_tip(90), mast_at(0)); }
if (what == "tool") { if (tool == "rake") { rake_frame(rake_lift); rake_bar(rake_lift); } if (tool == "trailer") drawbar(); }
if (what == "trailer" && tool == "trailer") trailer_ghost();
