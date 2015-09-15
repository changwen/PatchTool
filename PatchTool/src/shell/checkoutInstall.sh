#!/bin/bash
#installCheckout.sh
old="$2"
#获得当前运行的shell脚本所在目录
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
patchList=${DIR}"/patchList.txt"

cat $patchList | while read line
do
   first=`echo $line | cut -d ' ' -f 1`
   second1=`echo $line | cut -d ' ' -f 2`
   second=$(echo $second1 | tr -d "\n\r")   #截取的最后一列有换行符，需要去除

   oldFile=${old}${second}
   patchFile=${DIR}${second}

   if [ "$first" == "add" ] || [ "$first" == "modify" ]
   then
      #安装校验
      diff  $patchFile $oldFile
      if [ "${?}" != "0" ]
      then
         echo "Install  failed! Please try again"
         exit 1
      fi

   elif [ "$first" == "delete" ]
   then
      #安装校验
      if [ -e "$oldFile" ]
      then
         echo "Install failed! Please try again"
         exit 1
      fi

  fi
done