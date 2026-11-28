import matplotlib; matplotlib.use('Agg'); import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, FancyBboxPatch, Circle, Polygon
fig=plt.figure(figsize=(14,15))
fig.suptitle('Measurements for a hitch at the MOTOR end (the end that becomes the rear)',fontsize=15,y=0.985)
B='#1f4fbf'; R='#c62828'
def dim(a,p1,p2,lab,col=B,off=(0,0),ha='center'):
    a.annotate('',p1,p2,arrowprops=dict(arrowstyle='<->',lw=1.6,color=col))
    a.text((p1[0]+p2[0])/2+off[0],(p1[1]+p2[1])/2+off[1],lab,color=col,fontsize=15,fontweight='bold',ha=ha,va='center',bbox=dict(fc='white',ec='none',pad=1))
# ---------- top view
ax=fig.add_axes([0.03,0.56,0.94,0.39]); ax.set_aspect('equal'); ax.axis('off')
ax.set_title('1. Top view of the plate (looking down, motor end on the right)',fontsize=12,loc='left')
ax.add_patch(FancyBboxPatch((0,0),152,135,boxstyle='round,pad=0,rounding_size=4',fc='#e8eaee',ec='k',lw=1.2))
for x,y in [(13.5,7.5),(138.5,7.5),(13.5,127.5),(138.5,127.5)]: ax.add_patch(Circle((x,y),1.8,fc='white',ec='k'))
ax.text(76,67.5,'plate 152 x 135\n(known)',ha='center',va='center',fontsize=11,color='0.4')
ax.annotate('',(125,100),(30,100),arrowprops=dict(arrowstyle='->',lw=1.5,color='0.5')); ax.text(78,104,'towards the new REAR (motor end)',ha='center',fontsize=10,color='0.4')
# slots near motor edge (example)
for y0,y1 in [(20,62),(82,127)]: ax.add_patch(FancyBboxPatch((140,y0),4,y1-y0,boxstyle='round,pad=0,rounding_size=2',fc='#ffcc80',ec='#e65100',lw=1.5))
ax.text(20,124,'slots shown are an example: mark whatever is really there',fontsize=9,ha='left',va='center',color='#e65100',transform=ax.transData,clip_on=False)
ax.plot([152,152],[-8,143],color='0.5',ls=':',lw=.8)
dim(ax,(142,-6),(152,-6),'A',off=(0,-5))
dim(ax,(162,0),(162,20),'B',off=(4,0)); dim(ax,(170,0),(170,62),'C',off=(4,0)); dim(ax,(178,0),(178,82),'D',off=(4,0)); dim(ax,(186,0),(186,127),'E',off=(4,0))
for v in [20,62,82,127]: ax.plot([144,190],[v,v],color=B,ls=':',lw=.7)
ax.plot([152,190],[0,0],color=B,lw=.7)
ax.annotate('W',(144,40),(128,40),fontsize=15,fontweight='bold',color=R,arrowprops=dict(arrowstyle='->',color=R),va='center')
ax.set_xlim(-5,215); ax.set_ylim(-16,145)
# ---------- side view (not to scale, zoomed on the motor end)
bx=fig.add_axes([0.03,0.20,0.94,0.35]); bx.set_aspect('equal'); bx.axis('off')
bx.set_title('2. Side view of the motor end, looking at the left side (not to scale)',fontsize=12,loc='left')
bx.plot([40,260],[-130,-130],color='0.3',lw=2); bx.text(42,-137,'ground',fontsize=10,color='0.3')
bx.add_patch(Rectangle((40,-6),150,6,fc='silver',ec='k')); bx.text(60,3,'metal plate',fontsize=10,color='0.3')
bx.plot([190,190],[12,-130],color='0.5',ls=':',lw=.9); bx.text(191,8,'plate edge',fontsize=9,color='0.4')
cx0,cw,ch,cg=140,30,22,6
bx.add_patch(Rectangle((cx0,-6-cg-ch),cw,ch,fc='#8d6e63',ec='k',lw=1.2)); bx.text(cx0+cw/2,-6-cg-ch/2,'steel cross\nmember',ha='center',va='center',fontsize=9,color='white')
bx.add_patch(Polygon([[60,-50],[205,-50],[235,-88],[215,-122],[70,-122],[50,-88]],closed=True,fc='none',ec='0.6',ls='--',lw=1.2)); bx.text(70,-70,'side frame / track (dashed)',fontsize=9,color='0.45')
bx.add_patch(Circle((205,-88),22,fc='none',ec='0.3',lw=1.4)); bx.add_patch(Circle((205,-88),2,fc='k'))
bx.add_patch(Rectangle((150,-100),40,24,fc='#bdbdbd',ec='k',alpha=.8)); bx.text(170,-88,'motor /\ngearbox',ha='center',va='center',fontsize=8)
bx.text(205,-116,'sprocket',ha='center',fontsize=9,color='0.3')
dim(bx,(cx0+cw,-42),(190,-42),'F',off=(0,-6))
dim(bx,(cx0,-42),(cx0+cw,-42),'G',off=(0,-6),col=R)
dim(bx,(132,-6-cg-ch),(132,-6-cg),'H',off=(-7,0),col=R)
dim(bx,(cx0+cw+5,-6),(cx0+cw+5,-6-cg),'J',off=(7,0))
dim(bx,(190,-127),(235,-127),'K',off=(0,6))
dim(bx,(250,-130),(250,-110),'L',off=(7,0))
bx.plot([215,252],[-110,-110],color=B,ls=':',lw=.8)
dim(bx,(190,-60),(205,-60),'M',off=(0,5),col=R)
bx.plot([205,205],[-62,-88],color=R,ls=':',lw=.8)
bx.set_xlim(35,270); bx.set_ylim(-142,16)
fig.text(0.04,0.185,'Please measure, in mm (motor end only; the other end is already measured):',fontsize=12,fontweight='bold',va='top')
txt=("A    plate edge to the centre line of any slots near that edge  (W = slot width)        B to E   the long edge to each end of those slots\n"
"F    plate edge to the OUTER face of the steel cross member  (0 if it is flush with the edge, minus if it sticks out past it)\n"
"G    cross member width, front to back          H    cross member height          J    gap between the plate and the cross member (0 if it touches)\n"
"      Also: are there holes in the cross member? If so, their size and where they are (a phone photo from below with a ruler is perfect)\n"
"K    how far the tracks / side frames stick out past the plate edge at that end          L    lowest point of the frame at that end, above the ground\n"
"M    plate edge to the centre of the motor shaft / sprocket axle  (and how far the motor or gearbox reaches below the plate)")
fig.text(0.04,0.16,txt,fontsize=10.5,va='top',linespacing=1.7,family='DejaVu Sans')
fig.savefig('/tmp/claude-0/meas.png',dpi=110)
