# -*- coding: utf-8 -*-
from __future__ import unicode_literals
# Plots SNR/FER for bad, good and average base matrices
import sys
import pandas as pd
import os
import seaborn as sns
import argparse
import matplotlib.pyplot as plt

#sns.set(style="darkgrid", palette="Set2")
plt.style.use('seaborn-ticks')

DRAW = 1
SAVE = 0
parser = argparse.ArgumentParser(description='Draw or save images of SNR/FER graphs for LDPC-codes')
parser.add_argument('folders', metavar='folder', type=str, nargs='+',
                  help='folders with simulation result to draw')
parser.add_argument('--draw', dest='operation', action='store_const',
                    const=DRAW, default=SAVE,
                    help='draw images in window (default: save images to files)')
args = parser.parse_args()

filenames = args.folders # rr3_2304
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
    bot_plot, avg_plot, top_plot = None, None, None
    for xs, ys in bots:
        bot_plot, = ax.plot(xs, ys, color='red', lw=0.2)
    for xs, ys in avgs:
        avg_plot, = ax.plot(xs, ys, color='blue', lw=0.2)
    for xs, ys in tops:
        top_plot, = ax.plot(xs, ys, color='green', lw=0.2)

    ax.set_xlabel('Отношение сигнал-шум (дБ)')
    ax.set_ylabel('Вероятность ошибки на блок')
    #plt.rc('grid', linestyle="-", color='black')
    plt.grid(True)

    if args.operation == DRAW:
        plt.show()
    else:
        plt.savefig('../images/' + filename + '.pdf')
        plt.savefig('../images/' + filename + '.eps')
