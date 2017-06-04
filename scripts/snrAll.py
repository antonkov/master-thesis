# -*- coding: utf-8 -*-
from __future__ import unicode_literals
# Plots SNR/FER for bad, good and average base matrices
import pandas as pd
import argparse
import matplotlib.pyplot as plt
import numpy as np
from scipy.stats import t

FER_THRESHOLD = 0.01
MAX_SNR = 3.5
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

def calcSNR(xs, ys):
    vals = sorted(zip(xs,ys))
    for i in range(len(vals)-1):
        if vals[i+1][1] < FER_THRESHOLD:
            (x0, y0) = vals[i]
            (x1, y1) = vals[i+1]
            frac = (y0 - FER_THRESHOLD) / (y0 - y1)
            return x0 + (x1 - x0) * frac
    return MAX_SNR + 0.1

xgreen = []
xred = []
greenTests = []
redTests = []

filenames = args.folders # rr3_2304
for filename in filenames:
    def read_ssv(f):
        return pd.read_csv(f, delim_whitespace=True, header=1, index_col=False)

    df = read_ssv('../reports/' + filename + '.rpt')

    dfTypeTest = df['Filename'].str.extract('/(?P<baseTest>[a-z0-9]+)_(?P<markTest>\d+).mtx', expand=True)
    df = pd.concat([dfTypeTest, df['SNR'], df['FER']], axis=1)
    df = df.rename(columns={'FER': 'fer', 'SNR': 'snr'})

    tests = sorted(df['baseTest'].unique().tolist())
    greenTestIds = [x for x in tests if 'top' in x]
    redTestIds = [x for x in tests if 'bot' in x]
    tests = [x for x in tests if 'test' in x]
    greenTestIds += tests[:len(tests)/2]
    redTestIds += tests[len(tests)/2:]

    cxgreen = []
    cxred = []
    for name, group in df.groupby(['baseTest', 'markTest']):
        cur = group.loc[:, ['snr', 'fer']]
        cur.snr = cur.snr.astype(float)
        cur.fer = cur.fer.astype(float)
        cur = cur[cur.fer >= 0]
        xs = cur.snr
        ys = cur.fer
        snr = calcSNR(xs, ys)
        if name[0] in greenTestIds:
            cxgreen.append(snr)
            greenTests.append((xs,ys))
        elif name[0] in redTestIds:
            cxred.append(snr)
            redTests.append((xs,ys))
    mean = np.mean(cxgreen+cxred)
    cxgreen = np.array(cxgreen) - mean
    cxred = np.array(cxred) - mean
    xgreen += cxgreen.tolist()
    xred += cxred.tolist()

def calcStudent(x1, x2):
    n1 = len(x1)
    n2 = len(x2)
    M1 = sum(x1) / n1
    M2 = sum(x2) / n2
    S1 = sum((x-M1)**2 for x in x1)/n1
    S2 = sum((x-M2)**2 for x in x2)/n2
    # Satervait
    for alpha in [0.4,0.3,0.2, 0.1, 0.05,0.01,0.001,0.0001]:
        alpha /= 2
        v1 = S1/n1
        v2 = S2/n2
        ta = (v1 * t.isf(alpha,n1-1) + v2 * t.isf(alpha,n2-1))/(v1+v2)
        print 2*alpha, ta
        #print t.isf(alpha,n1+n2-2)
    return (M1 - M2) / (S1/(n1-1) + S2/(n2-1))**0.5

tStudent = calcStudent(xred, xgreen)
print 'tStudent', tStudent

f, ax = plt.subplots(figsize=(7, 7))
ax.set(yscale='log')
ax.set_xlim([1, MAX_SNR])
for xs, ys in greenTests:
    ax.plot(xs, ys, color='green', lw=0.2)
for xs, ys in redTests:
    ax.plot(xs, ys, color='red', lw=0.2)


ax.set_xlabel('Отношение сигнал-шум (дБ)')
ax.set_ylabel('Вероятность ошибки на блок')
#plt.rc('grid', linestyle="-", color='black')
plt.grid(True)

if args.operation == DRAW:
    plt.show()
else:
    plt.savefig('../images/' + filename + '.pdf')
    plt.savefig('../images/' + filename + '.eps')
