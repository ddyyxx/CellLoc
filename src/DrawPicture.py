#!/usr/bin/env python
# a bar plot with errorbars
# encoding: utf-8
import numpy as np
import matplotlib.pyplot as plt

filearray = [
    'Eval_Precise_GPS1min.json',  # 0
    'Eval_Precise_GPS2min.json',  # 1
    'Eval_Precise_GPS30s.json',  # 2
    'Eval_Precise_TAall.json',  # 3
    'Eval_Precise_TAwithoutangle.json',  # 4
    'Eval_Precise_TAwithoutBSandTA.json',  # 5
    'Eval_Precise_TAwithoutBSchange.json',  # 6
    'Eval_Precise_TAwithoutpostprocess.json',  # 7
    'Eval_Precise_TAwithoutpreprocess.json',  # 8
    'Eval_Precise_TAwithoutTAchange.json',  # 9
    'Eval_Precise_US.json',  # 10
    'Eval_Recall_GPS1min.json',  # 11
    'Eval_Recall_GPS2min.json',  # 12
    'Eval_Recall_GPS30s.json',  # 13
    'Eval_Recall_TAall.json',  # 14
    'Eval_Recall_TAwithoutangle.json',  # 15
    'Eval_Recall_TAwithoutBSandTA.json',  # 16
    'Eval_Recall_TAwithoutBSchange.json',  # 17
    'Eval_Recall_TAwithoutpostprocess.json',  # 18
    'Eval_Recall_TAwithoutpreprocess.json',  # 19
    'Eval_Recall_TAwithoutTAchange.json',  # 20
    'Eval_Recall_US.json',  # 21
    'Eval_Precise_withoutAll.json',
    'Eval_Recall_withoutAll.json'
]

label1 = [
    'GPS 1 min',
    'GPS 2 min',
    'GPS 30 sec',
    'With All',
    'With Direction Info',
    'With Cell Tower Switching',
    'With Cell Tower Switching',
    'With Postprocessing',
    'With Preprocessing',
    'With TA changing',
    'High Density',
    'GPS 1 min',
    'GPS 2 min',
    'GPS 30 sec',
    'With All',
    'With Direction Info',
    'With Cell Tower Switching',
    'With Cell Tower Switching',
    'With Postprocessing',
    'With Preprocessing',
    'With TA changing',
    'High Density',
    'CellLoc',
    'CellLoc'
]

label2 = [
    'GPS 1 min',
    'GPS 2 min',
    'GPS 30 sec',
    'With All',
    'Without Direction Info',
    'Without Cell Tower Switching',
    'Without Cell Tower Switching',
    'Without Postprocessing',
    'Without Preprocessing',
    'Without TA changing',
    'Low Density',
    'GPS 1 min',
    'GPS 2 min',
    'GPS 30 sec',
    'With All',
    'Without Direction Info',
    'Without Cell Tower Switching',
    'Without Cell Tower Switching',
    'Without Postprocessing',
    'Without Preprocessing',
    'Without TA changing',
    'Low Density',
    'Plain CellLoc',
    'Plain CellLoc'
]

datadir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\evaluation\\'


def returnStrList(filedir):
    File = open(filedir, 'r')
    x = File.readlines()
    n = len(x)
    for i in range(n):
        x[i] = float(x[i])
    x.sort()
    return x


def drawCDF(taall, index, xlabel, ylabel):
    plt.clf()
    dir1 = datadir + \
        filearray[taall]
    x1 = returnStrList(dir1)
    y1 = np.linspace(1, 1, len(x1))
    plt.plot(x1, np.add.accumulate(y1) / np.sum(y1),
             color='red', label=label1[index])
    dir2 = datadir + filearray[index]
    x2 = returnStrList(dir2)
    y2 = np.linspace(1, 1, len(x2))
    plt.plot(x2, np.add.accumulate(y2) / np.sum(y2),
             'b--', label=label2[index])
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    plt.xlim(0.5, 1)

    plt.legend(loc='upper left')
    plt.savefig(datadir + filearray[index][:-5])

    # plt.show()


def drawCDFGPS():
    plt.clf()
    dir1 = datadir + \
        filearray[14]
    x1 = returnStrList(dir1)
    y1 = np.linspace(1, 1, len(x1))
    plt.plot(x1, np.add.accumulate(y1) / np.sum(y1),
             color='red', label='CellLoc')
    dir2 = datadir + filearray[11]
    x2 = returnStrList(dir2)
    y2 = np.linspace(1, 1, len(x2))
    plt.plot(x2, np.add.accumulate(y2) / np.sum(y2),
             'b--', label='GPS 1min')
    dir3 = datadir + filearray[13]
    x3 = returnStrList(dir3)
    y3 = np.linspace(1, 1, len(x3))
    plt.plot(x3, np.add.accumulate(y3) / np.sum(y3),
             'go-', label='GPS 30s')
    plt.xlabel('Recall(Matched Length/True Length)')
    plt.ylabel('Probability')

    plt.xlim(0.5, 1)

    plt.legend(loc='upper left')
    plt.savefig(datadir + 'comparewithGPSRecall')
    # plt.show()

# drawCDFGPS()

drawCDF(3, 22, 'Precision(Matched Length/Output Length)', 'Probability')
drawCDF(14, 23, 'Recall(Matched Length/True Length)', 'Probability')


def solve():
    for i in range(11):
        if 4 <= i <= 10:
            drawCDF(3, i, 'Precision(Matched Length/Output Length)', 'Probability')
            drawCDF(14, i + 11, 'Recall(Matched Length/True Length)', 'Probability')
# solve()


def drawCDFdisError():
    plt.clf()
    dir1 = datadir + 'disError.json'
    x = returnStrList(dir1)
    y = np.linspace(1, 1, len(x))
    plt.plot(x, np.add.accumulate(y) / np.sum(y), color='red')
    plt.xlabel('Localization Error(meters)')
    plt.ylabel('Probability')
    plt.savefig(datadir + 'LocError')
    plt.show()
# drawCDFdisError()

# dirx = 'G:\\mongomap\\Python\\mapArcLength.txt'
# x = returnStrList(dirx)
# print np.sum(x) / len(x)
