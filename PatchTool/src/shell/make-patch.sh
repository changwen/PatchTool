#!/bin/bash

java -cp PatchTool-4.0-SNAPSHOT.jar com.pachira.tools.patchTool.main.PatchProduction $@
if [  "${?}" != "0"  ]; then
    echo "生成补丁包时出错！"
    exit 1
fi

./checkoutPatch.sh $@
if [  "${?}" != "0"  ]; then
    echo "生成的补丁包的文件跟源文件不同，生成失败"
fi

i=1
for content in $@
do
   if [ $content == "-p" ]; then
    shift $i

    cp checkoutInstall.sh $1
    if [  "${?}" != "0"  ]; then
    echo "checkoutInstall.sh文件移动失败"
    fi

    cp install.sh $1
    if [  "${?}" != "0"  ]; then
    echo "install.sh文件移动失败"
    fi

    cp PatchTool-4.0-SNAPSHOT.jar $1
    if [  "${?}" != "0"  ]; then
    echo "PatchTool-4.0-SNAPSHOT.jar移动失败"
    fi

    cp rollback.sh $1
    if [  "${?}" != "0"  ]; then
    echo "rollback.sh移动失败"
    fi
   fi
i=$[ $i +1 ]
done