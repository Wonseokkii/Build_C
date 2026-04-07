//
// Created by Jake on 2019.11.8.
//

#ifndef VITALSDKPROJECT_MIT16_HPP
#define VITALSDKPROJECT_MIT16_HPP

#endif //VITALSDKPROJECT_MIT16_HPP

void init(char *data, char *hea);

void initSignalInfo(int frequency, int format, char *desc, char *units, int gain, int adcres, int adczero);

//打开wfdb数据库文件
void open();

//设置采样开始的基准时间
void setBaseTime(char *time);

void doSample(int* array, long len);

void close();
