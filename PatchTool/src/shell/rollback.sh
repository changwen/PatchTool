#!/bin/bash
#获得当前运行的shell脚本所在目录
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -cp PatchTool-4.0-SNAPSHOT.jar com.pachira.tools.patchTool.main.Rollback $@ -p $DIR
     if [ "${?}" != "0" ]
      then
         echo "回滚失败！"
         exit 1
     fi
