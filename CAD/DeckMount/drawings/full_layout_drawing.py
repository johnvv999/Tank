import trimesh,numpy as np, matplotlib; matplotlib.use('Agg'); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon as P, Rectangle, FancyBboxPatch
from shapely.ops import unary_union
A='/tmp/claude-0/bl/A/'
base=trimesh.load(A+'base.stl'); tr=trimesh.load(A+'trays.stl')
fx0=tr.bounds[0][0]; rx1=tr.bounds[1][0]
print(fx0,rx1, base.bounds, tr.bounds)
fig=plt.figure(figsize=(15,11))
ax=fig.add_axes([0.05,0.40,0.92,0.53]); bx=fig.add_axes([0.05,0.05,0.92,0.30])
def draw(a,m,origin,normal,T,fc,lab,ec='k'):
    s=m.section(plane_origin=origin,plane_normal=normal); p,_=s.to_2D(to_2D=T); g=unary_union(p.polygons_full)
    for q in getattr(g,'geoms',[g]):
        a.add_patch(P(np.array(q.exterior.coords),fc=fc,ec=ec,lw=.5,label=lab)); lab=None
        for h in q.interiors: a.add_patch(P(np.array(h.coords),fc='white',ec=ec,lw=.3))
# --- top view: plate, then tray floors (cut at z 4.5) and base (cut at z 3)
ax.add_patch(FancyBboxPatch((0,0),152,135,boxstyle='round,pad=0,rounding_size=4',fc='silver',ec='k',lw=1,label='metal plate 152 x 135 x 3 (under the base, same outline)'))
draw(ax,base,[0,0,3],[0,0,1],np.eye(4),'#2f5fb3','base (cut 3 mm up)')
draw(ax,tr,[0,0,4.5],[0,0,1],np.eye(4),'#9aa0aa','trays (cut 2.5 mm above the plate)')
def hdim(a,x1,x2,y,txt,fs=11,col='darkred'):
    a.annotate('',(x1,y),(x2,y),arrowprops=dict(arrowstyle='<->',lw=1.2,color=col)); a.text((x1+x2)/2,y+2,txt,ha='center',fontsize=fs,color=col,fontweight='bold')
def vdim(a,y1,y2,x,txt,col='darkred'):
    a.annotate('',(x,y1),(x,y2),arrowprops=dict(arrowstyle='<->',lw=1.2,color=col)); a.text(x-2,(y1+y2)/2,txt,ha='right',va='center',rotation=90,fontsize=11,color=col,fontweight='bold')
for x in [fx0,0,152,rx1]: ax.plot([x,x],[-2,152],color='0.5',lw=.6,ls=':')
hdim(ax,fx0,0,146,f'front overhang {0-fx0:.0f}'); hdim(ax,0,152,146,'plate 152'); hdim(ax,152,rx1,146,f'rear overhang {rx1-152:.0f}')
hdim(ax,fx0,rx1,158,f'overall {rx1-fx0:.0f}',col='k')
hdim(ax,0,30,-8,'tongue 30',fs=9,col='0.25'); hdim(ax,122,152,-8,'tongue 30',fs=9,col='0.25')
vdim(ax,0,135,fx0-6,'plate 135'); vdim(ax,9.5,125.5,fx0+3,'tray 116')
ax.text(fx0/2,67.5,'FRONT TRAY\n2x IBT-2',ha='center',va='center',fontsize=12)
ax.text(152+(rx1-152)/2,67.5,'REAR TRAY\nUNO + GPS',ha='center',va='center',fontsize=12)
ax.text(76,67.5,'battery\ncradle',ha='center',va='center',fontsize=11,color='k',bbox=dict(fc='white',ec='none',alpha=.85))
ax.text(-8,-14,'← front of the tank',fontsize=10); ax.text(rx1-30,-14,'rear (hitch) →',fontsize=10)
ax.set_xlim(fx0-14,rx1+6); ax.set_ylim(-18,166); ax.set_aspect('equal'); ax.axis('off')
ax.set_title('Deck mount from above, measured from the printable files (mm)',fontsize=13)
ax.legend(loc='lower center',bbox_to_anchor=(0.5,-0.06),ncol=3,fontsize=9)
# --- side cut at y=30
T=np.array([[1,0,0,0],[0,0,1,0],[0,1,0,0],[0,0,0,1]],float)
bx.add_patch(Rectangle((0,-3),152,3,fc='silver',ec='k',lw=.6))
draw(bx,base,[0,38,0],[0,1,0],T,'#2f5fb3',None)
draw(bx,tr,[0,38,0],[0,1,0],T,'#9aa0aa',None)
for x in [fx0,0,152,rx1]: bx.plot([x,x],[-6,30],color='0.5',lw=.6,ls=':')
hdim(bx,fx0,0,22,f'{0-fx0:.0f}',fs=10); hdim(bx,0,152,22,'152',fs=10); hdim(bx,152,rx1,22,f'{rx1-152:.0f}',fs=10)
kw=dict(fontsize=9,arrowprops=dict(arrowstyle='->',color='red',lw=1.2),bbox=dict(fc='white',ec='0.6'))
bx.annotate('plate 3',(80,-1.5),(80,-12),ha='center',**kw)
bx.annotate('base floor 6\n(2 under each tongue)',(40,3),(50,-14),ha='center',**kw)
bx.annotate('tongue 4',(15,4),(15,-14),ha='center',**kw)
bx.annotate('tray floor 5,\nwalls 17 above the plate',(-40,7),(-40,-14),ha='center',**kw)
bx.annotate('tongue 4',(137,4),(137,-14),ha='center',**kw)
bx.annotate('tray floor 5',(215,4.5),(215,-14),ha='center',**kw)

bx.set_xlim(fx0-14,rx1+6); bx.set_ylim(-20,30); bx.set_aspect('equal'); bx.axis('off')
bx.set_title('Side cut through the trays and base, clear of the screw holes, 28 mm in from the left tray wall (true scale)',fontsize=11)
fig.savefig('/tmp/claude-0/full.png',dpi=120)
