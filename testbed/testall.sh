#!/bin/sh

TESTDIR=testdir
TESTLOG=test.log

[ -d ${TESTDIR} ] && rm -rf ${TESTDIR}
[ -f ${TESTLOG} ] && rm -f ${TESTLOG}
mkdir ${TESTDIR}

for template in *.vm
do
    echo ${template} >> ${TESTLOG}
    ./test.sh $template
    mv output ${TESTDIR}/${template}.output
    mv velocity.log ${TESTDIR}/${template}.log
done    

grep Exception ${TESTDIR}/*.output > error
