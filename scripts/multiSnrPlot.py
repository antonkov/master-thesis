import pandas as pd
import matplotlib.pyplot as plt

def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


df = read_ssv('../reports/ComparableTestReport.txt')
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

id = 0
for y in data:
    plt.plot(x, y, label=str(id))
    id += 1

plt.legend()

plt.show()

