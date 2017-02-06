import pandas as pd
import numpy as np
import seaborn as sns
sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


df = read_ssv('../reports/allSnrAllRandom.txt')
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

# Plot the average over replicates with bootstrap resamples
sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

sns.plt.show()