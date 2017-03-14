# Plots SNR - FER for bottom and top base matrices
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)


df = read_ssv('../reports/g3_2304.rpt')
df = pd.concat([df['Filename'].str.extract('(?P<type>(?:bot|top)\d{1,2})_(?P<test>\d).mtx', expand=True),
           df['SNR'], df['FER']], axis=1)
df = df.rename(columns={'FER': 'fer'})

f, ax = plt.subplots(figsize=(7, 7))

for name, group in df.groupby(['type', 'test']):
    cur = group.loc[:, ['SNR', 'fer']]
    cur.SNR = cur.SNR.astype(float)
    cur.fer = cur.fer.astype(float)
    cur = cur[cur.fer >= 0]
    xs = cur.SNR
    ys = cur.fer
    col = 'red'
    if 'top' in name[0]:
        col = 'green'
    plot, = ax.semilogy(xs, ys, color=col, lw=0.2)

#sns.tsplot(data, time=x, err_style='unit_traces')
#sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

plt.show()