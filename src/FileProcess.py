# encoding: utf-8
import json as js
import os
import math as Math


def max(a, b):
    if(a > b):
        return a
    else:
        return b


def min(a, b):
    if(a < b):
        return a
    else:
        return b


def changefilename():  # 更改文件名
    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\NewCarData'
    start = 65
    prefix = 'Ori_Mapdata'
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            destfile = prefix + "_%03d" % (start) + '.json'
            srcfile = os.path.join(srcdir, File)
            # print type(srcfile) , srcfile
            start += 1
            os.rename(srcfile, srcdir + '\\' + destfile)


def printfilename():  # 显示数据中的文件名表 json
    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\evaluation'
    index = -1
    print '['
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            # print '{"name": "' + File[:-5] + '"},'
            print '\''+File + '\''+','+'#' + str(index)
            index += 1
    print ']'


def getmap():  # 根据汽车GPS轨迹范围从OpenStreetMap下载地图数据
    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\MyJson'
    latmax = -90.0
    latmin = 90.0
    lngmax = -180.0
    lngmin = 180.0
    error = 0.0050
    index = 0
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            print '"' + File + '", //' + str(index)  # 程序中的文件名表
            index += 1
            filedirector = open(os.path.join(root, File), 'r')
            cardata = js.load(filedirector)
            for i in range(len(cardata)):
                latmax = max(latmax, cardata[i]["latitude"])
                latmin = min(latmin, cardata[i]["latitude"])
                lngmax = max(lngmax, cardata[i]["longitude"])
                lngmin = min(lngmin, cardata[i]["longitude"])
    # add error to expand the map
    latmin -= error
    latmax += error
    lngmin -= error
    lngmax += error
    print latmin, latmax, lngmin, lngmax


def sortCarData():  # 对数据按照基站id排序
    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\MyJson'
    destdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\BaseStation\\'

    index = 0
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            filedirector = open(os.path.join(root, File), 'r')
            sortdict = {}  # 排序集合
            print File
            cardata = js.load(filedirector)
            # print type(cardata)
            for i in range(len(cardata)):
                pci = cardata[i]["pci"]
                if pci in sortdict:
                    sortdict[pci] += [cardata[i]]
                else:
                    sortdict[pci] = [cardata[i]]
            outputdir = destdir + 'Ori_Mapdata' + "_%03d" % (index) + '.json'
            index += 1
            destfile = file(outputdir, 'w+')
            destfile.writelines('[\n')
            flag = 0
            for key in sortdict.keys():
                n = len(sortdict[key])
                for i in range(n):
                    output = ''
                    if flag == 1:
                        output += ',\n'
                    flag = 1
                    output += '{"longitude": ' + \
                        str(sortdict[key][i]['longitude'])
                    output += ',"latitude": ' + \
                        str(sortdict[key][i]['latitude'])
                    output += ',"pci": ' + str(sortdict[key][i]['pci'])
                    output += ',"rsrp": ' + str(sortdict[key][i]['rsrp'])
                    output += ',"rsrq": ' + str(sortdict[key][i]['rsrq'])
                    output += ',"ta": ' + str(sortdict[key][i]['ta']) + '}'

                    destfile.writelines(output)
            destfile.writelines('\n]\n')
    print 'sortCarData complete'


def Rad(d):
    PI = 3.141592653589793238462643383279502884
    return d * PI / 180.0


def Distance(lat1, lng1, lat2, lng2):
    radLat1 = Rad(lat1)
    radLat2 = Rad(lat2)
    a = radLat1 - radLat2
    b = Rad(lng1) - Rad(lng2)
    s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                                + Math.cos(radLat1) *
                                Math.cos(radLat2)
                                * Math.pow(Math.sin(b / 2), 2)))
    s = s * 6378137.0
    return s


def BasestationRedirect():  # 对车辆数据中的基站进行重新编号

    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\NewCarData'
    destdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\MyJson\\'
    index = 65
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            filedirector = open(os.path.join(root, File), 'r')
            Basestation = {}  # 基站集合
            cardata = js.load(filedirector)
            outputdir = destdir + 'Ori_Mapdata' + "_%03d" % (index) + '.json'
            destfile = file(outputdir, 'w+')
            destfile.writelines('[\n')
            hascomma = False
            starttime = cardata[0]['time']
            for i in range(len(cardata)):
                pci = cardata[i]["pci"] % 1000
                flag = False
                lat1 = cardata[i]['latitude']
                lng1 = cardata[i]['longitude']
                TA1 = cardata[i]['ta']
                time = cardata[i]['time']
                pciid = 0
                if pci in Basestation:
                    pciid = len(Basestation[pci])
                    for j in range(pciid):
                        lat2 = Basestation[pci][j][0]
                        lng2 = Basestation[pci][j][1]
                        TA2 = Basestation[pci][j][2]
                        dis = Distance(lng1, lat1, lng2, lat2)
                        if (dis < (TA1 + TA2) * 5) or (dis < 1000):  # 判断在同一基站
                            flag = True
                            pciid = j
                            break
                    if not flag:
                        Basestation[
                            pci] += [[lat1, lng1, TA1, pci + 1000 * pciid]]
                    else:
                        Basestation[pci][pciid] = [
                            lat1, lng1, TA1, pci + 1000 * pciid]
                else:
                    Basestation[pci] = [[lat1, lng1, TA1, pci]]
                output = ''
                if hascomma:
                    output += ',\n'
                hascomma = True
                output += '{"longitude": ' + str(lng1)
                output += ',"latitude": ' + str(lat1)
                output += ',"pci": ' + str(pci + 1000 * pciid)
                output += ',"rsrp": ' + str(cardata[i]['rsrp'])
                output += ',"rsrq": ' + str(cardata[i]['rsrq'])
                output += ',"ta": ' + str(TA1)
                output += ',"time": ' + str(time - starttime) + '}'
                destfile.writelines(output)
            index += 1
            destfile.writelines('\n]\n')


def BaseStationChangeError():  # 计算基站切换时两点的直线距离(试验用)
    srcdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\NewCarData\\NewData'
    destdir = 'G:\\mongomap\\cardata\\Zhengye_Drive_Testing_Data\\experiment\\'
    index = 0
    for root, dirs, files in os.walk(srcdir):
        for File in files:
            filedirector = open(os.path.join(root, File), 'r')
            cardata = js.load(filedirector)
            outputdir = destdir + 'ChangeError' + "_%03d" % (index) + '.json'
            destfile = file(outputdir, 'w+')
            prepci = cardata[0]['pci']
            for i in range(len(cardata)):
                pci = cardata[i]['pci']
                if pci != prepci:
                    lat1 = cardata[i - 1]['latitude']
                    lng1 = cardata[i - 1]['longitude']
                    lat2 = cardata[i]['latitude']
                    lng2 = cardata[i]['longitude']
                    time1 = cardata[i - 1]['time']
                    time2 = cardata[i]['time']
                    destfile.writelines(str(prepci) + ' ' + str(pci) + ' ' + str(
                        Distance(lat1, lng1, lat2, lng2)) + ' ' + str(time2 - time1) + '\n')
                    prepci = pci
            index += 1


# getmap()
# sortCarData()
# changefilename()
# sortCarData()
printfilename()
# BasestationRedirect()
# BaseStationChangeError()
