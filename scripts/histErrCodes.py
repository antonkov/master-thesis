import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import CheckButtons
import matplotlib.style as style
from collections import OrderedDict

style.use('ggplot')

def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

report = read_ssv('../reports/MarkMtx5Report.txt')
spectrum = read_ssv('../reports/BaseMtx5Spectrum.txt')
report = pd.concat([report['Filename'].str.extract('b5_10_(?P<base>\d{4})_(?P<numTest>\d{3})', expand=True),
           report['FER%']], axis=1)
spectrum = pd.concat([spectrum.Filename.str.extract('b5_10_(?P<base>\d{4})', expand=True),
                      spectrum.drop('Filename', 1)], axis=1)
df = pd.merge(report, spectrum, on='base')
df = df.rename(columns={'FER%':'fer'})
df.base = df.base.astype(int)
df.numTest = df.numTest.astype(int)
df.fer = df.fer.astype(float)
df.index = pd.MultiIndex.from_arrays(df[['base', 'numTest']].values.T)
del df['base']
del df['numTest']

grouped = df.groupby(level=0)
stat = grouped.agg(OrderedDict([('spectrum1', 'first'),
                                ('spectrum2', 'first'),
                                ('spectrum3', 'first'),
                                ('fer', 'mean')]))
stat = stat.sort_values(by='fer')
print stat

fig, ax = plt.subplots()
plt.subplots_adjust(right=0.8)
rax = plt.axes([0.82, 0.1, 0.1, 0.8])
#n, = df.base.unique().shape
n_bins = 10
n = 10
buttonStates = [False] * n
cols = ['blue', 'red', 'orange', 'green']
inRange = lambda x: x < 5
data = []
for i in range(0, n):
    xs = df.loc[i].fer.tolist()
    data.append(filter(inRange, xs))

check = CheckButtons(rax, map(str, range(n)), buttonStates)


def func(label):
    label = int(label)
    ax.cla()
    buttonStates[label] = not buttonStates[label]
    dataToPlot = []
    for i in range(len(buttonStates)):
        if buttonStates[i]:
            dataToPlot.append(data[i])
    ax.hist(dataToPlot, n_bins, histtype='bar')
    plt.draw()

check.on_clicked(func)

plt.show()
