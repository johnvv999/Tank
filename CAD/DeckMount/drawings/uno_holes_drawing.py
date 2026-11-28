import trimesh,numpy as np, matplotlib; matplotlib.use('Agg'); import matplotlib.pyplot as plt
from matplotlib.patches import Polygon as P, Rectangle, Circle
from shapely.ops import unary_union
m=trimesh.load('/home/user/tank/CAD/DeckMount/stl/deck_mount_rear_tray.stl')
def holes(z,lo,hi):
    s=m.section(plane_origin=[0,0,z],plane_normal=[0,0,1]); p,_=s.to_2D(to_2D=np.eye(4))
    out=[]
    for q in unary_union(p.polygons_full).geoms:
        for h in q.interiors:
            from shapely.geometry import Polygon
            g=Polygon(h)
            if lo<g.area<hi: out.append((g.centroid.x,g.centroid.y,2*np.sqrt(g.area/np.pi)))
    return out
# standoff outer: polygons at z=12 with small area
s=m.section(plane_origin=[0,0,12],plane_normal=[0,0,1]); p,_=s.to_2D(to_2D=np.eye(4))
from shapely.geometry import Polygon as SP
so=[(q.centroid.x,q.centroid.y,2*np.sqrt(SP(q.exterior).area/np.pi)) for q in p.polygons_full if 20<q.area<30]
H=holes(12,6,12)
# keep UNO ones (round standoffs), match to standoffs
H=[h for h in H if abs(h[2]-3.4)<0.3]
sd0=so[0][2]
so=[(h[0],h[1],sd0) for h in H]
H=sorted(H,key=lambda h:(-h[0],h[1]))
print(so); print(H)
zt=m.bounds[1][2]
# board placement (USB end to +x); UNO edge at x=84, y from 13
X0,Y0,L,W=84-68.58,15.5-2.54,68.58,53.34
fig=plt.figure(figsize=(13,9.5))
ax=fig.add_axes([0.04,0.08,0.62,0.82])
ax.add_patch(Rectangle((X0,Y0),L,W,fc='#d9efef',ec='teal',lw=1.5))
ax.text(X0+L+1.5,Y0+W/2,'USB-C and barrel jack edge\n(faces the rear window)',rotation=90,va='center',fontsize=9,color='teal')
nm={}
for (x,y,d),(sx,sy,sd) in zip(H,so):
    ax.add_patch(Circle((sx,sy),sd/2,fc='#9aa0aa',ec='k',lw=.8,zorder=3)); ax.add_patch(Circle((x,y),d/2,fc='white',ec='k',lw=.8,zorder=4))
    ax.plot([x-1,x+1],[y,y],'k',lw=.4,zorder=5); ax.plot([x,x],[y-1,y+1],'k',lw=.4,zorder=5)
# label holes by position
pts={}
for x,y,d in H:
    k=('R' if x>50 else 'L')+('T' if y>40 else 'B'); pts[k]=(x,y)
def dim(a,b,off,horiz,txt=None):
    (x1,y1),(x2,y2)=a,b
    if horiz:
        yy=off; ax.plot([x1,x1],[y1,yy],'0.5',lw=.6); ax.plot([x2,x2],[y2,yy],'0.5',lw=.6)
        ax.annotate('',(x1,yy),(x2,yy),arrowprops=dict(arrowstyle='<->',lw=1,color='darkred'))
        ax.text((x1+x2)/2,yy+0.8,txt or f'{abs(x2-x1):.2f}',ha='center',fontsize=10,color='darkred',bbox=dict(fc='white',ec='none',pad=.5))
    else:
        xx=off; ax.plot([x1,xx],[y1,y1],'0.5',lw=.6); ax.plot([x2,xx],[y2,y2],'0.5',lw=.6)
        ax.annotate('',(xx,y1),(xx,y2),arrowprops=dict(arrowstyle='<->',lw=1,color='darkred'))
        ax.text(xx+0.8,(y1+y2)/2,txt or f'{abs(y2-y1):.2f}',va='center',rotation=90,fontsize=10,color='darkred',bbox=dict(fc='white',ec='none',pad=.5))
RT,RB,LT,LB=pts['RT'],pts['RB'],pts['LT'],pts['LB']
dim(LT,RT,Y0+W+5,True); dim(LB,RB,Y0-5,True)
dim(RB,RT,X0+L+7,False); dim(LB,LT,X0-6,False)
dim(LT,LB,X0+3,False) if False else None
# diagonal-free extra: x offset between the two right holes
dim(RB,RT,X0+L-26,True,f'{abs(RT[0]-RB[0]):.2f}') if False else None
ax.text(RT[0]-1,RT[1]-6,f'RT is {abs(RT[0]-RB[0]):.2f} further\nfrom the USB edge than RB',fontsize=8,ha='right',color='0.3')
ex=X0+L
ax.text(X0+L/2,Y0+W/2+6,'Hole centre to board edge:',ha='center',fontsize=9,fontweight='bold')
ax.text(X0+L/2,Y0+W/2-4,f'LT, LB: {LT[0]-X0:.2f} from the left edge\nRT: {ex-RT[0]:.2f} from the USB edge, {Y0+W-RT[1]:.2f} from the top edge\nRB: {ex-RB[0]:.2f} from the USB edge, {RB[1]-Y0:.2f} from the bottom edge\nLT: {Y0+W-LT[1]:.2f} from the top edge, LB: {LB[1]-Y0:.2f} from the bottom edge',ha='center',va='center',fontsize=8.5)
ax.text(X0+L/2,Y0-10.5,'Same pattern as the datasheet (ABX00087 p.19), turned 180 degrees so the USB edge is on the right',ha='center',fontsize=8.5,color='0.3')
for k,(x,y) in pts.items(): ax.text(x+(-4 if k[0]=='L' else 4),y+3.5,f'{k}',fontsize=9,ha='center',fontweight='bold')
ax.set_xlim(X0-14,X0+L+14); ax.set_ylim(Y0-12,Y0+W+12); ax.set_aspect('equal'); ax.axis('off')
ax.set_title('UNO R4 WiFi holes on the reprinted rear tray, seen from above (rear of the tank to the right)\nmeasured from deck_mount_rear_tray.stl, centre to centre, mm',fontsize=11)
# table
ds={'RT':(13.97,2.54),'RB':(15.24,50.80),'LT':(66.04,7.62),'LB':(66.04,35.56)}
t=fig.add_axes([0.66,0.47,0.33,0.4]); t.axis('off')
rows=[]
import itertools
for a,b in [('LT','RT'),('LB','RB'),('RB','RT'),('LB','LT'),('LT','RB'),('LB','RT')]:
    mm=np.hypot(pts[a][0]-pts[b][0],pts[a][1]-pts[b][1]); dd=np.hypot(ds[a][0]-ds[b][0],ds[a][1]-ds[b][1])
    rows.append([f'{a} to {b}',f'{mm:.2f}',f'{dd:.2f}',f'{mm-dd:+.2f}'])
tb=t.table(cellText=rows,colLabels=['Holes','Tray STL','Datasheet','Diff'],loc='upper center',cellLoc='center'); tb.scale(1,1.6); tb.set_fontsize(10)
t.set_title('Straight-line spacing (mm)',fontsize=11)
# standoff side view
s2=fig.add_axes([0.70,0.08,0.26,0.36]); s2.set_aspect('equal')
sd=so[0][2]; hd=H[0][2]
s2.add_patch(Rectangle((-14,0),28,5,fc='#9aa0aa',ec='k')); s2.add_patch(Rectangle((-sd/2,5),sd,zt-5,fc='#9aa0aa',ec='k'))
s2.add_patch(Rectangle((-hd/2,0),hd,zt,fc='white',ec='k',ls='--'))
s2.add_patch(Rectangle((-12,zt),24,1.6,fc='#2a8c8c',ec='k')); s2.text(0,zt+3,'UNO board 1.6',ha='center',fontsize=8)
s2.annotate('',(9,0),(9,5),arrowprops=dict(arrowstyle='<->')); s2.text(10,2.5,'floor 5',va='center',fontsize=8)
s2.annotate('',(9,5),(9,zt),arrowprops=dict(arrowstyle='<->')); s2.text(10,(5+zt)/2,f'standoff {zt-5:.0f}',va='center',fontsize=8)
s2.annotate('',(-sd/2,zt+7),(sd/2,zt+7),arrowprops=dict(arrowstyle='<->')); s2.text(0,zt+8,f'standoff dia {sd:.1f}',ha='center',fontsize=8)
s2.text(-14,-4.5,f'hole dia {hd:.1f} through (M3 clearance)\nM3 x 20 down from the top, nut under the floor\nboard sits {zt:.0f} mm above the tray bottom',fontsize=8,va='top')
s2.set_xlim(-15,24); s2.set_ylim(-14,zt+11); s2.axis('off'); s2.set_title('Standoff (side view)',fontsize=10)
fig.savefig('/tmp/claude-0/uno_dims.png',dpi=130)
