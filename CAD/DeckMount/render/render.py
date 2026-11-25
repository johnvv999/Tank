import bpy, sys, math, os
from mathutils import Vector
argv = sys.argv[sys.argv.index("--") + 1:]
src, out, view = argv[0], argv[1], argv[2]
samples = int(argv[3]) if len(argv) > 3 else 96
bpy.ops.wm.read_factory_settings(use_empty=True)
sc = bpy.context.scene
sc.render.engine = 'CYCLES'; sc.cycles.device = 'CPU'; sc.cycles.samples = samples
sc.cycles.use_denoising = True
sc.render.resolution_x, sc.render.resolution_y = 1600, 1100
sc.view_settings.view_transform = 'AgX'; sc.view_settings.look = 'AgX - Medium High Contrast'; sc.view_settings.exposure = -1.4

def mat(name, rgb, rough=0.5, metal=0.0, coat=0.0, sss=0.0):
    m = bpy.data.materials.new(name); m.use_nodes = True
    b = m.node_tree.nodes["Principled BSDF"]
    b.inputs["Base Color"].default_value = (*rgb, 1); b.inputs["Roughness"].default_value = rough
    b.inputs["Metallic"].default_value = metal
    if coat: b.inputs["Coat Weight"].default_value = coat
    return m
M = {
 "tracks": mat("rubber", (0.025, 0.025, 0.027), 0.85),
 "wheels": mat("wheel", (0.3, 0.31, 0.33), 0.4, 1.0),
 "frame":  mat("alu", (0.62, 0.63, 0.66), 0.3, 1.0),
 "base":   mat("petg_blue", (0.05, 0.12, 0.45), 0.35, coat=0.3),
 "trays":  mat("petg_grey", (0.11, 0.115, 0.125), 0.4, coat=0.2),
 "hood":   mat("case", (0.035, 0.037, 0.042), 0.42, coat=0.15),
 "covers": mat("case2", (0.05, 0.053, 0.06), 0.45, coat=0.15),
 "battery": mat("ryobi_grey", (0.06, 0.065, 0.07), 0.5),
 "bat_body": mat("ryobi_black", (0.025, 0.026, 0.028), 0.55),
 "bat_band": mat("ryobi_band", (0.42, 0.62, 0.02), 0.4, coat=0.2),
 "bat_btn": mat("ryobi_btn", (0.33, 0.34, 0.36), 0.45),
 "bat_gauge": mat("ryobi_led", (0.1, 0.9, 0.1), 0.3),
 "adapter": mat("ryobi_green", (0.42, 0.62, 0.02), 0.4, coat=0.2),
 "ibt_pcb": mat("pcb_red", (0.5, 0.03, 0.02), 0.4),
 "ibt_sink": mat("sink", (0.75, 0.76, 0.78), 0.3, 1.0),
 "uno": mat("pcb_teal", (0.0, 0.32, 0.36), 0.35),
 "uno_parts": mat("chrome", (0.8, 0.8, 0.82), 0.2, 1.0),
 "gps": mat("pcb_red2", (0.55, 0.03, 0.03), 0.4),
 "h_hitch": mat("hitch", (0.035, 0.037, 0.042), 0.42, coat=0.15),
 "h_servo": mat("servo", (0.02, 0.02, 0.02), 0.5),
 "h_links": mat("steel", (0.7, 0.71, 0.73), 0.3, 1.0),
 "h_tool": mat("tool", (0.05, 0.053, 0.06), 0.45, coat=0.15),
 "h_trailer": mat("trailer", (0.12, 0.3, 0.08), 0.5),
 "p_cup": mat("pod", (0.035, 0.037, 0.042), 0.42, coat=0.15),
 "p_lid": mat("podlid", (0.05, 0.053, 0.06), 0.45, coat=0.15),
 "p_plate": mat("steelp", (0.6, 0.61, 0.63), 0.35, 1.0),
 "p_puck": mat("puck", (0.015, 0.015, 0.017), 0.6),
 "p_coil": mat("coax", (0.02, 0.02, 0.02), 0.5),
 "switch": mat("rocker", (0.015, 0.015, 0.017), 0.5),
 "fuse": mat("fuseh", (0.02, 0.02, 0.022), 0.55),
 "lead_red": mat("lead_r", (0.6, 0.02, 0.02), 0.45),
 "lead_black": mat("lead_k", (0.02, 0.02, 0.02), 0.45),
}
for k in M:
    p = os.path.join(src, k + ".stl")
    if not os.path.exists(p) or os.path.getsize(p) < 200: continue
    bpy.ops.wm.stl_import(filepath=p)
    o = bpy.context.selected_objects[0]
    o.scale = (0.001, 0.001, 0.001)
    o.data.materials.append(M[k])
    bpy.ops.object.shade_auto_smooth(angle=math.radians(35))
# ground
bpy.ops.mesh.primitive_plane_add(size=40, location=(0.07, 0.06, -0.1305))
g = bpy.context.object; g.data.materials.append(mat("floor", (0.55, 0.55, 0.56), 0.6))
# world: soft sky
w = bpy.data.worlds.new("w"); sc.world = w; w.use_nodes = True
bg = w.node_tree.nodes["Background"]; sky = w.node_tree.nodes.new("ShaderNodeTexSky")
try: sky.sky_type = 'MULTIPLE_SCATTERING'
except Exception:
    try: sky.sky_type = 'NISHITA'
    except Exception: pass
sky.sun_elevation = math.radians(40); sky.sun_rotation = math.radians(210)
w.node_tree.links.new(sky.outputs[0], bg.inputs[0]); bg.inputs[1].default_value = 0.06
# key + fill lights
def area(loc, energy, size, rot):
    bpy.ops.object.light_add(type='AREA', location=loc); l = bpy.context.object
    l.data.energy = energy; l.data.size = size; l.rotation_euler = rot
    return l
def aim(o, tgt):
    d = Vector(tgt) - o.location; o.rotation_euler = d.to_track_quat('-Z', 'Y').to_euler()
tgt = (0.07, 0.06, 0.05)
k = area((0.55, -0.45, 0.65), 14, 0.6, (0,0,0)); aim(k, tgt)
f = area((-0.5, 0.4, 0.35), 5, 1.0, (0,0,0)); aim(f, tgt)
r = area((0.1, 0.7, 0.5), 7, 0.5, (0,0,0)); aim(r, tgt)
# camera
views = {"front34": ((-0.50, -0.50, 0.26), (0.10, 0.06, -0.02)),
         "rear34": ((0.66, 0.54, 0.28), (0.12, 0.06, -0.01)),
         "side":   ((0.12, -0.80, 0.10), (0.12, 0.06, -0.02)),
         "swap":   ((-0.5, -0.56, 0.5), (0.07, 0.06, 0.15)),
         "latch":  ((0.02, -0.36, 0.20), (0.085, 0.06, 0.045)),
         "pod":    ((0.62, -0.30, 0.42), (0.20, 0.06, 0.07)),
         "hitch":  ((0.90, -0.64, 0.20), (0.22, 0.06, -0.035)),
         "trailer": ((0.95, -0.80, 0.42), (0.32, 0.06, -0.08))}
loc, t = views[view]
bpy.ops.object.camera_add(location=loc); cam = bpy.context.object; aim(cam, t)
cam.data.lens = {"swap": 42, "trailer": 40}.get(view, 50); sc.camera = cam
cam.data.dof.use_dof = True; cam.data.dof.focus_distance = (Vector(loc) - Vector(t)).length; cam.data.dof.aperture_fstop = 8
sc.render.filepath = out
bpy.ops.render.render(write_still=True)
