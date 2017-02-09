# Plot histogram for every Base Matrix on matrices Random Marked from there based on FER.
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import CheckButtons
from collections import OrderedDict
import matplotlib

matplotlib.rcParams.update({'font.size': 10})
matplotlib.rcParams['figure.figsize'] = 20, 11

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
plt.xlabel('fer')
plt.subplots_adjust(right=0.75)
rax = plt.axes([0.76, 0.1, 0.23, 0.8], frameon=True)
#n, = df.base.unique().shape
n_bins = 10
n = 10
buttonStates = [False] * n
cols = ['blue', 'red', 'orange', 'green', 'black', 'yellow', 'magenta', 'grey', 'cyan', 'darkgreen']
cols = cols[:n]
inRange = lambda x: x < 5
data = []
names = []
for i in range(0, n):
    name = ' '.join(map(str, [i] + list(stat.iloc[i].values)))
    names.append(name)
    xs = df.loc[i].fer.tolist()
    data.append(filter(inRange, xs))

rax.set_title('test spec1 spec2 spec3 fer')
check = CheckButtons(rax, names, buttonStates)

def func(label):
    label = int(label.split()[0])
    ax.cla()
    buttonStates[label] = not buttonStates[label]
    dataToPlot = []
    for i in range(len(buttonStates)):
        if buttonStates[i]:
            dataToPlot.append(data[i])
    ax.hist(dataToPlot, n_bins, histtype='bar')
    plt.draw()

plt.legend()
check.on_clicked(func)

plt.show()
