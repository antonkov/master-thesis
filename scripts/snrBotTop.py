# Plots SNR - FER for bottom and top base matrices
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

filenames = ['rr3_2304']
for filename in filenames:
    df = read_ssv('../reports/' + filename + '.rpt')
    df2 = read_ssv('../reports/avg_' + filename + '.rpt')
    df = pd.concat([df, df2])
    df = pd.concat([df['Filename'].str.extract('(?P<type>(?:bot|top|avg)\d{1,2})_(?P<test>\d).mtx', expand=True),
               df['SNR'], df['FER']], axis=1)
    df = df.rename(columns={'FER': 'fer'})

    f, ax = plt.subplots(figsize=(7, 7))

    tops = []
    bots = []
    avgs = []
    size = 42
    for name, group in df.groupby(['type', 'test']):
        cur = group.loc[:, ['SNR', 'fer']]
        cur.SNR = cur.SNR.astype(float)
        cur.fer = cur.fer.astype(float)
        cur = cur[cur.fer >= 0]
        xs = cur.SNR
        ys = cur.fer
        if 'bot' in name[0]:
            bots.append((xs, ys))
        if 'top' in name[0]:
            tops.append((xs, ys))
        if 'avg' in name[0]:
            avgs.append((xs, ys))

    ax.set(yscale='log')
    #plot, = ax.semilogy(xs, ys, color=col, lw=0.2)
    for xs, ys in bots:
        ax.plot(xs, ys, color='red', lw=0.2)
    for xs, ys in avgs:
        ax.plot(xs, ys, color='blue', lw=0.2)
    for xs, ys in tops:
        ax.plot(xs, ys, color='green', lw=0.2)

    #sns.tsplot(data, time=x, err_style='unit_traces')
    #sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

    plt.show()
    #plt.savefig('../images/' + filename + '.png')
