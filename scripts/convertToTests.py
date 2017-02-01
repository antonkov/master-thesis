import numpy as np
import os

if not os.path.exists("tests"):
    os.makedirs("tests")
k = 3
n = 7
M = 21
with open("list.txt", "r") as inputList:
    idx = 0
    for line in inputList.readlines():
        values = list(map(int, line.split()))
        if len(values) != k * n:
            continue
        hd = np.reshape(values, (k, n), order='F')
        with open("tests/hd{:03d}".format(idx), "w") as out:
            out.write("proto_matrix\n")
            out.write("{} {}\n".format(k, n))
            out.write("{}\n".format(M))
            for i in range(k):
                for j in range(n):
                    out.write(str(hd[i][j]) + " ")
                out.write("\n")
        idx += 1
