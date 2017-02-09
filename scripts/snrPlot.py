# Plots SNR - FER for set of matrices.
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


df = read_ssv('../reports/allSnrAllRandom.txt')
df = pd.concat([df['Filename'].str.extract('b5_10_\d{4}_(?P<test>\d{3})', expand=True),
           df['SNR'], df['FER%']], axis=1)
df = df.rename(columns={'FER%': 'fer'})
df.drop_duplicates(subset=['test', 'fer'], inplace=True)
df.index = pd.MultiIndex.from_arrays(df[['test', 'SNR']].values.T)
del df['test']
del df['SNR']
data = []
for test, row_df in df.groupby(level=0):
    data.append(row_df['fer'].values)
x = df.index.levels[1]

f, ax = plt.subplots(figsize=(7, 7))
ax.set(yscale='log')
# Plot the average over replicates with bootstrap resamples
for ys in data:
    sns.plt.plot(x[:len(ys)], ys, color='green', lw=0.3)
#sns.tsplot(data, time=x, err_style='unit_traces')
#sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

sns.plt.show()