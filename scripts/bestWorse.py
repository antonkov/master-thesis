import sys
import pandas as pd


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

reportName = sys.argv[1]
spectrum = read_ssv(reportName)
spectrum['test'] = spectrum['Filename'].str.extract('(\d*).mtx', expand=False)
spectrum = spectrum.loc[:, ['test', 'spectrum1', 'spectrum2']]
for i in range(1,3):
    specColumn = 'spectrum' + str(i)
    spectrum[['len' + str(i), 'cnt' + str(i)]] = spectrum[specColumn].str.extract('(\d*):(\d*)', expand=False)
    del spectrum[specColumn]

spectrum = spectrum.apply(pd.to_numeric)

spectrum = spectrum.sort_values(by=['len1', 'cnt1', 'len2', 'cnt2'])
spectrum = spectrum.set_index(['test'])
print 'best'
print spectrum.iloc[:3]
print 'worse'
print spectrum.iloc[-3:]
