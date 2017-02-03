import numpy as np
import os
import sys

argv = sys.argv[1:]
testsList = argv[0]
testsFolder = argv[1]

if not os.path.exists(testsFolder):
    os.makedirs(testsFolder)
k = 3
n = 7
M = 21
with open(testsList, "r") as inputList:
    idx = 0
    for line in inputList.readlines():
        values = list(map(int, line.split()))
        if len(values) != k * n:
            continue
        hd = np.reshape(values, (k, n), order='F')
        with open(testsFolder + "/hd{:03d}".format(idx), "w") as out:
            out.write("proto_matrix\n")
            out.write("{} {}\n".format(k, n))
            out.write("{}\n".format(M))
            for i in range(k):
                for j in range(n):
                    out.write(str(hd[i][j]) + " ")
                out.write("\n")
        idx += 1
