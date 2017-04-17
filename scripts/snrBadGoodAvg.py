# Plots SNR/FER for bad, good and average base matrices
import sys
import pandas as pd
import os
import seaborn as sns
import matplotlib.pyplot as plt

#sns.set(style="darkgrid", palette="Set2")
plt.style.use('seaborn-ticks')

filenames = sys.argv[1:] # rr3_2304
for filename in filenames:
    def read_ssv(f):
        return pd.read_csv(f, delim_whitespace=True, header=1, index_col=False)

    df = read_ssv('../reports/' + filename + '.rpt')

    avg_file = '../reports/avg_' + filename + '.rpt'
    if os.path.exists(avg_file):
        df2 = read_ssv(avg_file)
        df = pd.concat([df, df2])

    dfTypeTest = df['Filename'].str.extract('(?P<type>(?:bot|top|avg)\d{1,2})_(?P<test>\d).mtx', expand=True)
    df = pd.concat([dfTypeTest, df['SNR'], df['FER']], axis=1)
    df = df.rename(columns={'FER': 'fer', 'SNR': 'snr'})

    tops = []
    bots = []
    avgs = []
    for name, group in df.groupby(['type', 'test']):
        cur = group.loc[:, ['snr', 'fer']]
        cur.snr = cur.snr.astype(float)
        cur.fer = cur.fer.astype(float)
        cur = cur[cur.fer >= 0]
        xs = cur.snr
        ys = cur.fer
        if 'bot' in name[0]:
            bots.append((xs, ys))
        if 'top' in name[0]:
            tops.append((xs, ys))
        if 'avg' in name[0]:
            avgs.append((xs, ys))

    f, ax = plt.subplots(figsize=(7, 7))
    ax.set(yscale='log')
    ax.set_xlim([1, 3.5])
    for xs, ys in bots:
        ax.plot(xs, ys, color='red', lw=0.2)
    for xs, ys in avgs:
        ax.plot(xs, ys, color='blue', lw=0.2)
    for xs, ys in tops:
        ax.plot(xs, ys, color='green', lw=0.2)

    #plt.rc('grid', linestyle="-", color='black')
    plt.grid(True)

    plt.savefig('../images/' + filename + '.pdf')
    plt.savefig('../images/' + filename + '.eps')
