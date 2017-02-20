import sys
import pandas as pd


def read_ssv(filename):
    return pd.read_csv(filename, delim_whitespace=True, header=1, index_col=False)

reportName = sys.argv[1]
spectrum = read_ssv(reportName)
spectrum['test'] = spectrum['Filename'].str.extract('(\d*).mtx', expand=False)
spectrum = spectrum.loc[:, ['test', 'spectrum1', 'spectrum2']]
spectrum = spectrum.sort_values(by=['spectrum1', 'spectrum2'])
print 'best'
print spectrum.iloc[0]
print 'worse'
print spectrum.iloc[-1]