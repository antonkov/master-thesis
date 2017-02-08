import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import CheckButtons
from collections import OrderedDict
import matplotlib

matplotlib.rcParams.update({'font.size': 10})
matplotlib.rcParams['figure.figsize'] = 20, 11


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

reportName = 'RandomBase5_19AtLeast6_19'
report = read_ssv('../reports/' + reportName + 'Report.txt')
spectrum = read_ssv('../reports/' + reportName + 'Spectrum.txt')
report = pd.concat([report['Filename'].str.extract('b5_10_\d{4}_(?P<test>\d*)', expand=True),
           report['FER%']], axis=1)
spectrum = pd.concat([spectrum.Filename.str.extract('b5_10_\d{4}_(?P<test>\d*)', expand=True),
                      spectrum.drop('Filename', 1)], axis=1)
df = pd.merge(report, spectrum, on='test')
df = df.rename(columns={'FER%':'fer'})
df.test = df.test.astype(int)
df = df.set_index(['test'])
df.fer = df.fer.astype(float)

def spectrumList(row):
    SPEC_LEN = 15
    spec = [0] * SPEC_LEN
    for i in range(1, 4):
        lenCycle, cnt = map(int, row['spectrum{}'.format(i)].split(':'))
        spec[lenCycle] = cnt
    return spec

specs = df.apply(spectrumList, axis=1)
n = len(specs.index)
print 'specs done'

cntComparable = 0
betterSnr = 0
for i in range(n):
    print i
    for j in range(n):
        si = specs[i]
        sj = specs[j]
        less = False
        more = False
        for x, y in zip(si, sj):
            if x > y:
                more = True
                break
            elif x < y:
                less = True
        if more:
            continue
        if less:
            cntComparable += 1
            feri = df.fer.loc[i]
            ferj = df.fer.loc[j]
            if feri <= ferj:
                betterSnr += 1
print betterSnr
print cntComparable
