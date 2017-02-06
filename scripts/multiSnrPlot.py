import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import CheckButtons
import matplotlib

matplotlib.rcParams.update({'font.size': 10})
matplotlib.rcParams['figure.figsize'] = 20, 11

def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


# Data parsing
df = read_ssv('../reports/ComparableTest100KReport.txt')
spectrum = read_ssv('../reports/ComparableTestSpectrum.txt')
spectrum = pd.concat([spectrum['Filename'].str.extract('b5_10_\d{4}_(?P<test>\d{3})', expand=True),
                      spectrum.drop('Filename', 1)], axis=1)
print spectrum
df = pd.concat([df['Filename'].str.extract('b5_10_\d{4}_(?P<test>\d{3})', expand=True),
           df['SNR'], df['FER%']], axis=1)
df = df.rename(columns={'FER%': 'fer'})
df.index = pd.MultiIndex.from_arrays(df[['test', 'SNR']].values.T)
del df['test']
del df['SNR']
data = []
for test, row_df in df.groupby(level=0):
    data.append(row_df['fer'].values)
x = df.index.levels[1]

# Plotting
fig, ax = plt.subplots()
plt.subplots_adjust(right=0.75)
rax = plt.axes([0.76, 0.1, 0.23, 0.8], frameon=True)
n = len(data)

names = []
plots = []
id = 0
for y in data:
    s = ' '.join(spectrum.loc[id].values)
    names.append(s)
    plot, = ax.plot(x, y, label=s, visible=False)
    plots.append(plot)
    id += 1

check = CheckButtons(rax, names, [False] * n)
#for rect in check.rectangles:
#    rect.set_width(0.15)
#    rect.set_height(0.03)

def func(label):
    label = int(label.split()[0])
    plots[label].set_visible(not plots[label].get_visible())
    plt.draw()

check.on_clicked(func)

plt.show()

