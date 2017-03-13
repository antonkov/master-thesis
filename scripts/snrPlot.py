# Plots SNR - FER for set of matrices.
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


df = read_ssv('snrtest')
df = pd.concat([df['Filename'].str.extract('_(?P<test>\d{3})', expand=True),
           df['SNR'], df['FER']], axis=1)
df = df.rename(columns={'FER': 'fer'})
df.drop_duplicates(subset=['test', 'fer'], inplace=True)
df.index = pd.MultiIndex.from_arrays(df[['test', 'SNR']].values.T)
del df['test']
del df['SNR']
data = []
for test, row_df in df.groupby(level=0):
    data.append(row_df['fer'].values)
x = df.index.levels[1]

f, ax = plt.subplots(figsize=(7, 7))
for ys in data:
    plot, = ax.semilogy(x[:len(ys)], ys, color='blue')
#sns.tsplot(data, time=x, err_style='unit_traces')
#sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

plt.show()