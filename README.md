# cuckoo agent

## Introduction
本專題之目標在於架設 **分散式惡意軟體分析系統** ，讓惡意軟體分析工作可以平行化的進行，大量減少分析時所需要的時間。

我們採用 master 與 worker 的架構來實作分散式系統，並透過自行撰寫的 master agent 與 worker agent 來執行工作的分配與report 的回傳。

<img src="https://github.com/a110605/cuckoo_agent/blob/master/pictures/1.png" height="400" width="600">

## Implementation
1. Follow the [cuckoo installation manual](http://docs.cuckoosandbox.org/en/latest/installation/) to setup cuckoo system enviornment. 

2. Run server agent on cuckoo master node
```
$ java -jar cuckooServer.jar 
```

3. Run client agent on cuckoo slave node
```
$ java -jar cuckooWorker.jar
```

4. Cuckoo master will distribute malware samples to cuckoo slave and start analyzing tasks.

5. After finished tasks, cuckoo slave will transmit report (.html & .json) back to master.   


## System Evalution
- Time consumption of analyzing 50 malware samples using standalone cuckoo and distribute cuckoo system. 

<img src="https://github.com/a110605/cuckoo_agent/blob/master/pictures/2.png" height="400" width="600">

- Time of analysis tasks with various resources in distributed cuckoo system 

<img src="https://github.com/a110605/cuckoo_agent/blob/master/pictures/3.png" height="400" width="600">


## Authors
- 李士暄 r03725019@ntu.edu.tw
- 葉展奇 r04725042@ntu.edu.tw

Please contact me via  the email above. Thanks 

## Reference 
For more detail infomration, please refer to [開源期末專題報告](https://drive.google.com/file/d/0B7gHAJvjPzxVV09fbmY5c28weE0/view?usp=sharing)

