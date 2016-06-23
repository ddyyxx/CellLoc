数据库打开命令：
mongod --dbpath XXX（后面的XXX是数据地址 mongo文件夹地址）

数据准备：

地图：现在大连的地图基本上全了，如果换成其他城市地图，
其获取方法如下：
从https://www.openstreetmap.org 截取地图，输入经纬度范围（经纬度范围可根据车辆数据获得，也可以自己估计），点击导出即可。下载下来的地图放在mongomap/NewMap文件夹下。
将地图数据导入数据库:
运行MapProcess.MapInsertToMdb中的主函数即可：

基站坐标：
对于一个新的测试数据文件，将该文件名加入到defcons.SystemSettings中的filename_array中，并且将该文件进行处理（将相同基站ID的数据放在一起）后放入cardata/Basestation中，运行basestation.MainReader中的主函数（它计算所有测试数据的基站，你可以用注释的方法仅计算新的测试数据的基站坐标），计算好的基站数据在cardata/solu中。

数据测试：
运行location.DoExperiment中的主函数，可以进行实验，你可以选则测试多个数据或者只测试单个数据，选择DoExperiment中的l,r值即可。

defcons.SystemSettings中有许多设定，具体可以看其中的注释。

显示：

进入G:\mongomap\osmmap\PythonMap
执行 python -m SimpleHTTPServer
打开网页： localhost:8000
