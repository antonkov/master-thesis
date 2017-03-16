import numpy as np
import sys

for filename in sys.argv[1:]:
    with open(filename, 'r') as f:
        f.readline() # type
        n, m = map(int, f.readline().split()) # size
        f.readline() # M
        a = []
        for i in range(n):
            a.append(map(int, f.readline().split()))
        w = np.zeros(m, np.int)
        for i in range(n):
            for j in range(m):
                if a[i][j] != -1:
                    w[j] += 1
        print filename
        for j in range(m):
            print w[j],
        print ""
