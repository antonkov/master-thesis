# Plots SNR - FER for bottom and top base matrices
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set(style="darkgrid", palette="Set2")


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

types = ['g', 'r', 'q']
sizes = ['576', '2304']
for size in sizes:
    for type in types:
        def read_df(name):
            df = read_ssv('../reports/' + name + '.rpt')
            df = pd.concat([df['Filename'].str.extract('(?P<type>(?:bot|top)\d{1,2})_(?P<test>\d).mtx', expand=True),
                            df['SNR'], df['FER']], axis=1)
            df = df.rename(columns={'FER': 'fer'})
            return df

        df3 = read_df(type + '3_' + size)
        df4 = read_df(type + '4_' + size)

        f, ax = plt.subplots(figsize=(7, 7))

        def plot_graph(group, col):
            cur = group.loc[:, ['SNR', 'fer']]
            cur.SNR = cur.SNR.astype(float)
            cur.fer = cur.fer.astype(float)
            cur = cur[cur.fer >= 0]
            xs = cur.SNR
            ys = cur.fer
            plot, = ax.semilogy(xs, ys, color=col, lw=0.08)

        for name, group in df3.groupby(['type', 'test']):
            plot_graph(group, 'green' if 'top' in name[0] else 'red')
        for name, group in df4.groupby(['type', 'test']):
            plot_graph(group, 'blue' if 'top' in name[0] else 'orange')

        #sns.tsplot(data, time=x, err_style='unit_traces')
        #sns.tsplot(data, time=x, err_style="boot_traces", n_boot=500)

        plt.savefig('../images/' + 'comp_' + type + '_' + size + '.png')
