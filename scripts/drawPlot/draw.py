import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

cntGroups = 0
handles = []
colors = ['r', 'b', 'g', 'y']
fig = plt.figure()
ax = fig.add_axes([0.1, 0.1, 0.6, 0.75])
with open('points', 'r') as f:
    m = int(f.readline())
    for it in range(m):
        name = f.readline()
        ptsx = []
        ptsy = []
        cntGroups += 1
        n = int(f.readline())
        for i in range(n):
            val = float(f.readline())
            ptsx.append(val)
            noise = np.random.normal(0, 0.3);
            ptsy.append(cntGroups + noise)
        patch = mpatches.Patch(color=colors[it], label=name)
        handles.append(patch)
        ax.plot(ptsx, ptsy, colors[it] + 'x')
ax.legend(handles=handles,
        bbox_to_anchor=(1.05, 1), loc=2, borderaxespad=0., prop={'size':6})
plt.show()
