#include <stdio.h>
#include "wfdb.h"
#include "wfdb_lib.hpp"

static WFDB_Siginfo *siginfo;
static char *dataFile, *heaFile;

void init(char *data, char *hea) {
    dataFile = data;
    heaFile = hea;
}

void initSignalInfo(int frequency, int format, char *desc, char *units, int gain, int adcres, int adczero) {
    siginfo = (WFDB_Siginfo *) malloc(sizeof(WFDB_Siginfo));
    if (siginfo == NULL) {
        fprintf(stderr, "insufficient memory\n");
        exit(1);
    }

    //采样频率
    setsampfreq(frequency);
    siginfo->fmt = format;
    //数据记录文件
    ///cygdrive/c/Users/Jake/temp/
    siginfo->fname = dataFile;
    siginfo->group = 0;
    //信号描述
    siginfo->fmt = siginfo->fmt;
    siginfo->bsize = 0;
    //printf("Signal description [up to 30 characters]: ");
    siginfo->desc = desc;
    //printf("Signal units [up to 20 characters]: ");
    siginfo->units = units;
    //printf(" Signal gain [adu/%s]: ", s->units);
    siginfo->gain = gain; //mV
    //printf(" Signal ADC resolution in bits [8-16]: ");
    siginfo->adcres = adcres;
    //printf(" Signal ADC zero level [adu]: ");
    siginfo->adczero = adczero;

}

//打开wfdb数据库文件
void open() {
    if (osigfopen(siginfo, 1) < 1) exit(1);
}

//设置采样开始的基准时间
void setBaseTime(char *time){
    setbasetime(time);
}

void doSample(int* array, long len) {
    //采样数据
    WFDB_Sample *v = (WFDB_Sample *) malloc(sizeof(WFDB_Sample));
    if (v == NULL) {
        fprintf(stderr, "insufficient memory\n");
        exit(1);
    }

    for (int i = 0; i < len; i++) {
        *v = array[i];
        if (putvec(v) < 0) exit(1);
    }

    free(v);

    (void) newheader(heaFile);
}

void close(){
    //wfdb 库退出
    wfdbquit();
}

int main() {

    //数据hea文件名
    ///cygdrive/c/Users/Jake/temp/
    init("ECGRec_201917-C600026_2019-08-12.dat", "ECGRec_201917-C600026_2019-08-12");

    initSignalInfo(128, 16, "测试...", "mV", 1000, 0, 0);

    open();

    setBaseTime("14:32:33 05/11/2019");

    int sample_Records = 86400 * 128;
    int *sample = new int[sample_Records];
    for (int i = 0; i < sample_Records; i++) {
        sample[i] = (rand() % (2000 - 100 + 1)) + 100;
    }

    doSample(sample, sample_Records);
    doSample(sample, sample_Records);
    doSample(sample, sample_Records);

    close();

    //程序退出
    exit(0);
}
